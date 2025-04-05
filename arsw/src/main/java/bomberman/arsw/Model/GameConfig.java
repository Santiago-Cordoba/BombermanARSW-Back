package bomberman.arsw.Model;

public class GameConfig {
    private int duration;
    private int lives;

    public GameConfig(int durationSeconds, int lives) {
        this.duration = durationSeconds;
        this.lives = lives;
    }

    // Getters
    public int getDuration() {
        return duration;
    }

    public int getLives() {
        return lives;
    }

    public String toJsonString() {
        return String.format(
                "{\"duration\":%d,\"initialLives\":%d}",
                duration,
                lives
        );
    }
}