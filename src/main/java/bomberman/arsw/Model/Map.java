package bomberman.arsw.Model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class Map {
    private int width;
    private int height;
    private Cell[][] cells;

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
}

