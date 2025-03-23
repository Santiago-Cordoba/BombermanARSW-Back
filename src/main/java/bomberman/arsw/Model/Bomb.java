package bomberman.arsw.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bomb {
    private int xPosition;
    private int yPosition;
    private int radius;
    private int timeExplosion;
    private Player owner;
}
