package bomberman.arsw.Socket;

import bomberman.arsw.Model.*;
import bomberman.arsw.Service.roomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.beans.factory.annotation.Autowired;
import bomberman.arsw.util.CryptoUtils;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Controller
public class WebSocketController {
    private final roomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ScheduledExecutorService scheduledExecutor;

    @Autowired
    public WebSocketController(roomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    @MessageMapping("/room/{roomCode}/join")
    public void joinRoom(@DestinationVariable String roomCode, @Payload PlayerJoinRequest request) {
        Player player = new Player(

                0,  // initial X position
                0,  // initial Y position
                1,  // initial lives
                request.getPlayerName(),
                1   // initial bomb capacity
        );

        boolean isFirstPlayer = roomService.getPlayersInRoom(roomCode).isEmpty();
        player.setHost(isFirstPlayer);

        roomService.addPlayerToRoom(roomCode, player);

        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/ready")
    public void toggleReady(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.togglePlayerReadyStatus(roomCode, request.getPlayerId());
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/start")
    public void startGame(@DestinationVariable String roomCode, @Payload Map<String, Object> payload) {
        System.out.println("Received start request: " + payload); // Debug

        String playerId = (String) payload.get("playerId");

        Map<String, Object> configPayload = (Map<String, Object>) payload.get("config");
        int duration = configPayload != null ? (int) configPayload.get("duration") : 300; // Default 5 min
        int lives = configPayload != null ? (int) configPayload.get("lives") : 5; // Default 3 vidas

        if (roomService.isHost(roomCode, playerId) && roomService.canStartGame(roomCode)) {
            // 1. Crear configuración
            GameConfig config = new GameConfig(duration, lives); // 5 min, 3 vidas

            // 2. Obtener jugadores
            List<Player> players = roomService.getPlayersInRoom(roomCode);

            // 3. Crear tablero (esto ahora crea el mapa internamente)
            roomService.createGameBoard(roomCode, config, players);

            players.forEach(p -> p.setLives(config.getLives()));

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
        try {
            List<Player> players = roomService.getPlayersInRoom(roomCode);
            String hostId = players.isEmpty() ? "" : players.get(0).getId();

            // Crear estructura de datos
            Map<String, Object> data = new HashMap<>();
            data.put("host", hostId);
            data.put("type", "PLAYER_UPDATE");
            data.put("players", players.stream().map(Player::toMap).collect(Collectors.toList()));

            // Convertir a JSON y cifrar
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            String encrypted = CryptoUtils.encrypt(json); // Asegúrate que este método funciona

            // Crear mensaje cifrado
            Map<String, Object> encryptedPayload = new HashMap<>();
            encryptedPayload.put("type", "ENCRYPTED");
            encryptedPayload.put("payload", encrypted);

            // Enviar mensaje
            messagingTemplate.convertAndSend("/topic/room/" + roomCode, encryptedPayload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/room/{roomCode}/status")
    public void getGameStatus(@DestinationVariable String roomCode) {
        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            try {
                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_START");
                response.put("config", board.getConfig());
                response.put("players", board.getPlayers());

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(response);
                String encrypted = CryptoUtils.encrypt(json);

                Map<String, Object> encryptedPayload = new HashMap<>();
                encryptedPayload.put("type", "ENCRYPTED");
                encryptedPayload.put("payload", encrypted);

                messagingTemplate.convertAndSend("/topic/room/" + roomCode, encryptedPayload);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @MessageMapping("/game/{roomCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String roomCode,
            @Payload PlayerMoveRequest request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            synchronized (board) {
                Player player = board.getPlayerById(request.getPlayerId());
                if (player != null && board.movePlayer(player, request.getNewX(), request.getNewY())) {
                    System.out.println(
                            "[BROADCAST] Jugador " + player.getName() +
                                    " movido a (" + request.getNewX() + ", " + request.getNewY() + ")" +
                                    " en sala: " + roomCode
                    );
                    broadcastGameState2(roomCode, board); // Enviar nuevo estado del juego
                }
            }
        }
    }


    private void broadcastGameState2(String roomCode, GameBoard board) {
        try {
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

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);
            String encrypted = CryptoUtils.encrypt(json);

            Map<String, Object> encryptedPayload = new HashMap<>();
            encryptedPayload.put("type", "ENCRYPTED");
            encryptedPayload.put("payload", encrypted);

            messagingTemplate.convertAndSend("/topic/game/" + roomCode, encryptedPayload);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
        try {
            Map<String, Object> response = Map.of(
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
                                    "id", b.getId(),
                                    "x", b.getX(),
                                    "y", b.getY(),
                                    "timer", b.getTimer(),
                                    "range", b.getRange(),
                                    "playerId", b.getPlayerId()
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
            );

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(response);
            String encrypted = CryptoUtils.encrypt(json);

            Map<String, Object> encryptedPayload = new HashMap<>();
            encryptedPayload.put("type", "ENCRYPTED");
            encryptedPayload.put("payload", encrypted);

            messagingTemplate.convertAndSend("/topic/game/" + roomCode, encryptedPayload);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            try {
                Map<String, Object> state = Map.of(
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
                );

                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_UPDATE");
                response.put("state", state);

                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(response);
                String encrypted = CryptoUtils.encrypt(json);

                Map<String, Object> encryptedPayload = new HashMap<>();
                encryptedPayload.put("type", "ENCRYPTED");
                encryptedPayload.put("payload", encrypted);

                messagingTemplate.convertAndSend("/topic/game/" + roomCode, encryptedPayload);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @MessageMapping("/game/{roomCode}/spawnPowerup")
    public void spawnPowerup(
            @DestinationVariable String roomCode,
            @Payload PowerupPosition position) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            synchronized (board) {
                try {
                    LifeUpPowerUp powerup = new LifeUpPowerUp();
                    powerup.setPosition(position.getRow(), position.getCol());
                    board.addPowerUp(powerup);

                    // Cambiar el formato del mensaje para que coincida con lo que espera el frontend
                    messagingTemplate.convertAndSend(
                            "/topic/game/" + roomCode + "/powerup",
                            Map.of(
                                    "row", position.getRow(),
                                    "col", position.getCol(),
                                    "show", true,
                                    "type", "LIFE_UP" // Añadir tipo para consistencia
                            )
                    );

                    scheduledExecutor.schedule(() -> {
                        synchronized (board) {
                            if (board.getPowerUps().contains(powerup)) {
                                board.removePowerUp(powerup);
                                messagingTemplate.convertAndSend(
                                        "/topic/game/" + roomCode + "/powerup",
                                        Map.of(
                                                "row", position.getRow(),
                                                "col", position.getCol(),
                                                "show", false
                                        )
                                );
                            }
                        }
                    }, 10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @MessageMapping("/game/{roomCode}/collectPowerup")
    public void collectPowerup(
            @DestinationVariable String roomCode,
            @Payload PowerupCollectRequest request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            synchronized (board) {
                try {
                    Optional<PowerUp> powerupOpt = board.getPowerUpAt(request.getX(), request.getY());

                    if (powerupOpt.isPresent()) {
                        PowerUp powerup = powerupOpt.get();
                        Player player = board.getPlayerById(request.getPlayerId());

                        if (player != null) {
                            powerup.applyEffect(player);
                            board.removePowerUp(powerup);

                            // Notificar a todos los clientes
                            messagingTemplate.convertAndSend(
                                    "/topic/game/" + roomCode + "/powerup",
                                    Map.of(
                                            "type", "POWERUP_COLLECTED",
                                            "powerUpType", powerup.getType().name(), // Incluir tipo
                                            "x", powerup.getX(),
                                            "y", powerup.getY(),
                                            "playerId", player.getId(),
                                            "show", false
                                    )
                            );

                            sendGameUpdate(roomCode, board);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @MessageMapping("/game/{roomCode}/guardar")
    public void guardarEstado(@DestinationVariable String roomCode) {
        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            roomService.saveGameState(roomCode, board); // Este método debe existir en roomService
            System.out.println("Estado del juego guardado para la sala " + roomCode);
        }
    }

    @MessageMapping("/game/{roomCode}/leer")
    public void leerEstado(@DestinationVariable String roomCode) {
        GameBoard board = roomService.loadGameState(roomCode); // Este método también debe existir
        if (board != null) {
            roomService.setGameBoard(roomCode, board); // Este método debería permitir cargar el board
            sendGameUpdate(roomCode, board); // Reutilizas tu método para enviar el estado al cliente
            System.out.println("Estado del juego cargado para la sala " + roomCode);
        }
    }



}