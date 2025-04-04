package bomberman.arsw.Model;

public class GameConfig {
    private int durationSeconds;
    private int lives;

    public GameConfig(int durationSeconds, int lives) {
        this.durationSeconds = durationSeconds;
        this.lives = lives;
    }

    // Getters
    public int getDurationSeconds() {
        return durationSeconds;
    }

    public int getLives() {
        return lives;
    }
}