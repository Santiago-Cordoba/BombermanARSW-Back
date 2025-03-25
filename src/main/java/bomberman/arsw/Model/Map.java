package bomberman.arsw.Model;

import java.util.logging.Logger;

public class Map {
    private int width;
    private int height;
    private Cell[][] cells;
    private static final Logger logger = Logger.getLogger(Map.class.getName());

    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                cells[i][j] = new Cell(j, i);
            }
        }
    }

    public boolean isCellEmpty(int x, int y) {
        return cells[y][x].isEmpty();  // Se asume que isEmpty() está en Cell.java
    }

    public void setBlock(int x, int y) {
        cells[y][x].setBlock(true);
        logger.info("Bloque colocado en (" + x + ", " + y + ")");
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell getCell(int x, int y) {
        return cells[y][x];
    }

    public void printMap() {
        logger.info("Imprimiendo el mapa...");
        for (int i = 0; i < height; i++) {
            StringBuilder row = new StringBuilder();
            for (int j = 0; j < width; j++) {
                if (cells[i][j].hasPlayer()) {
                    row.append("P ");
                } else if (cells[i][j].hasWall()) {
                    row.append("# ");
                } else {
                    row.append(". ");
                }
            }
            logger.info(row.toString());
        }
    }

    // ✅ Corrección: Ahora setCell actualiza correctamente la celda
    public void setCell(int x, int y, char type) {
        if (type == 'P') {
            cells[y][x].setPlayer(true);
        } else if (type == '#') {
            cells[y][x].setBlock(true);
        }
    }
}
