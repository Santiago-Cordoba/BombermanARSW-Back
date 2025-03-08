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
}
