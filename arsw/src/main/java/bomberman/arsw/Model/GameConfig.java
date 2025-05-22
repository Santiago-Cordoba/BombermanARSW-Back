package bomberman.arsw.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;

public class GameConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int duration;
    private final int lives;

    @JsonCreator
    public GameConfig(@JsonProperty("duration") int duration,
                      @JsonProperty("lives") int lives) {
        this.duration = duration;
        this.lives = lives;
    }

    @JsonProperty
    public int getDuration() { return duration; }

    @JsonProperty
    public int getLives() { return lives; }


}