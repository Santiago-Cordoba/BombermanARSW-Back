package bomberman.arsw.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bomb implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private final int x;
    private final int y;
    private int timer;
    private int range;
    private transient Player owner; // Transient para evitar serialización circular
    private String playerId; // Almacenará el ID del jugador para serialización
    @Getter
    private long creationTime; // Añade este campo
    private static final int EXPLOSION_TIME = 2000;


    @JsonCreator
    public Bomb(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y,
            @JsonProperty("timer") int timer,
            @JsonProperty("range") int range,
            @JsonProperty("playerId") String playerId,
            @JsonProperty("id") String id) {
        this.x = x;
        this.y = y;
        this.timer = timer;
        this.range = range;
        this.playerId = playerId;
        this.id = id;
    }

    // Constructor principal para uso en el juego
    public Bomb(int x, int y, Player owner) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.playerId = owner != null ? owner.getId() : null;
        this.timer = 2; // Tiempo predeterminado antes de explotar
        this.range = owner != null ? owner.getBombRange() : 1;
        this.id = "bomb-" + x + "-" + y + "-" + System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Posición de bomba inválida");
        }
    }

    @JsonIgnore
    public boolean shouldExplode() {
        return System.currentTimeMillis() - creationTime >= EXPLOSION_TIME;
    }

    // Métodos para manejar correctamente la serialización
    @JsonProperty("playerId")
    public String getPlayerId() {
        return owner != null ? owner.getId() : playerId;
    }

    @JsonIgnore
    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.playerId = owner != null ? owner.getId() : null;
    }

    // Getters y setters estándar
    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    // Método para serialización personalizada
    public String toJsonString() {
        return String.format(
                "{\"id\":\"%s\",\"x\":%d,\"y\":%d,\"timer\":%d,\"range\":%d,\"playerId\":\"%s\"}",
                id,
                x,
                y,
                timer,
                range,
                getPlayerId()
        );
    }

    @Override
    public String toString() {
        return "Bomb{" +
                "id='" + id + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", timer=" + timer +
                ", range=" + range +
                ", playerId='" + getPlayerId() + '\'' +
                '}';
    }

}