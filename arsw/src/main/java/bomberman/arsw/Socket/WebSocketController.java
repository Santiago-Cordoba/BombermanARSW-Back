package bomberman.arsw.Socket;

import bomberman.arsw.Model.*;
import bomberman.arsw.Service.roomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
    public void startGame(@DestinationVariable String roomCode, @Payload Map<String, Object> payload) {
        String playerId = (String) payload.get("playerId");

        if (roomService.isHost(roomCode, playerId) && roomService.canStartGame(roomCode)) {
            // 1. Crear configuración
            GameConfig config = new GameConfig(300, 3); // 5 min, 3 vidas

            // 2. Obtener jugadores
            List<Player> players = roomService.getPlayersInRoom(roomCode);

            // 3. Crear tablero (esto ahora crea el mapa internamente)
            roomService.createGameBoard(roomCode, config, players);

            // 4. Obtener tablero creado
            GameBoard board = roomService.getGameBoard(roomCode);
            if (board == null) {
                throw new IllegalStateException("Game board not initialized for room: " + roomCode);
            }

            // 5. Formatear jugadores como Map (suponiendo que tienes un método toMap())
            List<Map<String, Object>> playersData = players.stream()
                    .map(Player::toMap)
                    .toList();

            // 6. Formatear mapa
            GameMap gameMap = board.getGameMap();
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("width", gameMap.getWidth());
            mapData.put("height", gameMap.getHeight());
            mapData.put("cells", gameMap.getCellStates());

            // 7. Formatear configuración como Map (opcional si el frontend no acepta el objeto Java tal cual)
            Map<String, Object> configData = new HashMap<>();
            configData.put("duration", config.getDuration());
            configData.put("lives", config.getLives());

            // 8. Armar mensaje completo
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_START");
            response.put("config", configData);
            response.put("players", playersData);
            response.put("map", mapData);

            // 9. Enviar mensaje a los clientes
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
            messagingTemplate.convertAndSend("/topic/game/" + roomCode, response);
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

    @MessageMapping("/game/{roomCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String roomCode,
            @Payload PlayerMoveRequest request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            Player player = board.getPlayerById(request.getPlayerId());
            if (player != null && board.movePlayer(player, request.getNewX(), request.getNewY())) {
                // ¡Log para depuración!
                System.out.println(
                        "[BROADCAST] Jugador " + player.getName() +
                                " movido a (" + request.getNewX() + ", " + request.getNewY() + ")" +
                                " en sala: " + roomCode
                );
                broadcastGameState2(roomCode, board); // Envía el estado actualizado
            }
        }
    }

    private void broadcastGameState2(String roomCode, GameBoard board) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "GAME_UPDATE");
        response.put("players", board.getPlayers().stream()
                .map(p -> Map.of(
                        "id", p.getId(),
                        "name", p.getName(),
                        "x", p.getX(),
                        "y", p.getY(),
                        "lives", p.getLives(),
                        "bombCapacity", p.getBombCapacity()
                ))
                .collect(Collectors.toList()));
        response.put("map", board.getGameMap().getCellStates());
        response.put("powerUps", board.getPowerUps().stream()
                .map(pu -> Map.of(
                        "type", pu.getType().name(),
                        "x", pu.getX(),
                        "y", pu.getY()
                ))
                .collect(Collectors.toList()));

        messagingTemplate.convertAndSend("/topic/game/" + roomCode, response);
    }

    @MessageMapping("/game/{roomCode}/placeBomb")
    public void handlePlaceBomb(
            @DestinationVariable String roomCode,
            @Payload PlayerActionRequest request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            Player player = board.getPlayerById(request.getPlayerId());
            if (player != null) {
                // Colocar la bomba
                board.placeBomb(player.getX(), player.getY(), player);
                sendGameUpdate(roomCode, board);
            }
        }
    }
    private void sendGameUpdate(String roomCode, GameBoard board) {
        messagingTemplate.convertAndSend("/topic/game/" + roomCode, Map.of(
                "type", "GAME_UPDATE",
                "players", board.getPlayers().stream()
                        .map(p -> Map.of(
                                "id", p.getId(),
                                "name", p.getName(),
                                "x", p.getX(),
                                "y", p.getY(),
                                "lives", p.getLives(),
                                "bombCapacity", p.getBombCapacity(),
                                "bombRange", p.getBombRange()
                        ))
                        .collect(Collectors.toList()),
                "bombs", board.getBombs().stream()
                        .map(b -> Map.of(
                                "id", b.getId(), // ¡Nuevo campo!
                                "x", b.getX(),
                                "y", b.getY(),
                                "timer", b.getTimer(),
                                "range", b.getRange(), // ¡Nuevo campo!
                                "playerId", b.getPlayerId() // ¡Nuevo campo!
                        ))
                        .collect(Collectors.toList()),
                "map", board.getGameMap().getCellStates(),
                "powerUps", board.getPowerUps().stream()
                        .map(pu -> Map.of(
                                "type", pu.getType().name(),
                                "x", pu.getX(),
                                "y", pu.getY()
                        ))
                        .collect(Collectors.toList())
        ));
    }

    private void broadcastGameState(String roomCode, GameBoard board) {
        messagingTemplate.convertAndSend("/topic/game/" + roomCode, Map.of(
                "type", "GAME_UPDATE",
                "state", board.getGameStateJson()
        ));
    }


    @MessageMapping("/game/{roomCode}/init")
    public void initGame(
            @DestinationVariable String roomCode,
            @Payload Map<String, Object> request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            // Enviar estado completo del juego
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_UPDATE");
            response.put("state", Map.of(
                    "players", board.getPlayers().stream().map(p -> Map.of(
                            "id", p.getId(),
                            "name", p.getName(),
                            "x", p.getX(),
                            "y", p.getY(),
                            "lives", p.getLives()
                    )).collect(Collectors.toList()),
                    "map", Map.of(
                            "width", board.getGameMap().getWidth(),
                            "height", board.getGameMap().getHeight(),
                            "cells", board.getGameMap().getCellStates()
                    ),
                    "config", Map.of(
                            "duration", board.getConfig().getDuration(),
                            "lives", board.getConfig().getLives()
                    )
            ));

            messagingTemplate.convertAndSend("/topic/game/" + roomCode, response);
        }
    }

    @MessageMapping("/game/{roomCode}/collectPowerUp")
    public void handleCollectPowerUp(
            @DestinationVariable String roomCode,
            @Payload Map<String, Object> payload) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            String playerId = (String) payload.get("playerId");
            int x = (int) payload.get("x");
            int y = (int) payload.get("y");

            if (board.collectPowerUp(playerId, x, y)) {
                broadcastGameState2(roomCode, board);
            }
        }
    }

    @Scheduled(fixedRate = 100) // cada 100ms
    public void broadcastGameState() {
        for (String roomCode : roomService.getAllRoomCodes()) {
            GameBoard board = roomService.getGameBoard(roomCode);
            if (board != null) {
                messagingTemplate.convertAndSend("/topic/game/" + roomCode, Map.of(
                        "type", "GAME_UPDATE",
                        "config", Map.of(
                                "duration", board.getConfig().getDuration(),
                                "lives", board.getConfig().getLives()
                        ),
                        "players", board.getPlayers().stream().map(p -> Map.of(
                                "id", p.getId(),
                                "name", p.getName(),
                                "x", p.getX(),
                                "y", p.getY(),
                                "lives", p.getLives(),
                                "bombCapacity", p.getBombCapacity(),
                                "bombRange", p.getBombRange(),
                                "invincible", p.isInvincible()
                        )).collect(Collectors.toList()),
                        "map", Map.of(
                                "width", board.getGameMap().getWidth(),
                                "height", board.getGameMap().getHeight(),
                                "cells", board.getGameMap().getCellStates()
                        ),
                        "bombs", board.getBombs().stream().map(b -> Map.of(
                                "id", b.getId(),
                                "x", b.getX(),
                                "y", b.getY(),
                                "timer", b.getTimer(),
                                "range", b.getRange(),
                                "playerId", b.getPlayerId()
                        )).collect(Collectors.toList()),
                        "powerUps", board.getPowerUps().stream().map(pu -> Map.of(
                                "type", pu.getType().name(),
                                "x", pu.getX(),
                                "y", pu.getY()
                        )).collect(Collectors.toList()),
                        "explosions", board.getExplosions().stream()
                                .map(e -> Map.of(
                                        "x", e.getX(),
                                        "y", e.getY(),
                                        "isCenter", e.isCenter()
                                ))
                                .collect(Collectors.toList())
                ));
            }
        }
    }


}