package bomberman.arsw.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private final String code;
    private final List<Player> players = new CopyOnWriteArrayList<>();


    public Room(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(String playerName) {
        players.removeIf(p -> p.getName().equals(playerName));
    }


}