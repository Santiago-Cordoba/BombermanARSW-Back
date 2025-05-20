package bomberman.arsw.Model;

import java.util.Map;
import java.util.Random;

public class PowerUpFactory {

    private static final PowerUpType[] availableTypes = {
            PowerUpType.BOMB_UP,
            PowerUpType.FIRE_UP,
            PowerUpType.SPEED_UP,
            PowerUpType.LIFE_UP,
            PowerUpType.INVINCIBILITY,
            PowerUpType.FIRE_PLUS
    };

    private static final Map<PowerUpType, Double> probabilities = Map.of(
            PowerUpType.BOMB_UP, 0.15,
            PowerUpType.FIRE_UP, 0.15,
            PowerUpType.SPEED_UP, 0.15,
            PowerUpType.LIFE_UP, 0.50,
            PowerUpType.INVINCIBILITY, 0.25,
            PowerUpType.FIRE_PLUS, 0.25
    );

    private static final Random random = new Random();

    public static PowerUp generateRandomPowerUp(int x, int y) {
        PowerUpType selected = availableTypes[random.nextInt(availableTypes.length)];
        return createPowerUpByType(selected, x, y);
    }

    private static PowerUp createPowerUpByType(PowerUpType type, int x, int y) {
        switch (type) {
            case BOMB_UP:
                // return new BombCapacityPowerUp(x, y);
            case FIRE_UP:
               // return new FirePowerUp(x, y);
            case SPEED_UP:
               // return new SpeedPowerUp(x, y);
            case LIFE_UP:
                return new LifeUpPowerUp(1, y, x);
            case INVINCIBILITY:
                return new InvincibilityPowerUp(x, y);
            case FIRE_PLUS:
                return new ExtraFirePowerUp(x, y);
            default:
                throw new IllegalArgumentException("Tipo de power-up no reconocido: " + type);
        }
    }
}
