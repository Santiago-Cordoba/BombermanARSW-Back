package bomberman.arsw.Model;

public class ExtraFirePowerUp implements PowerUp {
    private int x, y;

    public ExtraFirePowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void applyEffect(Player player) {
        player.increaseBombRange(); // Ya implementado en Player
    }

    @Override
    public PowerUpType getType() {
        return PowerUpType.FIRE_PLUS;
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
        return String.format("{\"type\":\"FIRE_PLUS\",\"x\":%d,\"y\":%d}", x, y);
    }
}
