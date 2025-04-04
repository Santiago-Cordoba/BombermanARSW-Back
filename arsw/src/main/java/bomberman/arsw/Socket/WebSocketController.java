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
        if (roomService.canStartGame(roomCode) && roomService.isHost(roomCode, request.getPlayerId())) {
            // Configuración del juego
            GameConfig gameConfig = new GameConfig(5 * 60, 3); // 5 minutos en segundos, 3 vidas

            // Obtener todos los jugadores de la sala
            List<Player> players = roomService.getPlayersInRoom(roomCode);

            // Crear y guardar el tablero con la configuración y jugadores
            roomService.createGameBoard(roomCode, gameConfig, players);

            // Notificar a todos los jugadores que el juego comienza con la info de jugadores
            messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                    Map.of(
                            "type", "GAME_START",
                            "config", gameConfig,
                            "players", players.stream().map(p -> Map.of(
                                    "id", p.getId(),
                                    "name", p.getName(),
                                    "row", p.getX(),  // Asegúrate que estas propiedades existen
                                    "col", p.getY(),
                                    "lives", p.getLives()
                            )).collect(Collectors.toList())
                    )
            );
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
            messagingTemplate.convertAndSend("/topic/room/" + roomCode,
                    Map.of(
                            "type", "GAME_START",
                            "config", board.getConfig(),
                            "players", board.getPlayers()
                    )
            );
        }
    }


}