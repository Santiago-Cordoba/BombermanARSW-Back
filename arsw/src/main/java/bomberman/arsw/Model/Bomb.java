package bomberman.arsw.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// Clase Bomb
public class Bomb {
    private final Player owner;
    private final int x;
    private final int y;
    private int timer;
    private int range;

    public Bomb( int x, int y, Player owner) {
        this.owner = owner;
        this.x = x;
        this.y = y;
        this.timer = 2; // Segundos antes de explotar
        this.range = owner != null ? owner.getBombRange() : 1; // Rango inicial de la explosión

        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Posición de bomba inválida");
        }
    }

    public String toJsonString() {
        return String.format(
                "{\"x\":%d,\"y\":%d,\"timer\":%d,\"ownerId\":\"%s\"}",
                x,
                y,
                timer,
                owner != null ? owner.getId() : "null"
        );
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Player getOwner() { return owner; }
    public int getTimer() { return timer; }

    public int getRange() {
        return range;
    }

    public String getId() {
        return "bomb-" + x + "-" + y + "-" + timer; // Ejemplo de ID único
    }

    public String getPlayerId() {
        return owner != null ? owner.getId() : "unknown";
    }
}






