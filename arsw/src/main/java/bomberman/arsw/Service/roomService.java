package bomberman.arsw.Service;

import bomberman.arsw.Model.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class roomService {
    private final RoomManager roomManager;

    public roomService(RoomManager roomManager) {
        this.roomManager = roomManager;
    }

    public Room createOrGetRoom(String roomCode) {
        return roomManager.getOrCreateRoom(roomCode);
    }

    public Optional<Room> getRoom(String roomCode) {
        return Optional.ofNullable(roomManager.getRoom(roomCode));
    }

    private Map<String, GameBoard> gameBoards = new HashMap<String, GameBoard>();

    public void createGameBoard(String roomCode, GameConfig config, List<Player> players) {
        GameBoard board = new GameBoard(config, players);
        gameBoards.put(roomCode, board);
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




}