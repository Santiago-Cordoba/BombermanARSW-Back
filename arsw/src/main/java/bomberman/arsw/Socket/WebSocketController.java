package bomberman.arsw.Socket;

import bomberman.arsw.Model.*;
import bomberman.arsw.Service.roomService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WebSocketController {
    private final roomService roomService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public WebSocketController(roomService roomService,
                               SimpMessagingTemplate messagingTemplate,
                               RedisTemplate<String, Object> redisTemplate) {
        this.roomService = roomService;
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
    }


    @MessageMapping("/room/{roomCode}/join")
    public void joinRoom(@DestinationVariable String roomCode, @Payload PlayerJoinRequest request) {
        // Siempre cargar primero desde Redis
        Room existingRoom = roomService.loadRoomFromRedis(roomCode);

        // Verificar jugador existente
        if (existingRoom != null && existingRoom.getPlayers().stream()
                .anyMatch(p -> p.getName().equals(request.getPlayerName()))) {
            throw new IllegalArgumentException("Ya existe un jugador con ese nombre en la sala");
        }

        Player player = new Player(0, 0, 3, request.getPlayerName(), 1);

        // Operación atómica usando Redis
        redisTemplate.execute(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.watch("room:players:" + roomCode);

                Room room = (Room) operations.opsForValue().get("room:players:" + roomCode);
                boolean isFirstPlayer = (room == null || room.getPlayers().isEmpty());
                player.setHost(isFirstPlayer);

                operations.multi();
                if (room == null) {
                    room = new Room(roomCode);
                }
                room.addPlayer(player);
                operations.opsForValue().set("room:players:" + roomCode, room);

                return operations.exec();
            }
        });

        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/ready")
    public void toggleReady(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.togglePlayerReadyStatus(roomCode, request.getPlayerId());
        saveRoomStateToRedis(roomCode);
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/room/{roomCode}/start")
    public void startGame(@DestinationVariable String roomCode, @Payload Map<String, Object> payload) {
        String playerId = (String) payload.get("playerId");
        Map<String, Object> configPayload = (Map<String, Object>) payload.get("config");
        int duration = configPayload != null ? (int) configPayload.get("duration") : 300;
        int lives = configPayload != null ? (int) configPayload.get("lives") : 3;

        if (roomService.isHost(roomCode, playerId) && roomService.canStartGame(roomCode)) {
            GameConfig config = new GameConfig(duration, lives);
            List<Player> players = roomService.getPlayersInRoom(roomCode);
            roomService.createGameBoard(roomCode, config, players);
            players.forEach(p -> p.setLives(config.getLives()));

            GameBoard board = roomService.getGameBoard(roomCode);
            if (board == null) {
                throw new IllegalStateException("Game board not initialized for room: " + roomCode);
            }

            // Guardar estado completo en Redis
            saveGameStateToRedis(roomCode, board);

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

            messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
            messagingTemplate.convertAndSend("/topic/game/" + roomCode, response);
        }
    }

    @MessageMapping("/room/{roomCode}/leave")
    public void leaveRoom(@DestinationVariable String roomCode, @Payload PlayerActionRequest request) {
        roomService.removePlayerFromRoom(roomCode, request.getPlayerId());
        saveRoomStateToRedis(roomCode);
        sendRoomUpdate(roomCode);
    }

    @MessageMapping("/game/{roomCode}/move")
    public void handlePlayerMove(
            @DestinationVariable String roomCode,
            @Payload PlayerMoveRequest request) {

        GameBoard board = loadGameStateFromRedis(roomCode);
        if (board == null) {
            board = roomService.getGameBoard(roomCode);
        }

        if (board != null) {
            synchronized (board) {
                Player player = board.getPlayerById(request.getPlayerId());
                if (player != null && board.movePlayer(player, request.getNewX(), request.getNewY())) {
                    // Guardar el estado actualizado en Redis
                    saveGameStateToRedis(roomCode, board);

                    sendGameUpdate(roomCode, board);
                }
            }
        }
    }


    @MessageMapping("/game/{roomCode}/placeBomb")
    public void handlePlaceBomb(
            @DestinationVariable String roomCode,
            @Payload PlayerActionRequest request) {

        GameBoard board = loadGameStateFromRedis(roomCode);
        if (board == null) {
            board = roomService.getGameBoard(roomCode);
        }

        if (board != null) {
            Player player = board.getPlayerById(request.getPlayerId());
            if (player != null && player.canPlaceBomb()) {
                if (!board.getGameMap().getCell(player.getX(), player.getY()).hasBomb()) {
                    Bomb bomb = new Bomb(player.getX(), player.getY(), player);
                    board.addBomb(bomb);
                    board.getGameMap().placeBomb(player.getX(), player.getY(), bomb);

                    saveGameStateToRedis(roomCode, board);
                    sendGameUpdate(roomCode, board);

                    // Programar la explosión
                    scheduleBombExplosion(roomCode, bomb.getId());
                }
            }
        }
    }

    private void scheduleBombExplosion(String roomCode, String bombId) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handleBombExplosion(roomCode, bombId);
            }
        }, 2000); // 2 segundos
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
                saveGameStateToRedis(roomCode, board);
                broadcastGameState(roomCode, board);
            }
        }
    }

    @MessageMapping("/room/{roomCode}/status")
    public void getGameStatus(@DestinationVariable String roomCode) {
        // Intentar cargar desde Redis primero
        GameBoard board = loadGameStateFromRedis(roomCode);
        if (board == null) {
            board = roomService.getGameBoard(roomCode);
        }

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

    @MessageMapping("/game/{roomCode}/init")
    public void initGame(
            @DestinationVariable String roomCode,
            @Payload Map<String, Object> request) {

        // Cargar estado desde Redis
        GameBoard board = loadGameStateFromRedis(roomCode);
        if (board == null) {
            board = roomService.getGameBoard(roomCode);
        }

        if (board != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GAME_UPDATE");
            response.put("state", Map.of(
                    "players", board.getPlayers().stream().map(p -> Map.of(
                            "id", p.getId(),
                            "name", p.getName(),
                            "x", p.getX(),
                            "y", p.getY(),
                            "lives", p.getLives(),
                            "bombCapacity", p.getBombCapacity(),
                            "bombRange", p.getBombRange()
                    )).collect(Collectors.toList()),
                    "map", Map.of(
                            "width", board.getGameMap().getWidth(),
                            "height", board.getGameMap().getHeight(),
                            "cells", board.getGameMap().getCellStates()
                    ),
                    "config", Map.of(
                            "duration", board.getConfig().getDuration(),
                            "lives", board.getConfig().getLives()
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
                    )).collect(Collectors.toList())
            ));

            messagingTemplate.convertAndSend("/topic/game/" + roomCode, response);
        }
    }

    // Métodos auxiliares para Redis
    private void saveRoomStateToRedis(String roomCode) {
        List<Player> players = roomService.getPlayersInRoom(roomCode);
        redisTemplate.opsForValue().set("room:players:" + roomCode, (Serializable) players);
    }

    private void loadRoomStateFromRedis(String roomCode) {
        List<Player> players = (List<Player>) redisTemplate.opsForValue().get("room:players:" + roomCode);
        if (players != null && !players.isEmpty()) {
            // Restaurar jugadores en la sala
            players.forEach(p -> roomService.addPlayerToRoom(roomCode, p));
        }
    }

    private void saveGameStateToRedis(String roomCode, GameBoard board) {
        redisTemplate.opsForValue().set("game:state:" + roomCode, board);
    }

    private GameBoard loadGameStateFromRedis(String roomCode) {
        return (GameBoard) redisTemplate.opsForValue().get("game:state:" + roomCode);
    }

    // Métodos auxiliares para enviar actualizaciones
    private void sendRoomUpdate(String roomCode) {
        List<Player> players = roomService.getPlayersInRoom(roomCode);
        String hostId = players.isEmpty() ? "" : players.get(0).getId();

        Map<String, Object> response = new HashMap<>();
        response.put("type", "PLAYER_UPDATE");
        response.put("players", players);
        response.put("host", hostId);

        messagingTemplate.convertAndSend("/topic/room/" + roomCode, response);
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
                        "bombCapacity", p.getBombCapacity(),
                        "bombRange", p.getBombRange()
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
                        .filter(b -> !b.shouldExplode())
                        .map(b -> Map.of(
                                "id", b.getId(),
                                "x", b.getX(),
                                "y", b.getY(),
                                "timer", (2000 - (System.currentTimeMillis() - b.getCreationTime())) / 1000.0,
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
        ));
    }

    private void handleBombExplosion(String roomCode, String bombId) {
        GameBoard board = loadGameStateFromRedis(roomCode);
        if (board == null) return;

        synchronized (board) {
            Bomb bomb = board.getBombs().stream()
                    .filter(b -> b.getId().equals(bombId))
                    .findFirst()
                    .orElse(null);

            if (bomb != null) {
                // 1. Eliminar la bomba
                board.getBombs().remove(bomb);
                board.getGameMap().removeBomb(bomb.getX(), bomb.getY());

                // 2. Restaurar capacidad del jugador
                Player owner = board.getPlayerById(bomb.getPlayerId());
                if (owner != null) {
                    owner.increaseBombCapacity();
                }

                // 3. Aplicar daño en el área
                applyExplosionDamage(board, bomb.getX(), bomb.getY(), bomb.getRange());

                // 4. Guardar y notificar
                saveGameStateToRedis(roomCode, board);
                sendGameUpdate(roomCode, board);
            }
        }
    }

    private void applyExplosionDamage(GameBoard board, int x, int y, int range) {
        // Implementa la lógica de daño en el área
        // Esto debería afectar jugadores y paredes destructibles
        // Por ejemplo:
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                if (dx == 0 || dy == 0) { // Solo en cruz
                    checkExplosionCell(board, x + dx, y + dy);
                }
            }
        }
    }

    private void checkExplosionCell(GameBoard board, int x, int y) {
        if (!board.getGameMap().isValidPosition(x, y)) return;

        Cell cell = board.getGameMap().getCell(x, y);

        // Dañar jugadores
        cell.getPlayers().forEach(player -> {
            player.increaseLives(-1);
            if (player.getLives() <= 0) {
                board.getPlayers().remove(player);
            }
        });

        // Destruir paredes
        if (cell.isWall() && cell.isDestructible()) {
            cell.setWall(false);
            // Posiblemente generar power-up aquí
        }
    }
}