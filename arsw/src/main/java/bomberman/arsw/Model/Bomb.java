package bomberman.arsw.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bomb {
    private String id;
    private int xPosition;
    private int yPosition;
    private int radius;
    private int timeExplosion;
    private Player owner;
}
