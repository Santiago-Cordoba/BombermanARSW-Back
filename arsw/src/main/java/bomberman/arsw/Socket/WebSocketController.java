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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public WebSocketController(roomService roomService, SimpMessagingTemplate messagingTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
        this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    // Método helper para enviar mensajes cifrados
    private void sendEncryptedMessage(String destination, Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            String encrypted = CryptoUtils.encrypt(json);

            Map<String, Object> encryptedPayload = new HashMap<>();
            encryptedPayload.put("type", "ENCRYPTED");
            encryptedPayload.put("payload", encrypted);

            messagingTemplate.convertAndSend(destination, encryptedPayload);

            // Log para depuración
            System.out.println("Mensaje cifrado enviado a " + destination + ": " + encrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        System.out.println("Received start request: " + payload);

        String playerId = (String) payload.get("playerId");

        Map<String, Object> configPayload = (Map<String, Object>) payload.get("config");
        int duration = configPayload != null ? (int) configPayload.get("duration") : 300;
        int lives = configPayload != null ? (int) configPayload.get("lives") : 5;

        if (roomService.isHost(roomCode, playerId) && roomService.canStartGame(roomCode)) {
            GameConfig config = new GameConfig(duration, lives);
            List<Player> players = roomService.getPlayersInRoom(roomCode);
            roomService.createGameBoard(roomCode, config, players);

            players.forEach(p -> p.setLives(config.getLives()));

            GameBoard board = roomService.getGameBoard(roomCode);
            if (board == null) {
                throw new IllegalStateException("Game board not initialized for room: " + roomCode);
            }

            List<Map<String, Object>> playersData = players.stream()
                    .map(Player::toMap)
                    .toList();

            GameMap gameMap = board.getGameMap();
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("width", gameMap.getWidth());
            mapData.put("height", gameMap.getHeight());
            mapData.put("cells", gameMap.getCellStates());

            Map<String, Object> configData = new HashMap<>();
            configData.put("duration", config.getDuration());
            configData.put("lives", config.getLives());

            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_START");
            response.put("config", configData);
            response.put("players", playersData);
            response.put("map", mapData);

            sendEncryptedMessage("/topic/room/" + roomCode, response);
            sendEncryptedMessage("/topic/game/" + roomCode, response);
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

        Map<String, Object> data = new HashMap<>();
        data.put("host", hostId);
        data.put("type", "PLAYER_UPDATE");
        data.put("players", players.stream().map(Player::toMap).collect(Collectors.toList()));

        sendEncryptedMessage("/topic/room/" + roomCode, data);
    }

    @MessageMapping("/room/{roomCode}/status")
    public void getGameStatus(@DestinationVariable String roomCode) {
        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_START");
            response.put("config", board.getConfig());
            response.put("players", board.getPlayers());

            sendEncryptedMessage("/topic/room/" + roomCode, response);
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
                    broadcastGameState(roomCode, board);
                }
            }
        }
    }

    private void broadcastGameState(String roomCode, GameBoard board) {
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

        sendEncryptedMessage("/topic/game/" + roomCode, response);
    }

    @MessageMapping("/game/{roomCode}/placeBomb")
    public void handlePlaceBomb(
            @DestinationVariable String roomCode,
            @Payload PlayerActionRequest request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
            Player player = board.getPlayerById(request.getPlayerId());
            if (player != null) {
                board.placeBomb(player.getX(), player.getY(), player);
                sendGameUpdate(roomCode, board);
            }
        }
    }

    private void sendGameUpdate(String roomCode, GameBoard board) {
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

        sendEncryptedMessage("/topic/game/" + roomCode, response);
    }

    @MessageMapping("/game/{roomCode}/init")
    public void initGame(
            @DestinationVariable String roomCode,
            @Payload Map<String, Object> request) {

        GameBoard board = roomService.getGameBoard(roomCode);
        if (board != null) {
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

            sendEncryptedMessage("/topic/game/" + roomCode, response);
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

                            // Mensaje sobre el powerup (no cifrado para este ejemplo)
                            messagingTemplate.convertAndSend(
                                    "/topic/game/" + roomCode + "/powerup",
                                    Map.of(
                                            "type", "POWERUP_COLLECTED",
                                            "powerUpType", powerup.getType().name(),
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
            roomService.saveGameState(roomCode, board);
            System.out.println("Estado del juego guardado para la sala " + roomCode);
            GameBoard loadedBoard = roomService.loadGameState("testRoom");
            System.out.println("Loaded game board: " + loadedBoard);
        }
    }

    @MessageMapping("/game/{roomCode}/leer")
    public void leerEstado(@DestinationVariable String roomCode) {
        GameBoard board = roomService.loadGameState(roomCode);
        if (board != null) {
            roomService.setGameBoard(roomCode, board);
            sendGameUpdate(roomCode, board);
            System.out.println("Estado del juego cargado para la sala " + roomCode);
        }
    }
}