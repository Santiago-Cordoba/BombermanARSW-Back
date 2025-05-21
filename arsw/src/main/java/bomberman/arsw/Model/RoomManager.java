package bomberman.arsw.Model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;

@Component
public class RoomManager {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public synchronized Room getOrCreateRoom(String code) {
        return rooms.computeIfAbsent(code, Room::new);
    }

    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public synchronized void removeRoom(String code) {
        rooms.remove(code);
    }

    public boolean roomExists(String code) {
        return rooms.containsKey(code);
    }

    // Nuevo método añadido para sincronización con Redis
    public synchronized void updateRoomPlayers(String roomCode, List<Player> players) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.setPlayers(new CopyOnWriteArrayList<>(players));
        }
    }
}