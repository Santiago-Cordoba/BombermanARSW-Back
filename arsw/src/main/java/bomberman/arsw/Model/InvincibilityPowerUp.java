package bomberman.arsw.Model;

import java.util.Timer;
import java.util.TimerTask;

public class InvincibilityPowerUp implements PowerUp {
    private final int durationSeconds = 15;
    private int x;
    private int y;
    private final PowerUpType type = PowerUpType.INVINCIBILITY;

    public InvincibilityPowerUp(int y, int x) {
        this.y = y;
        this.x = x;
    }

    public InvincibilityPowerUp() {
        this(0, 0);
    }

    @Override
    public void applyEffect(Player player) {
        player.setInvincible(true);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                player.setInvincible(false);
            }
        }, durationSeconds * 1000L);
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
                "{\"type\":\"%s\",\"duration\":%d,\"x\":%d,\"y\":%d}",
                type.name(), durationSeconds, x, y
        );
    }

}
