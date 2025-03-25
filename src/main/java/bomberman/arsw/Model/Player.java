package bomberman.arsw.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class Player {
    private int xPosition;
    private int yPosition;
    private int lifes;
    private String name;
    private List<Bomb> nBombs;

    // Constructor corregido
    public Player(int id, int x, int y) {
        this.xPosition = x;
        this.yPosition = y;
        this.lifes = 3;  // Asignar vidas por defecto
        this.name = "Player" + id; // Nombre automático
    }

    public void setPosition(int x, int y) {
        this.xPosition = x;
        this.yPosition = y;
    }
}
