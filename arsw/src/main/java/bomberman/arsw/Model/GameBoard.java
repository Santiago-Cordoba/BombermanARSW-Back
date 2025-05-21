package bomberman.arsw.Model;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
        this.positionPlayers();
        this.spawnInitialPowerUps();

        System.out.println("PowerUps iniciales generados: " + this.powerUps.size());
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

    private void spawnInitialPowerUps() {
        Set<String> playerSpawnPositions = new HashSet<>();
        for (Player player : players) {
            playerSpawnPositions.add(player.getX() + "," + player.getY());
        }

        for (Player player : players) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    playerSpawnPositions.add((player.getX() + dx) + "," + (player.getY() + dy));
                }
            }
        }

        List<Cell> emptyCells = gameMap.getEmptyCells()
                .stream()
                .filter(cell -> {
                    String cellPos = cell.getX() + "," + cell.getY();
                    return !cell.isWall() &&
                            !cell.hasBomb() &&
                            !cell.hasPlayer() &&
                            !playerSpawnPositions.contains(cellPos);
                })
                .collect(Collectors.toList());

        Collections.shuffle(emptyCells);

        int powerUpsToSpawn = Math.min(5, emptyCells.size());

        for (int i = 0; i < powerUpsToSpawn; i++) {
            Cell cell = emptyCells.get(i);
            PowerUp powerUp;

            powerUp = new BombRangePowerUp(cell.getX(), cell.getY());


            cell.addPowerUp(powerUp);
            powerUps.add(powerUp);
        }
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

    public boolean collectPowerUp(String playerId, int x, int y) {
        Player player = getPlayerById(playerId);
        if (player == null) return false;

        Cell cell = gameMap.getCell(x, y);
        if (cell == null || !cell.hasCollectiblePowerUp()) return false;

        return cell.collectPowerUp(player);
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
        if (x < 0 || x >= gameMap.getWidth() || y < 0 || y >= gameMap.getHeight()) {
            return false;
        }

        Cell cell = gameMap.getCell(x, y);

        // No permitir si hay pared, bomba o jugadores ya en la celda
        if (cell.isWall() || cell.hasBomb() || cell.hasPlayer()) {
            return false;
        }

        return true;
    }

    public synchronized boolean movePlayer(Player player, int newX, int newY) {
        if (!isValidMove(newX, newY)) {
            return false;
        }

        gameMap.removePlayer(player.getX(), player.getY(), player);

        Cell newCell = gameMap.getCell(newX, newY);
        if (newCell.hasPowerUp()) {
            PowerUp powerUp = newCell.getPowerUp();
            powerUp.applyEffect(player);
            newCell.removePowerUp();
            powerUps.remove(powerUp);
        }

        // Actualizar posición del jugador
        player.setPosition(newX, newY);
        gameMap.placePlayer(newX, newY, player);

        return true;
    }

    public void placeBomb(int x, int y, Player owner) {
        if (owner == null || !owner.canPlaceBomb() || !gameMap.isValidPosition(x, y)) {
            return;
        }

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
        // Verificar si la bomba aún existe (puede haber sido removida por otra explosión)
        if (!bombs.contains(bomb)) {
            return;
        }

        // Remover la bomba
        bombs.remove(bomb);
        gameMap.removeBomb(bomb.getX(), bomb.getY());
        bomb.getOwner().increaseBombCapacity();

        // Calcular celdas afectadas
        List<Cell> affectedCells = new ArrayList<>();

        // Añadir celda central
        affectedCells.add(gameMap.getCell(bomb.getX(), bomb.getY()));

        // Explosión en 4 direcciones
        explodeDirection(bomb.getX(), bomb.getY(), 0, 1, bomb.getRange(), affectedCells);  // Derecha
        explodeDirection(bomb.getX(), bomb.getY(), 0, -1, bomb.getRange(), affectedCells); // Izquierda
        explodeDirection(bomb.getX(), bomb.getY(), 1, 0, bomb.getRange(), affectedCells);  // Abajo
        explodeDirection(bomb.getX(), bomb.getY(), -1, 0, bomb.getRange(), affectedCells); // Arriba

        // Procesar efectos
        processAffectedCells(affectedCells, bomb.getOwner());
    }

    private void explodeDirection(int startX, int startY, int dx, int dy, int range, List<Cell> affectedCells) {
        for (int i = 1; i <= range; i++) {
            int x = startX + (dx * i);
            int y = startY + (dy * i);

            if (!gameMap.isValidPosition(x, y)) {
                break;
            }

            Cell cell = gameMap.getCell(x, y);
            affectedCells.add(cell);

            if (cell.isWall() && !cell.isDestructible()) {
                break;
            }

            if (cell.isWall() && cell.isDestructible()) {
                break;
            }
        }
    }

    private void processAffectedCells(List<Cell> affectedCells, Player owner) {
        boolean playersAffected = false;
        for (Cell cell : affectedCells) {
            // Destruir paredes destructibles
            if (cell.isWall() && cell.isDestructible()) {
                gameMap.destroyWall(cell.getX(), cell.getY());

            }

            // Dañar jugadores en la celda
            for (Player player : new ArrayList<>(cell.getPlayers())) {
                player.increaseLives(-1); // Reducir una vida
                if (player.getLives() <= 0) {
                    // Jugador muerto, removerlo del juego
                    players.remove(player);
                    gameMap.removePlayer(player.getX(), player.getY(), player);
                    }
                }


            // Eliminar bombas en la celda (explosión en cadena)
            if (cell.hasBomb()) {
                Bomb chainBomb = cell.getBomb();
                bombs.remove(chainBomb);
                explodeBomb(chainBomb); // Explosión en cadena
            }

            // Eliminar power-ups en la celda
            if (cell.hasPowerUp()) {
                PowerUp powerUp = cell.getPowerUp();
                cell.removePowerUp();
                powerUps.remove(powerUp);

            }
        }
    }

    public Player checkWinner() {
        // Si solo queda un jugador con vidas, es el ganador
        List<Player> alivePlayers = players.stream()
                .filter(p -> p.getLives() > 0)
                .collect(Collectors.toList());

        if (alivePlayers.size() == 1) {
            return alivePlayers.get(0);
        }

        // Si todos los jugadores han muerto, no hay ganador (empate)
        if (alivePlayers.isEmpty() && !players.isEmpty()) {
            return null; // Representa empate
        }

        return null; // Aún no hay ganador
    }

    public boolean removePowerUp(PowerUp powerUp) {
        return powerUps.remove(powerUp);
    }

    public Optional<PowerUp> getPowerUpAt(int x, int y) {
        return powerUps.stream()
                .filter(pu -> pu.getX() == x && pu.getY() == y)
                .findFirst();
    }

    public void forceExplodeBombAt(int x, int y) {
        Bomb target = bombs.stream()
                .filter(b -> b.getX() == x && b.getY() == y)
                .findFirst()
                .orElse(null);

        if (target != null) {
            explodeBomb(target);
        }
    }

}