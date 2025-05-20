package bomberman.arsw.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameMap {
    private int width;
    private int height;
    private Cell[][] cells;
    Map<String, Object> cellState = new HashMap<>();

    public GameMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];

        // Inicializar todas las celdas
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = new Cell(x, y);
            }
        }
    }

    // Métodos para manipular el mapa
    public boolean placeWall(int x, int y) {
        if (isValidPosition(x, y) && !cells[y][x].hasPlayer() && !cells[y][x].hasBomb()) {
            cells[y][x].setWall(true);
            return true;
        }
        return false;
    }

    public boolean placeBomb(int x, int y, Bomb bomb) {
        if (isValidPosition(x, y) && !cells[y][x].isWall() && !cells[y][x].hasBomb()) {
            cells[y][x].setBomb(bomb);
            return true;
        }
        return false;
    }

    public boolean placePlayer(int x, int y, Player player) {
        if (isValidPosition(x, y) && !cells[y][x].isWall() && !cells[y][x].hasBomb()) {
            cells[y][x].addPlayer(player);
            return true;
        }
        return false;
    }

    public boolean placePowerUp(int x, int y, PowerUp powerUp) {
        if (isValidPosition(x, y) && !cells[y][x].isWall()) {
            cells[y][x].addPowerUp(powerUp);
            return true;
        }
        return false;
    }

    public void removePlayer(int x, int y, Player player) {
        if (isValidPosition(x, y)) {
            cells[y][x].removePlayer(player);
        }
    }

    public void removeBomb(int x, int y) {
        if (isValidPosition(x, y)) {
            cells[y][x].setBomb(null);
        }
    }

    public void removePowerUp(int x, int y) {
        if (isValidPosition(x, y)) {
            cells[y][x].setPowerUp(null);
        }
    }

    public void destroyWall(int x, int y) {
        if (isValidPosition(x, y) && cells[y][x].isWall()) {
            cells[y][x].setWall(false);
            // Al destruir una pared, podría dejar un power-up
            // Aquí podrías añadir lógica para generar power-ups aleatorios
        }
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int x, int y) {
        if (isValidPosition(x, y)) {
            return cells[y][x];
        }
        return null;
    }

    public static GameMap createDefaultMap(int playersCount) {
        int width = 15;
        int height = 13;
        GameMap gameMap = new GameMap(width, height);

        // 1. Paredes en los bordes y en patrón de rejilla
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = gameMap.getCell(x, y);

                // Establecer paredes en bordes o en posiciones pares
                boolean isWall = x == 0 || y == 0 || x == width - 1 || y == height - 1 ||
                        (x % 2 == 0 && y % 2 == 0);

                cell.setWall(isWall);

                // Todas las paredes son indestructibles
                if (isWall) {
                    cell.setDestructible(false);
                }

                // Inicializar sin power-ups
                cell.setPowerUp(null);
            }
        }

        // 2. Limpiar áreas de spawn de jugadores
        clearPlayerSpawnAreas(gameMap, width, height, playersCount);

        return gameMap;
    }

    private static boolean isNearCorner(int x, int y, int width, int height) {
        // Áreas seguras en las esquinas (radio de 2 celdas)
        return (x < 2 && y < 2) ||                     // Esquina superior izquierda
                (x > width - 3 && y < 2) ||             // Esquina superior derecha
                (x < 2 && y > height - 3) ||            // Esquina inferior izquierda
                (x > width - 3 && y > height - 3);      // Esquina inferior derecha
    }

    private static void clearPlayerSpawnAreas(GameMap gameMap, int width, int height, int playersCount) {
        // Limpiar áreas de aparición según el número de jugadores
        if (playersCount >= 1) {
            clearArea(gameMap, 1, 1); // Esquina superior izquierda
        }
        if (playersCount >= 2) {
            clearArea(gameMap, width - 2, 1); // Esquina superior derecha
        }
        if (playersCount >= 3) {
            clearArea(gameMap, 1, height - 2); // Esquina inferior izquierda
        }
        if (playersCount >= 4) {
            clearArea(gameMap, width - 2, height - 2); // Esquina inferior derecha
        }
    }

    private static void clearArea(GameMap gameMap, int centerX, int centerY) {
        // Limpiar un área de 3x3 alrededor del punto central
        for (int y = centerY - 1; y <= centerY + 1; y++) {
            for (int x = centerX - 1; x <= centerX + 1; x++) {
                if (gameMap.isValidPosition(x, y)) {
                    gameMap.getCell(x, y).setWall(false);
                }
            }
        }
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"width\":").append(width).append(",");
        sb.append("\"height\":").append(height).append(",");
        sb.append("\"cells\":[");

        for (int y = 0; y < height; y++) {
            sb.append("[");
            for (int x = 0; x < width; x++) {
                sb.append(cells[y][x].toJsonString());
                if (x < width - 1) sb.append(",");
            }
            sb.append("]");
            if (y < height - 1) sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }

    public List<List<Map<String, Object>>> getCellStates() {
        List<List<Map<String, Object>>> cellStates = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            List<Map<String, Object>> row = new ArrayList<>();
            for (int x = 0; x < width; x++) {
                Cell cell = cells[y][x];
                Map<String, Object> cellState = new HashMap<>();

                cellState.put("x", x);
                cellState.put("y", y);
                cellState.put("isWall", cell.isWall());
                cellState.put("isDestructible", cell.isDestructible()); // Todas las paredes son destructibles
                cellState.put("hasBomb", cell.hasBomb());
                cellState.put("hasPowerUp", cell.hasPowerUp());

                if (cell.hasPowerUp()) {
                    cellState.put("powerUpType", cell.getPowerUp().getType());
                }

                row.add(cellState);
            }
            cellStates.add(row);
        }

        return cellStates;
    }

    public List<Cell> getEmptyCells() {
        List<Cell> emptyCells = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Cell cell = cells[y][x];
                if (!cell.isWall() && !cell.hasBomb() && !cell.hasPlayer()) {
                    emptyCells.add(cell);
                }
            }
        }
        return emptyCells;
    }
}