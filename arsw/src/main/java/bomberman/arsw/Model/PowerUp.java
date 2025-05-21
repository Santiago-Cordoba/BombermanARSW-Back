package bomberman.arsw.Model;

public abstract class PowerUp {
    private final PowerUpType type;
    private int x;
    private int y;

    public PowerUp(PowerUpType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public abstract void applyEffect(Player player);
    public abstract String toJsonString();

    public PowerUpType getType() {
        return type;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}