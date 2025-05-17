package bomberman.arsw.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cell {
    private final int x;
    private final int y;
    private boolean isWall;
    private Bomb bomb;
    private PowerUp powerUp;
    private List<Player> players;
    private boolean destructible;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.isWall = false;
        this.players = new ArrayList<>();
    }

    // Métodos para verificar el estado de la celda
    public boolean hasBomb() {
        return bomb != null;
    }

    public boolean hasPowerUp() {
        return powerUp != null;
    }

    public boolean hasPlayer() {
        return !players.isEmpty();
    }

    public boolean isWall() {
        return isWall;
    }

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

    public void addPowerUp(PowerUp powerUp) {
        if (!isWall) {
            this.powerUp = powerUp;
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

    public boolean hasCollectiblePowerUp() {
        return powerUp != null && !isWall && !hasBomb();
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"x\":").append(x);
        sb.append(", \"y\":").append(y);
        sb.append(", \"isWall\":").append(isWall);
        sb.append(", \"isDestructible\":").append(isDestructible());
        sb.append(", \"hasBomb\":").append(bomb != null);
        sb.append(", \"hasPowerUp\":").append(powerUp != null);
        if (powerUp != null) {
            sb.append(", \"powerUpType\": \"").append(powerUp.getType()).append("\"");
        }

        sb.append(", \"players\":").append(players.stream()
                .map(Player::getId)
                .collect(Collectors.joining("\",\"", "[\"", "\"]")));
        sb.append("}");
        return sb.toString();
    }

    // Getters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Bomb getBomb() {
        return bomb;
    }

    public PowerUp getPowerUp() {
        return powerUp;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public boolean isDestructible() {
        return destructible;
    }

    public void setDestructible(boolean destructible) {
        this.destructible = destructible;
    }

    public void removePowerUp() {
        this.powerUp = null;
    }

    public boolean collectPowerUp(Player player) {
        if (powerUp != null && player != null) {
            powerUp.applyEffect(player);
            powerUp = null;
            return true;
        }
        return false;
    }
}
