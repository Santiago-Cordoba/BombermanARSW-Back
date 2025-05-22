package bomberman.arsw.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true) // <- Agregado aquí
public class GameBoard implements Serializable {
    private static final long serialVersionUID = 1L;
    private final GameConfig config;
    private final List<Player> players;
    private final GameMap gameMap;
    private List<Bomb> bombs;
    private List<PowerUp> powerUps;

    public GameBoard(GameConfig config, List<Player> players, GameMap gameMap) {
        this.config = config;
        this.players = new ArrayList<>(players);
        this.bombs = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.gameMap = gameMap;
        this.positionPlayers();

        System.out.println("PowerUps iniciales generados: " + this.powerUps.size());
    }

    @JsonCreator
    public GameBoard(@JsonProperty("config") GameConfig config,
                     @JsonProperty("players") List<Player> players,
                     @JsonProperty("gameMap") GameMap gameMap,
                     @JsonProperty("bombs") List<Bomb> bombs,
                     @JsonProperty("powerUps") List<PowerUp> powerUps) {
        this.config = config;
        this.players = players;
        this.gameMap = gameMap;
        this.bombs = bombs != null ? bombs : new ArrayList<>();
        this.powerUps = powerUps != null ? powerUps : new ArrayList<>();
    }

    @JsonProperty
    public GameConfig getConfig() { return config; }

    @JsonProperty
    public List<Player> getPlayers() { return players; }

    @JsonProperty
    public GameMap getGameMap() { return gameMap; }

    @JsonProperty
    public List<Bomb> getBombs() { return bombs; }

    public void setBombs(List<Bomb> bombs) { this.bombs = bombs; }

    @JsonProperty
    public List<PowerUp> getPowerUps() { return powerUps; }

    public void setPowerUps(List<PowerUp> powerUps) { this.powerUps = powerUps; }

    private void positionPlayers() {
        if (players.size() >= 1) players.get(0).setPosition(1, 1);
        if (players.size() >= 2) players.get(1).setPosition(gameMap.getWidth() - 2, 1);
        if (players.size() >= 3) players.get(2).setPosition(1, gameMap.getHeight() - 2);
        if (players.size() >= 4) players.get(3).setPosition(gameMap.getWidth() - 2, gameMap.getHeight() - 2);
    }



    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
    }

    public void removeBomb(Bomb bomb) {
        bombs.remove(bomb);
    }

    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    public boolean collectPowerUp(String playerId, int x, int y) {
        Player player = getPlayerById(playerId);
        if (player == null) return false;

        Cell cell = gameMap.getCell(x, y);
        if (cell == null || !cell.hasCollectiblePowerUp()) return false;

        return cell.collectPowerUp(player);
    }

    public Player getPlayerById(String playerId) {
        return players.stream().filter(p -> p.getId().equals(playerId)).findFirst().orElse(null);
    }

    public boolean isValidMove(int x, int y) {
        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight()) return false;
        Cell cell = gameMap.getCell(x, y);
        return !(cell.isWall() || cell.hasBomb() || cell.hasPlayer());
    }

    public synchronized boolean movePlayer(Player player, int newX, int newY) {
        if (!isValidMove(newX, newY)) return false;

        gameMap.removePlayer(player.getX(), player.getY(), player);

        Cell newCell = gameMap.getCell(newX, newY);
        if (newCell.hasPowerUp()) {
            PowerUp powerUp = newCell.getPowerUp();
            powerUp.applyEffect(player);
            newCell.removePowerUp();
            powerUps.remove(powerUp);
        }

        player.setPosition(newX, newY);
        gameMap.placePlayer(newX, newY, player);

        return true;
    }

    public void placeBomb(int x, int y, Player owner) {
        if (owner == null || !owner.canPlaceBomb() || !gameMap.isValidPosition(x, y)) return;

        Bomb bomb = new Bomb(x, y, owner);
        bombs.add(bomb);
        gameMap.placeBomb(x, y, bomb);
        owner.decreaseBombCapacity();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.schedule(() -> {
            try {
                explodeBomb(bomb);
            } catch (Exception e) {
                System.err.println("Error al explotar bomba: " + e.getMessage());
            } finally {
                scheduler.shutdown();
            }
        }, 2, TimeUnit.SECONDS);
    }

    private void explodeBomb(Bomb bomb) {
        if (!bombs.contains(bomb)) return;

        bombs.remove(bomb);
        gameMap.removeBomb(bomb.getX(), bomb.getY());
        bomb.getOwner().increaseBombCapacity();

        List<Cell> affectedCells = new ArrayList<>();
        affectedCells.add(gameMap.getCell(bomb.getX(), bomb.getY()));

        explodeDirection(bomb.getX(), bomb.getY(), 0, 1, bomb.getRange(), affectedCells);
        explodeDirection(bomb.getX(), bomb.getY(), 0, -1, bomb.getRange(), affectedCells);
        explodeDirection(bomb.getX(), bomb.getY(), 1, 0, bomb.getRange(), affectedCells);
        explodeDirection(bomb.getX(), bomb.getY(), -1, 0, bomb.getRange(), affectedCells);

        processAffectedCells(affectedCells, bomb.getOwner());
    }

    private void explodeDirection(int startX, int startY, int dx, int dy, int range, List<Cell> affectedCells) {
        for (int i = 1; i <= range; i++) {
            int x = startX + dx * i;
            int y = startY + dy * i;

            if (!gameMap.isValidPosition(x, y)) break;

            Cell cell = gameMap.getCell(x, y);
            affectedCells.add(cell);

            if (cell.isWall()) break;
        }
    }

    private void processAffectedCells(List<Cell> affectedCells, Player owner) {
        for (Cell cell : affectedCells) {
            if (cell.isWall() && cell.isDestructible()) {
                gameMap.destroyWall(cell.getX(), cell.getY());
            }

            for (Player player : new ArrayList<>(cell.getPlayers())) {
                player.increaseLives(-1);
                if (player.getLives() <= 0) {
                    players.remove(player);
                    gameMap.removePlayer(player.getX(), player.getY(), player);
                }
            }

            if (cell.hasBomb()) {
                Bomb chainBomb = cell.getBomb();
                bombs.remove(chainBomb);
                explodeBomb(chainBomb);
            }

            if (cell.hasPowerUp()) {
                PowerUp powerUp = cell.getPowerUp();
                cell.removePowerUp();
                powerUps.remove(powerUp);
            }
        }
    }

    public Player checkWinner() {
        List<Player> alivePlayers = players.stream().filter(p -> p.getLives() > 0).collect(Collectors.toList());
        return (alivePlayers.size() == 1) ? alivePlayers.get(0) : null;
    }

    public boolean removePowerUp(PowerUp powerUp) {
        return powerUps.remove(powerUp);
    }

    public Optional<PowerUp> getPowerUpAt(int x, int y) {
        return powerUps.stream().filter(pu -> pu.getX() == x && pu.getY() == y).findFirst();
    }

    public void forceExplodeBombAt(int x, int y) {
        Bomb target = bombs.stream().filter(b -> b.getX() == x && b.getY() == y).findFirst().orElse(null);
        if (target != null) explodeBomb(target);
    }
}
