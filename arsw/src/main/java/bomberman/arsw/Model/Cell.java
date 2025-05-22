package bomberman.arsw.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cell implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int x;
    private final int y;
    @JsonProperty("isWall")
    private boolean isWall;
    private Bomb bomb;
    private PowerUp powerUp;
    private List<Player> players;
    private boolean destructible;

    @JsonCreator
    public Cell(@JsonProperty("x") int x,
                @JsonProperty("y") int y,
                @JsonProperty("isWall") boolean isWall,
                @JsonProperty("bomb") Bomb bomb,
                @JsonProperty("powerUp") PowerUp powerUp,
                @JsonProperty("players") List<Player> players,
                @JsonProperty("destructible") boolean destructible) {
        this.x = x;
        this.y = y;
        this.isWall = isWall;
        this.bomb = bomb;
        this.powerUp = powerUp;
        this.players = players != null ? new ArrayList<>(players) : new ArrayList<>();
        this.destructible = destructible;
    }

    // Constructor simplificado para uso normal
    public Cell(int x, int y) {
        this(x, y, false, null, null, new ArrayList<>(), false);
    }

    // Métodos para verificar el estado de la celda
    @JsonIgnore
    public boolean hasBomb() {
        return bomb != null;
    }

    @JsonIgnore
    public boolean hasPowerUp() {
        return powerUp != null;
    }

    @JsonIgnore
    public boolean hasPlayer() {
        return !players.isEmpty();
    }


    public boolean isWall() {
        return isWall;
    }

    @JsonIgnore
    public boolean isEmpty() {
        return !isWall && !hasBomb() && !hasPlayer();
    }

    // Métodos para manipular la celda
    public void setWall(boolean isWall) {
        this.isWall = isWall;
        if (isWall) {
            this.bomb = null;
            this.powerUp = null;
            this.players.clear();
        }
    }

    public void setBomb(Bomb bomb) {
        if (!isWall) {
            this.bomb = bomb;
        }
    }

    public void addPlayer(Player player) {
        if (!isWall && !hasBomb()) {
            players.add(player);
        }
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void setPowerUp(PowerUp powerUp) {
        if (!isWall) {
            this.powerUp = powerUp;
        }
    }

    @JsonIgnore
    public boolean hasCollectiblePowerUp() {
        return powerUp != null && !isWall && !hasBomb();
    }

    // Getters con @JsonProperty
    @JsonProperty
    public int getX() {
        return x;
    }

    @JsonProperty
    public int getY() {
        return y;
    }

    @JsonProperty
    public Bomb getBomb() {
        return bomb;
    }

    @JsonProperty
    public PowerUp getPowerUp() {
        return powerUp;
    }

    @JsonProperty
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    @JsonProperty
    public boolean isDestructible() {
        return destructible;
    }

    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }

    @JsonIgnore
    public void removePowerUp() {
        this.powerUp = null;
    }

    @JsonIgnore
    public boolean collectPowerUp(Player player) {
        if (powerUp != null && player != null) {
            powerUp.applyEffect(player);
            powerUp = null;
            return true;
        }
        return false;
    }

    // Método para serialización personalizada
    @JsonIgnore
    public String toJsonString() {
        return String.format(
                "{\"x\":%d,\"y\":%d,\"isWall\":%b,\"hasBomb\":%b,\"hasPowerUp\":%b,\"players\":%s,\"destructible\":%b}",
                x, y, isWall, hasBomb(), hasPowerUp(),
                players.stream().map(Player::getId).collect(Collectors.joining("\",\"", "[\"", "\"]")),
                destructible
        );
    }

    @Override
    public String toString() {
        return "Cell{" +
                "x=" + x +
                ", y=" + y +
                ", isWall=" + isWall +
                ", bomb=" + (bomb != null) +
                ", powerUp=" + (powerUp != null) +
                ", players=" + players.size() +
                ", destructible=" + destructible +
                '}';
    }
}