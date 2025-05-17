package bomberman.arsw.Model;

public class SpeedPowerUp implements PowerUp {
    private int x;
    private int y;
    private final PowerUpType type = PowerUpType.SPEED;

    public SpeedPowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void applyEffect(Player player) {
        player.setSpeed(player.getSpeed() + 1);
    }

    @Override
    public PowerUpType getType() {
        return type;
    }

    @Override
    public int getX() { return x; }

    @Override
    public int getY() { return y; }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toJsonString() {
        return String.format("{\"type\":\"%s\",\"x\":%d,\"y\":%d}", type.name(), x, y);
    }
}
