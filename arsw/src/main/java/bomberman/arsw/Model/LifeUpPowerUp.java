package bomberman.arsw.Model;

public class LifeUpPowerUp implements PowerUp {
    private final int lifeIncrease;
    private int x;
    private int y;
    private final PowerUpType type = PowerUpType.LIFE_UP;

    public LifeUpPowerUp(int lifeIncrease, int y, int x) {
        this.lifeIncrease = lifeIncrease;
        this.y = y;
        this.x = x;
    }

    public LifeUpPowerUp() {
        this(1); // Por defecto aumenta 1 vida
    }

    public LifeUpPowerUp(int lifeIncrease) {
        this.lifeIncrease = lifeIncrease;
    }



    @Override
    public void applyEffect(Player player) {
        player.increaseLives(lifeIncrease);
    }

    @Override
    public PowerUpType getType() {
        return type;
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
        return String.format(
                "{\"type\":\"%s\",\"lifeIncrease\":%d,\"x\":%d,\"y\":%d}",
                type.name(),
                lifeIncrease,
                x,
                y
        );
    }
}