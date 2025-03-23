package bomberman.arsw.Model;

public class Cell {
    private int xPosition;
    private int yPosition;
    private Wall wall;
    private Bomb bomb;
    private Player player;
    private PowerUp powerUp;

    public Cell(int xPosition, int yPosition) {
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        wall = null;
        bomb = null;
        player = null;
        powerUp = null;
    }
}
