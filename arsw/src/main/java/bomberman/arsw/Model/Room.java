package bomberman.arsw.Model;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room implements Serializable {
    private final String code;
    private List<Player> players = new CopyOnWriteArrayList<>();

    public Room(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = new CopyOnWriteArrayList<>(players);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(String playerId) {
        players.removeIf(p -> p.getId().equals(playerId));
    }

    public boolean containsPlayer(String playerId) {
        return players.stream().anyMatch(p -> p.getId().equals(playerId));
    }
}