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

    public boolean isEmpty() {
        return wall == null && bomb == null && player == null && powerUp == null;
    }

    public void setBlock(boolean b) {
        if (b) {
            this.wall = new Wall(); // Suponiendo que Wall tiene un constructor vacío
        } else {
            this.wall = null;
        }
    }

    public boolean hasPlayer() {
        return player != null;
    }

    public boolean hasWall() {
        return wall != null;
    }

    public void setPlayer(Player player) {
        this.player = player; // 🔥 Ahora la celda almacena al jugador
    }

    public Player getPlayer() {
        return this.player;
    }
}
