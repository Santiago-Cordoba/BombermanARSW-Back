package bomberman.arsw.Model;

public class InvincibilityPowerUp implements PowerUp {

    private static final int DURATION = 15_000; // 15 segundos
    private int x;
    private int y;

    public InvincibilityPowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getDuration() {
        return DURATION;
    }

    @Override
    public void applyEffect(Player player) {
        player.activateInvincibility(DURATION);
    }

    @Override
    public PowerUpType getType() {
        return PowerUpType.INVINCIBILITY;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toJsonString() {
        return String.format("{\"type\":\"INVINCIBILITY\",\"x\":%d,\"y\":%d}", x, y);
    }
}
