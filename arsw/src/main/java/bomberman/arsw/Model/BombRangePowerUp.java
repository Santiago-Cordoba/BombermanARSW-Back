package bomberman.arsw.Model;

public class BombRangePowerUp extends PowerUp {

    public BombRangePowerUp() {
        super(PowerUpType.BOMB_RANGE_UP, 0, 0); // valores por defecto
    }

    public BombRangePowerUp(int x, int y) {
        super(PowerUpType.BOMB_RANGE_UP, x, y);
    }

    @Override
    public void applyEffect(Player player) {
        player.increaseBombRange(1);
    }

    @Override
    public String toJsonString() {
        return String.format(
                "{\"type\":\"%s\",\"x\":%d,\"y\":%d}",
                getType().name(),
                getX(),
                getY()
        );
    }
}
