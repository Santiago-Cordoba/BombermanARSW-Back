package bomberman.arsw.Model;

import org.springframework.stereotype.Service;
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
        this.config = config != null ? config : new GameConfig();
        this.gameMap = new Map(config.getMapaAlto(), config.getMapaAncho());
        this.players = new ArrayList<>();

        assignRandomPositions();
        placeRandomBlocks(config.getBloques());
    }

    public void placeBomb() {
        if (players.isEmpty()) return;

        Player player = players.get(0);
        int x = player.getXPosition();
        int y = player.getYPosition();

        // Verificar que la celda no tenga ya una bomba
        if (!gameMap.getCell(x, y).hasBomb()) {
            gameMap.setCell(x, y, 'B');
            System.out.println("Bomba colocada en posición: (" + x + ", " + y + ")");
        }
    }

    // ✅ Mueve al jugador en la dirección especificada
    public void movePlayer(String direction) {
        if (players.isEmpty()) return;

        Player player = players.get(0);
        int newX = player.getXPosition();
        int newY = player.getYPosition();

        switch (direction) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
        }

        if (newX >= 0 && newX < gameMap.getWidth() &&
                newY >= 0 && newY < gameMap.getHeight() &&
                gameMap.isCellEmpty(newX, newY)) {

            gameMap.getCell(player.getXPosition(), player.getYPosition()).setPlayer(false);
            player.setPosition(newX, newY);
            gameMap.getCell(newX, newY).setPlayer(true);
        }
    }

    // ✅ Devuelve el mapa como un string
    public String getMapAsString() {
        StringBuilder mapString = new StringBuilder();
        for (int i = 0; i < gameMap.getHeight(); i++) {
            for (int j = 0; j < gameMap.getWidth(); j++) {
                Cell cell = gameMap.getCell(i, j);
                if (cell.hasPlayer()) {
                    mapString.append("P");
                } else if (cell.hasBomb()) {
                    mapString.append("B");  // Ahora mostrará 'B' para bombas
                } else if (cell.hasWall()) {
                    mapString.append("#");
                } else {
                    mapString.append(".");
                }
            }
            mapString.append("\n");
        }
        return mapString.toString();
    }

    // ✅ Retorna la configuración del juego
    public GameConfig getConfig() {
        return config;
    }

    // ✅ Método para inicializar posiciones de los jugadores
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

    // ✅ Método para colocar bloques aleatorios en el mapa
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

    public void startGame() {
        System.out.println("Juego iniciado con la siguiente configuración:");
        System.out.println("Duración: " + config.getDuracion() + " min");
        System.out.println("Jugadores: " + config.getJugadores());
        System.out.println("Bloques: " + config.getBloques());
        System.out.println("Vidas: " + config.getVidas());

        // Limpiar el mapa y reiniciar posiciones
        this.gameMap = new Map(config.getMapaAlto(), config.getMapaAncho());
        this.players.clear();

        assignRandomPositions();
        placeRandomBlocks(config.getBloques());

        System.out.println("Mapa generado:\n" + getMapAsString());
    }

}
