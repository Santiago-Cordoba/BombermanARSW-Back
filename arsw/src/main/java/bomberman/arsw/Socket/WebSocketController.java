package bomberman.arsw.Socket;

import bomberman.arsw.Model.GameBoard;
import bomberman.arsw.Model.GameConfig;
import bomberman.arsw.Model.Player;
import bomberman.arsw.Service.roomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
public class WebSocketController {
    private final roomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WebSocketController(roomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/{roomCode}/join")
    public void joinRoom(@DestinationVariable String roomCode, @Payload PlayerJoinRequest request) {
        Player player = new Player(

                0,  // initial X position
                0,  // initial Y position
                3,  // initial lives
                request.getPlayerName(),
                1   // initial bomb capacity
        );

        boolean isFirstPlayer = roomService.getPlayersInRoom(roomCode).isEmpty();
        player.setHost(isFirstPlayer);

        roomService.addPlayerToRoom(roomCode, player);

        // Enviar actualización a todos los jugadores de la sala
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/ready")
    public void toggleReady(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.togglePlayerReadyStatus(roomCode, request.getPlayerId());
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/start")
    public void startGame(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        System.out.println("SOLICITUD DE INICIO PARA SALA: " + roomCode);
        System.out.println("Jugador solicitante: " + request.getPlayerId());

        // Verificar condiciones
        boolean canStart = roomService.canStartGame(roomCode);
        boolean isHost = roomService.isHost(roomCode, request.getPlayerId());

        System.out.println("Puede iniciar: " + canStart);
        System.out.println("Es host: " + isHost);

        if (canStart && isHost) {
            System.out.println("CREANDO TABLERO PARA SALA: " + roomCode);

            GameConfig gameConfig = new GameConfig(5 * 60, 3);
            List<Player> players = roomService.getPlayersInRoom(roomCode);

            System.out.println("Jugadores en sala:");
            players.forEach(p -> System.out.println("- " + p.getName() + " (ID: " + p.getId() + ")"));

            // Crear y almacenar tablero
            roomService.createGameBoard(roomCode, gameConfig, players);

            // Verificar que el tablero se creó
            GameBoard board = roomService.getGameBoard(roomCode);
            System.out.println("Tablero creado: " + (board != null));

            // Enviar estado inicial a todos los jugadores
            messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                    Map.of(
                            "type", "GAME_START",
                            "config", gameConfig,
                            "players", players.stream().map(p -> Map.of(
                                    "id", p.getId(),
                                    "name", p.getName(),
                                    "row", p.getX(),
                                    "col", p.getY(),
                                    "lives", p.getLives()
                            )).collect(Collectors.toList()),
                            "board", board.getBoardState() // Añadir estado del tablero
                    )
            );

            System.out.println("MENSAJE DE INICIO ENVIADO A CLIENTES");
        } else {
            System.out.println("NO SE CUMPLEN LAS CONDICIONES PARA INICIAR");
        }
    }

    @MessageMapping("/room/{roomCode}/leave")
    public void leaveRoom(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.removePlayerFromRoom(roomCode, request.getPlayerId());
        sendRoomUpdate(roomCode);
    }

    private void sendRoomUpdate(String roomCode) {
        List<Player> players = roomService.getPlayersInRoom(roomCode);
        String hostId = players.isEmpty() ? "" : players.get(0).getId();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "PLAYER_UPDATE");
        response.put("players", players);
        response.put("host", hostId);

        messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
    }

    @MessageMapping("/room/{roomCode}/status")
    public void getGameStatus(@DestinationVariable String roomCode) {
        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            board.update();
            messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                    Map.of(
                            "type", "GAME_START",
                            "config", board.getConfig(),
                            "players", board.getPlayers()
                    )
            );
        }
    }

    @MessageMapping("/room/{roomCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String roomCode,
            @Payload Map<String, Object> payload) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            String playerId = (String) payload.get("playerId");
            int newX = (int) payload.get("x");
            int newY = (int) payload.get("y");

            Player player = board.getPlayerById(playerId);
            if (player != null) {
                player.setPosition(newX, newY);
                board.update();

                // Notificar a todos los jugadores del movimiento
                messagingTemplate.convertAndSend("/topic/room/" + roomCode + "/movement",
                        Map.of(
                                "type", "PLAYER_MOVED",
                                "playerId", playerId,
                                "x", newX,
                                "y", newY,
                                "board", board.getBoardState()
                        )
                );
            }
        }
    }

}