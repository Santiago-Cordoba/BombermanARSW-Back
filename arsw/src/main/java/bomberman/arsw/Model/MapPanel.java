package bomberman.arsw.Model;

import javax.swing.*;
import java.awt.*;

public class MapPanel extends JPanel {
    private Map map;

    public MapPanel(Map map) {
        this.map = map;
        setPreferredSize(new Dimension(300, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int cellSize = 20;
        for (int i = 0; i < map.getHeight(); i++) {
            for (int j = 0; j < map.getWidth(); j++) {
                if (map.getCell(i, j).hasWall()) {
                    g.setColor(Color.BLACK);
                } else if (map.getCell(i, j).hasPlayer()) {
                    g.setColor(Color.RED);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                g.setColor(Color.GRAY);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }
    }
}
