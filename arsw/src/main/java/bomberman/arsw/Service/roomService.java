package bomberman.arsw.Service;

import bomberman.arsw.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class roomService {
    private final RoomManager roomManager;
    private final RedisTemplate<String, Object> redisTemplate;

    // Tiempo de vida para las entradas en Redis (24 horas)
    private static final long ROOM_TTL = 24 * 60 * 60;

    @Autowired
    public roomService(RoomManager roomManager, RedisTemplate<String, Object> redisTemplate) {
        this.roomManager = roomManager;
        this.redisTemplate = redisTemplate;
    }

    public Room createOrGetRoom(String roomCode) {
        // Primero intenta cargar desde Redis
        Room room = loadRoomFromRedis(roomCode);
        if (room != null) {
            roomManager.addRoom(roomCode, room);
            return room;
        }

        // Si no existe en Redis, crea una nueva
        Room newRoom = roomManager.getOrCreateRoom(roomCode);
        saveRoomToRedis(roomCode, newRoom);
        return newRoom;
    }

    public Optional<Room> getRoom(String roomCode) {
        // Primero verifica en memoria
        Optional<Room> roomOpt = Optional.ofNullable(roomManager.getRoom(roomCode));
        if (roomOpt.isPresent()) {
            return roomOpt;
        }

        // Si no está en memoria, busca en Redis
        Room redisRoom = loadRoomFromRedis(roomCode);
        if (redisRoom != null) {
            roomManager.addRoom(roomCode, redisRoom);
            return Optional.of(redisRoom);
        }

        return Optional.empty();
    }

    public void createGameBoard(String roomCode, GameConfig config, List<Player> players) {
        // Crear el mapa primero
        GameMap gameMap = GameMap.createDefaultMap(players.size());

        // Luego crear el tablero con el mapa
        GameBoard board = new GameBoard(config, players, gameMap);

        // Guardar el tablero en Redis
        saveGameBoardToRedis(roomCode, board);

        // Posicionar jugadores en el mapa
        positionPlayers(board, players);

        // Actualizar la sala en Redis con el nuevo tablero
        Optional<Room> roomOpt = getRoom(roomCode);
        roomOpt.ifPresent(room -> {
            room.setGameBoard(board);
            saveRoomToRedis(roomCode, room);
        });
    }

    private void positionPlayers(GameBoard board, List<Player> players) {
        GameMap map = board.getGameMap();
        int width = map.getWidth();
        int height = map.getHeight();

        int[][] startPositions = {
                {1, 1},
                {width-2, height-2},
                {width-2, 1},
                {1, height-2}
        };

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int[] pos = startPositions[i];
            p.setPosition(pos[0], pos[1]);
            map.placePlayer(pos[0], pos[1], p);
        }
    }

    public GameBoard getGameBoard(String roomCode) {
        // Primero intenta obtener de Redis
        GameBoard board = loadGameBoardFromRedis(roomCode);
        if (board != null) {
            return board;
        }

        // Si no está en Redis, busca en el roomManager
        Optional<Room> roomOpt = getRoom(roomCode);
        return roomOpt.map(Room::getGameBoard).orElse(null);
    }

    public boolean addPlayerToRoom(String roomCode, Player player) {
        Room room = createOrGetRoom(roomCode);
        room.addPlayer(player);
        saveRoomToRedis(roomCode, room);
        return true;
    }

    public boolean removePlayerFromRoom(String roomCode, String playerId) {
        Optional<Room> roomOpt = getRoom(roomCode);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.removePlayer(playerId);

            if (room.getPlayers().isEmpty()) {
                roomManager.removeRoom(roomCode);
                // También eliminar de Redis si la sala está vacía
                redisTemplate.delete("room:" + roomCode);
                redisTemplate.delete("gameboard:" + roomCode);
            } else {
                // Si aún hay jugadores, actualizar Redis
                saveRoomToRedis(roomCode, room);
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
                saveRoomToRedis(roomCode, room);
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
        return getRoom(roomCode)
                .map(Room::getPlayers)
                .orElse(List.of());
    }

    // Métodos para interactuar con Redis

    public void saveRoomToRedis(String roomCode, Room room) {
        redisTemplate.opsForValue().set("room:" + roomCode, room, ROOM_TTL, TimeUnit.SECONDS);
    }

    public Room loadRoomFromRedis(String roomCode) {
        return (Room) redisTemplate.opsForValue().get("room:" + roomCode);
    }

    public void saveGameBoardToRedis(String roomCode, GameBoard board) {
        redisTemplate.opsForValue().set("gameboard:" + roomCode, board, ROOM_TTL, TimeUnit.SECONDS);
    }

    public GameBoard loadGameBoardFromRedis(String roomCode) {
        return (GameBoard) redisTemplate.opsForValue().get("gameboard:" + roomCode);
    }

    // Método para restaurar una sala completa (jugadores + tablero)
    public void restoreRoom(String roomCode, List<Player> players, GameBoard board) {
        Room room = new Room(roomCode);
        players.forEach(room::addPlayer);
        room.setGameBoard(board);
        roomManager.addRoom(roomCode, room);

        // Guardar en Redis
        saveRoomToRedis(roomCode, room);
        if (board != null) {
            saveGameBoardToRedis(roomCode, board);
        }
    }
}