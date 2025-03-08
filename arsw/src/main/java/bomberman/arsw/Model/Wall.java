package bomberman.arsw.Model;

import lombok.Data;

@Data
public class Wall {
    private boolean destructible;
    private int xPosition;
    private int yPosition;
}
