package bomberman.arsw.Service;

import bomberman.arsw.Model.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class roomService {

    private final RoomManager roomManager;
    private final RedisTemplate<String, GameBoard> redisTemplate;

    private Map<String, GameBoard> gameBoards = new HashMap<String, GameBoard>();
    private Map<String, GameBoard> activeBoards = new HashMap<>();

    public roomService(RoomManager roomManager, RedisTemplate<String, GameBoard> redisTemplate) {
        this.roomManager = roomManager;
        this.redisTemplate = redisTemplate;
    }

    public Room createOrGetRoom(String roomCode) {
        return roomManager.getOrCreateRoom(roomCode);
    }

    public Optional<Room> getRoom(String roomCode) {
        return Optional.ofNullable(roomManager.getRoom(roomCode));
    }

    public void createGameBoard(String roomCode, GameConfig config, List<Player> players) {
        GameMap gameMap = GameMap.createDefaultMap(players.size());
        GameBoard board = new GameBoard(config, players, gameMap);
        gameBoards.put(roomCode, board);
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
        return gameBoards.get(roomCode);
    }

    public boolean addPlayerToRoom(String roomCode, Player player) {
        Room room = roomManager.getOrCreateRoom(roomCode);
        room.addPlayer(player);
        return true;
    }

    public boolean removePlayerFromRoom(String roomCode, String playerId) {
        Optional<Room> roomOpt = getRoom(roomCode);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.removePlayer(playerId);
            if (room.getPlayers().isEmpty()) {
                roomManager.removeRoom(roomCode);
            }
            return true;
        }
        return false;
    }

    public boolean togglePlayerReadyStatus(String roomCode, String playerId) {
        return getRoom(roomCode)
                .map(room -> {
                    Optional<Player> playerOpt = room.getPlayers().stream()
                            .filter(p -> p.getId().equals(playerId))
                            .findFirst();
                    playerOpt.ifPresent(player -> player.setReady(!player.isReady()));
                    return playerOpt.isPresent();
                })
                .orElse(false);
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


    public void saveGameState(String roomCode, GameBoard board) {
        redisTemplate.opsForValue().set(roomCode, board);
    }

    public GameBoard loadGameState(String roomCode) {
        return redisTemplate.opsForValue().get(roomCode);
    }

    public void setGameBoard(String roomCode, GameBoard board) {
        activeBoards.put(roomCode, board);
    }
}
