package bomberman.arsw.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class GameBoard {
    private final GameConfig config;
    private final List<Player> players;
    private final GameMap gameMap;
    private final List<Bomb> bombs;
    private final List<PowerUp> powerUps;

    public GameBoard(GameConfig config, List<Player> players, GameMap gameMap) {
        this.config = config;
        this.players = new ArrayList<>(players);
        this.bombs = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.gameMap = gameMap;
        positionPlayers();
        initializePowerUps();
    }

    private void positionPlayers() {
        if (players.size() >= 1) {
            players.get(0).setPosition(1, 1); // Esquina superior izquierda
        }
        if (players.size() >= 2) {
            players.get(1).setPosition(gameMap.getWidth() - 2, 1); // Esquina superior derecha
        }
        if (players.size() >= 3) {
            players.get(2).setPosition(1, gameMap.getHeight() - 2); // Esquina inferior izquierda
        }
        if (players.size() >= 4) {
            players.get(3).setPosition(gameMap.getWidth() - 2, gameMap.getHeight() - 2); // Esquina inferior derecha
        }
    }

    private void initializePowerUps() {
        // Colocar power-ups en posiciones estratégicas
        powerUps.add(new LifeUpPowerUp(1,3, 3));

    }

    public String getGameStateJson() {
        try {
            return String.format(
                    "{\"config\":%s,\"players\":[%s],\"map\":%s,\"bombs\":[%s],\"powerUps\":[%s]}",
                    config.toJsonString(),
                    players.stream().map(Player::toJsonString).collect(Collectors.joining(",")),
                    gameMap.toJsonString(),
                    bombs.stream().map(Bomb::toJsonString).collect(Collectors.joining(",")),
                    powerUps.stream().map(PowerUp::toJsonString).collect(Collectors.joining(","))
            );
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Failed to generate game state\"}";
        }
    }

    // Métodos para gestionar bombas
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
    }

    public void removeBomb(Bomb bomb) {
        bombs.remove(bomb);
    }

    public List<Bomb> getBombs() {
        return new ArrayList<>(bombs);
    }

    // Métodos para gestionar power-ups
    public void addPowerUp(PowerUp powerUp) {
        powerUps.add(powerUp);
    }

    public void removePowerUp(PowerUp powerUp) {
        powerUps.remove(powerUp);
    }

    public List<PowerUp> getPowerUps() {
        return new ArrayList<>(powerUps);
    }

    // Getters
    public GameConfig getConfig() {
        return config;
    }

    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public Player getPlayerById(String playerId) {
        return players.stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    public boolean isValidMove(int x, int y) {
        // Verificar límites del mapa
        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight()) {
            return false;
        }

        // Verificar si hay pared en la celda
        if (gameMap.getCell(x, y).isWall()) {
            return false;
        }

        // Verificar si hay bomba en la celda
        if (bombs.stream().anyMatch(b -> b.getX() == x && b.getY() == y)) {
            return false;
        }

        return true;
    }

    public void placeBomb(int x, int y, Player owner) {
        if (owner.canPlaceBomb()) {
            Bomb bomb = new Bomb(x, y, owner);
            bombs.add(bomb);
            owner.decreaseBombCapacity();

            // Programar explosión (ejemplo simple)
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    explodeBomb(bomb);
                }
            }, 3000); // 3 segundos
        }
    }

    private void explodeBomb(Bomb bomb) {
        // Lógica de explosión
        bombs.remove(bomb);
        bomb.getOwner().increaseBombCapacity(); // Usamos getOwner() de la bomba


    }
}