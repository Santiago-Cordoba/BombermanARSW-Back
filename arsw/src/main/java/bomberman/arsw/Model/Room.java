package bomberman.arsw.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String code;
    private List<Player> players;
    private GameBoard gameBoard;

    // Constructor para deserialización JSON
    @JsonCreator
    public Room(@JsonProperty("code") String code,
                @JsonProperty("players") List<Player> players,
                @JsonProperty("gameBoard") GameBoard gameBoard) {
        this.code = code;
        this.players = players != null ?
                new CopyOnWriteArrayList<>(players) : new CopyOnWriteArrayList<>();
        this.gameBoard = gameBoard;
    }

    // Constructor normal para uso programático
    public Room(String code) {
        this(code, new CopyOnWriteArrayList<>(), null);
    }

    @JsonProperty
    public String getCode() {
        return code;
    }

    @JsonProperty
    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = new CopyOnWriteArrayList<>(players);
    }

    public void addPlayer(Player player) {
        if (players.stream().anyMatch(p -> p.getId().equals(player.getId()) || p.getName().equals(player.getName()))) {
            throw new IllegalArgumentException("Ya existe un jugador con ese ID o nombre en la sala");
        }
        players.add(player);
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
    }

    public boolean containsPlayer(String playerId) {
        return players.stream().anyMatch(p -> p.getId().equals(playerId));
    }

    @JsonProperty
    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
    }

    public boolean allPlayersReady() {
        return !players.isEmpty() && players.stream().allMatch(Player::isReady);
    }

    // Método para verificar si el jugador es host
    public boolean isHost(String playerId) {
        return !players.isEmpty() && players.get(0).getId().equals(playerId);
    }

    @Override
    public String toString() {
        return "Room{" +
                "code='" + code + '\'' +
                ", players=" + players +
                ", gameBoard=" + (gameBoard != null) +
                '}';
    }
}