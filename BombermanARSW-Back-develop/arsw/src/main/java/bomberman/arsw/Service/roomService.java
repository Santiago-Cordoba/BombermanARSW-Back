package bomberman.arsw.Service;

import bomberman.arsw.Model.*;
import org.springframework.stereotype.Service;

import java.util.*;

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
        // Crear el mapa primero
        GameMap gameMap = GameMap.createDefaultMap(players.size());

        // Luego crear el tablero con el mapa
        GameBoard board = new GameBoard(config, players, gameMap);

        // Guardar el tablero en el mapa
        gameBoards.put(roomCode, board);

        // Posicionar jugadores en el mapa
        positionPlayers(board, players);
    }

    private void positionPlayers(GameBoard board, List<Player> players) {
        GameMap map = board.getGameMap();
        int width = map.getWidth();
        int height = map.getHeight();

        // Posiciones iniciales según número de jugadores
        int[][] startPositions = {
                {1, 1},
                {width-2, height-2},// Jugador 1: esquina superior izquierda
                {width-2, 1},           // Jugador 2: esquina superior derecha
                {1, height-2},          // Jugador 3: esquina inferior izquierda
                     // Jugador 4: esquina inferior derecha
        };

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            int[] pos = startPositions[i];
            p.setPosition(pos[0], pos[1]);
            map.placePlayer(pos[0], pos[1], p);
        }
    }

    private void placeInitialPlayers(GameBoard board, List<Player> players) {
        GameMap map = board.getGameMap();

        // Posiciones iniciales según número de jugadores
        int[][] startPositions = {
                {1, 1},         // Jugador 1: esquina superior izquierda
                {map.getWidth()-2, 1},  // Jugador 2: esquina superior derecha
                {1, map.getHeight()-2}, // Jugador 3: esquina inferior izquierda
                {map.getWidth()-2, map.getHeight()-2} // Jugador 4: esquina inferior derecha
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

    public List<String> getAllRoomCodes() {
        return new ArrayList<>(gameBoards.keySet());
    }

}