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
    private Random random = new Random();

    public Game(GameConfig config) {
        this.config = config != null ? config : new GameConfig(); // Evitar null
        this.gameMap = new Map(config.getMapaAlto(), config.getMapaAncho()); // ✅ Usa la config del usuario
        this.players = new ArrayList<>();

        assignRandomPositions();
        placeRandomBlocks(config.getBloques());

        System.out.println("Mapa generado:");
        gameMap.printMap();
    }

    public void startGame() {
        System.out.println("Iniciando juego con configuración: " + config);
        initializePlayers();  // ✅ Asegura que hay jugadores antes de posicionarlos
        assignRandomPositions();
        placeRandomBlocks(config.getBloques());
        gameMap.printMap();
        showMap();
    }

    private void initializePlayers() {
        for (int i = 0; i < config.getJugadores(); i++) {
            int x = random.nextInt(gameMap.getWidth());
            int y = random.nextInt(gameMap.getHeight());
            Player player = new Player(players.size(), x, y);
            player.setLifes(config.getVidas());  // ✅ Configurar vidas según el usuario
            players.add(player);
        }
    }

    private void assignRandomPositions() {
        int numPlayers = config.getJugadores();
        for (int i = 0; i < numPlayers; i++) {
            int x, y;
            do {
                x = random.nextInt(gameMap.getWidth());
                y = random.nextInt(gameMap.getHeight());
            } while (!gameMap.isCellEmpty(x, y));

            Player newPlayer = new Player(i, x, y);
            players.add(newPlayer);
            gameMap.setCell(x, y, 'P');
        }
    }

    private void placeRandomBlocks(int numBlocks) {
        for (int i = 0; i < numBlocks; i++) {
            int x, y;
            do {
                x = random.nextInt(gameMap.getWidth());
                y = random.nextInt(gameMap.getHeight());
            } while (!gameMap.isCellEmpty(x, y));

            gameMap.setCell(x, y, '#');
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
                    mapString.append("P");
                    System.out.print("P"); // 👈 Imprime en consola
                } else if (cell.hasWall()) {
                    mapString.append("#");
                    System.out.print("#"); // 👈 Imprime en consola
                } else {
                    mapString.append(".");
                    System.out.print("."); // 👈 Imprime en consola
                }
            }
            mapString.append("\n");
            System.out.println(); // 👈 Nueva línea en consola
        }
        System.out.println("Mapa generado:\n" + mapString.toString());
        return mapString.toString();
    }


    public GameConfig getConfig() {
        return config;
    }
}
