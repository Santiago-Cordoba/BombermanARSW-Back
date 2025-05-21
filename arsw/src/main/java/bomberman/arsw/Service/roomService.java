package bomberman.arsw.Service;

import bomberman.arsw.Model.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class roomService {

    private final RoomManager roomManager;
    private final RedisTemplate<String, GameBoard> gameBoardRedisTemplate;
    private final RedisTemplate<String, Room> roomRedisTemplate;
    private final Map<String, GameBoard> activeBoards = new HashMap<>();

    // Tiempo de expiración para los datos en Redis (1 día)
    private static final long REDIS_EXPIRATION_TIME = 24;
    private static final TimeUnit REDIS_EXPIRATION_UNIT = TimeUnit.HOURS;

    public roomService(RoomManager roomManager,
                       RedisTemplate<String, GameBoard> gameBoardRedisTemplate,
                       RedisTemplate<String, Room> roomRedisTemplate) {
        this.roomManager = roomManager;
        this.gameBoardRedisTemplate = gameBoardRedisTemplate;
        this.roomRedisTemplate = roomRedisTemplate;
    }

    public Room createOrGetRoom(String roomCode) {
        // Primero intentar cargar desde Redis
        Room cachedRoom = loadRoomState(roomCode);
        if (cachedRoom != null) {
            roomManager.getOrCreateRoom(roomCode).setPlayers(cachedRoom.getPlayers());
        }
        return roomManager.getOrCreateRoom(roomCode);
    }

    public Optional<Room> getRoom(String roomCode) {
        // Primero intentar cargar desde Redis
        Room cachedRoom = loadRoomState(roomCode);
        if (cachedRoom != null) {
            return Optional.of(cachedRoom);
        }
        return Optional.ofNullable(roomManager.getRoom(roomCode));
    }

    public void createGameBoard(String roomCode, GameConfig config, List<Player> players) {
        GameMap gameMap = GameMap.createDefaultMap(players.size());
        GameBoard board = new GameBoard(config, players, gameMap);
        activeBoards.put(roomCode, board);
        positionPlayers(board, players);
    }

    private void positionPlayers(GameBoard board, List<Player> players) {
        GameMap map = board.getGameMap();
        int width = map.getWidth();
        int height = map.getHeight();

        int[][] startPositions = {
                {1, 1},
                {width-2, height-2},
                {width-2, 1},
                {1, height-2},
        };

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int[] pos = startPositions[i];
            p.setPosition(pos[0], pos[1]);
            map.placePlayer(pos[0], pos[1], p);
        }
    }

    public GameBoard getGameBoard(String roomCode) {
        // Primero buscar en los tableros activos
        if (activeBoards.containsKey(roomCode)) {
            return activeBoards.get(roomCode);
        }

        // Si no está en memoria, cargar desde Redis
        GameBoard board = gameBoardRedisTemplate.opsForValue().get(roomCode);
        if (board != null) {
            activeBoards.put(roomCode, board);
        }
        return board;
    }

    public boolean addPlayerToRoom(String roomCode, Player player) {
        Room room = roomManager.getOrCreateRoom(roomCode);
        room.addPlayer(player);
        saveRoomState(roomCode, room);
        return true;
    }

    public boolean removePlayerFromRoom(String roomCode, String playerId) {
        Optional<Room> roomOpt = getRoom(roomCode);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.removePlayer(playerId);

            if (room.getPlayers().isEmpty()) {
                roomManager.removeRoom(roomCode);
                roomRedisTemplate.delete(roomCode);
                gameBoardRedisTemplate.delete(roomCode);
                activeBoards.remove(roomCode);
            } else {
                saveRoomState(roomCode, room);
            }
            return true;
        }
        return false;
    }

    public boolean togglePlayerReadyStatus(String roomCode, String playerId) {
        Optional<Room> roomOpt = getRoom(roomCode);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            Optional<Player> playerOpt = room.getPlayers().stream()
                    .filter(p -> p.getId().equals(playerId))
                    .findFirst();

            if (playerOpt.isPresent()) {
                Player player = playerOpt.get();
                player.setReady(!player.isReady());

                // Actualizar en Redis y en el roomManager
                saveRoomState(roomCode, room);
                roomManager.getOrCreateRoom(roomCode).setPlayers(room.getPlayers());

                return true;
            }
        }
        return false;
    }

    public boolean canStartGame(String roomCode) {
        return getRoom(roomCode)
                .map(room -> {
                    List<Player> players = room.getPlayers();
                    return players.size() >= 2 &&
                            players.stream().allMatch(Player::isReady);
                })
                .orElse(false);
    }

    public boolean isHost(String roomCode, String playerId) {
        return getRoom(roomCode)
                .map(room -> !room.getPlayers().isEmpty() &&
                        room.getPlayers().get(0).getId().equals(playerId))
                .orElse(false);
    }

    public List<Player> getPlayersInRoom(String roomCode) {
        // Primero intentar cargar desde Redis
        Room cachedRoom = loadRoomState(roomCode);
        if (cachedRoom != null) {
            return cachedRoom.getPlayers();
        }

        // Si no está en Redis, cargar desde el roomManager
        return getRoom(roomCode)
                .map(Room::getPlayers)
                .orElse(List.of());
    }

    public void saveGameState(String roomCode, GameBoard board) {
        try {
            gameBoardRedisTemplate.opsForValue().set(roomCode, board, REDIS_EXPIRATION_TIME, REDIS_EXPIRATION_UNIT);
            activeBoards.put(roomCode, board);
        } catch (Exception e) {
            System.err.println("Error al guardar el estado del juego en Redis: " + e.getMessage());
        }
    }

    public GameBoard loadGameState(String roomCode) {
        try {
            GameBoard board = gameBoardRedisTemplate.opsForValue().get(roomCode);
            if (board != null) {
                activeBoards.put(roomCode, board);
            }
            return board;
        } catch (Exception e) {
            System.err.println("Error al cargar el estado del juego desde Redis: " + e.getMessage());
            return null;
        }
    }

    public void setGameBoard(String roomCode, GameBoard board) {
        activeBoards.put(roomCode, board);
    }

    private void saveRoomState(String roomCode, Room room) {
        try {
            roomRedisTemplate.opsForValue().set(roomCode, room, REDIS_EXPIRATION_TIME, REDIS_EXPIRATION_UNIT);
        } catch (Exception e) {
            System.err.println("Error al guardar el estado de la sala en Redis: " + e.getMessage());
        }
    }

    private Room loadRoomState(String roomCode) {
        try {
            return roomRedisTemplate.opsForValue().get(roomCode);
        } catch (Exception e) {
            System.err.println("Error al cargar el estado de la sala desde Redis: " + e.getMessage());
            return null;
        }
    }
}