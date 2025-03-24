package bomberman.arsw.Model;

import org.springframework.stereotype.Service;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Service
public class Game {
    private static final Logger logger = Logger.getLogger(Game.class.getName());
    private GameConfig config;
    private Map gameMap;
    private List<Player> players;

    public Game(GameConfig config) {
        this.config = config != null ? config : new GameConfig(); // Evitar null
        this.gameMap = new Map(15, 15);
        this.players = new ArrayList<>();
    }

    public void startGame() {
        System.out.println("Iniciando juego con configuración: " + config);
        assignRandomPositions();
        placeRandomBlocks(20);
        gameMap.printMap();
        showMap();
    }

    private void placeRandomBlocks(int blockCount) {
        Random random = new Random();
        for (int i = 0; i < blockCount; i++) {
            int x, y;
            do {
                x = random.nextInt(gameMap.getWidth());
                y = random.nextInt(gameMap.getHeight());
            } while (!gameMap.isCellEmpty(x, y)); // Asegura que no haya jugador o bloque

            gameMap.setBlock(x, y); // 🔥 Ahora el bloque se coloca en el mapa
        }
    }


    public GameConfig getConfig() {
        return config;
    }

    private void assignRandomPositions() {
        Random random = new Random();
        for (int i = 0; i < players.size(); i++) {
            int x, y;
            do {
                x = random.nextInt(gameMap.getWidth());
                y = random.nextInt(gameMap.getHeight());
            } while (!gameMap.isCellEmpty(x, y)); // Asegura que no haya otro jugador o bloque

            Player player = players.get(i);
            player.setPosition(x, y);
            gameMap.getCell(y, x).setPlayer(player); // 🔥 Ahora el jugador se coloca en la celda
        }
    }

    public void showMap() {
        JFrame frame = new JFrame("Bomberman Map");
        frame.add(new MapPanel(gameMap));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public void printMap() {
        logger.info("Llamando a printMap() desde Game.java...");
        if (gameMap == null) {
            logger.warning("gameMap es NULL, no se puede imprimir.");
            return;
        }
        gameMap.printMap();
    }

    public String getMapAsString() {
        StringBuilder mapString = new StringBuilder();
        for (int i = 0; i < gameMap.getHeight(); i++) {
            for (int j = 0; j < gameMap.getWidth(); j++) {
                Cell cell = gameMap.getCell(i, j);
                if (cell.hasPlayer()) {
                    mapString.append("P"); // Jugador
                } else if (cell.hasWall()) {
                    mapString.append("#"); // Pared
                } else {
                    mapString.append(".");
                }
            }
            mapString.append("\n");
        }
        System.out.println("Mapa generado:\n" + mapString.toString()); // 🔥 LOG para ver el mapa en consola
        return mapString.toString();
    }

}
