package bomberman.arsw.Model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameBoard {
    private List<Bomb> bombs = new ArrayList<>();
    private GameConfig config;
    private List<Player> players;
    private int size = 15;
    private CellType[][] board;

    public enum CellType {
        EMPTY('0'),
        WALL('1'),
        PLAYER('P'),
        BOMB('B'),
        EXPLOSION('X'),
        POWERUP('U');

        private final char symbol;

        CellType(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    public GameBoard(GameConfig config, List<Player> players) {
        System.out.println("[GameBoard] Inicializando nuevo tablero");
        System.out.println("[GameBoard] Configuración: " + config);
        System.out.println("[GameBoard] Jugadores: " + players.size());

        this.config = config;
        this.players = new ArrayList<>(players); // Crear copia para evitar modificaciones externas
        this.board = new CellType[size][size];

        initializeBoard();
        initializePlayerPositions();

        System.out.println("[GameBoard] Tablero inicializado correctamente");
        printBoardState();
    }

    private void initializeBoard() {
        System.out.println("[GameBoard] Inicializando estructura del tablero...");
        // Bordes sólidos
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0 || i == size - 1 || j == 0 || j == size - 1) {
                    board[i][j] = CellType.WALL;
                } else {
                    board[i][j] = CellType.EMPTY;
                }
            }
        }

        // Bloques destructibles (patrón de damero)
        for (int i = 2; i < size - 2; i += 2) {
            for (int j = 2; j < size - 2; j += 2) {
                board[i][j] = CellType.WALL;
            }
        }
    }

    private void initializePlayerPositions() {
        System.out.println("[GameBoard] Posicionando jugadores...");
        switch(players.size()) {
            case 1:
                players.get(0).setPosition(size/2, size/2);
                break;
            case 2:
                players.get(0).setPosition(1, 1);
                players.get(1).setPosition(size - 2, size - 2);
                break;
            case 3:
                players.get(0).setPosition(1, 1);
                players.get(1).setPosition(1, size-2);
                players.get(2).setPosition(size-2, 1);
                break;
            case 4:
                players.get(0).setPosition(1, 1);
                players.get(1).setPosition(1, size-2);
                players.get(2).setPosition(size-2, 1);
                players.get(3).setPosition(size-2, size-2);
                break;
        }
        updatePlayerCells();
    }

    private void updatePlayerCells() {
        // Limpiar posiciones anteriores de jugadores
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == CellType.PLAYER) {
                    board[i][j] = CellType.EMPTY;
                }
            }
        }

        // Actualizar nuevas posiciones
        for (Player player : players) {
            if (player.getLives() > 0) {
                board[player.getX()][player.getY()] = CellType.PLAYER;
                System.out.println("[GameBoard] Jugador " + player.getId() + " en posición: " + player.getX() + "," + player.getY());
            }
        }
    }

    public char[][] getBoardState() {
        char[][] state = new char[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                state[i][j] = board[i][j].getSymbol();
            }
        }

        // Añadir bombas
        for (Bomb bomb : bombs) {
            state[bomb.getXPosition()][bomb.getYPosition()] = CellType.BOMB.getSymbol();
        }

        return state;
    }

    public void update() {
        System.out.println("[GameBoard] Actualizando estado del tablero...");
        updatePlayerCells();
    }

    // Resto de métodos...
    public void printBoardState() {
        char[][] state = getBoardState();
        System.out.println("=== ESTADO ACTUAL DEL TABLERO ===");
        for (char[] row : state) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println("=================================");
    }
}