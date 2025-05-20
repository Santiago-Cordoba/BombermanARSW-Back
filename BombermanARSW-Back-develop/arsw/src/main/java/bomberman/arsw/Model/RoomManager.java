package bomberman.arsw.Model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component // o @Service
public class RoomManager {
    // Almacena todas las salas activas usando ConcurrentHashMap para seguridad en hilos
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    // Resto del código permanece igual...
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
}