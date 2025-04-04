package bomberman.arsw.Model;

import java.util.List;

public class GameBoard {
    private GameConfig config;
    private List<Player> players;
    private int size = 15; // Tamaño del tablero

    public GameBoard(GameConfig config, List<Player> players) {
        this.config = config;
        this.players = players;
        initializePlayerPositions();
    }

    private void initializePlayerPositions() {
        // Asignar posiciones iniciales según la cantidad de jugadores
        switch(players.size()) {
            case 1:
                players.get(0).setPosition(size/2, size/2); // Centro
                break;
            case 2:
                players.get(0).setPosition(1, 1);           // Esquina superior izquierda
                players.get(1).setPosition(size - 2, size - 2); // Esquina inferior derecha
                break;
            case 3:
                players.get(0).setPosition(1, 1);           // Esquina superior izquierda
                players.get(1).setPosition(1, size-2);      // Esquina superior derecha
                players.get(2).setPosition(size-2, 1);      // Esquina inferior izquierda
                break;
            case 4:
                players.get(0).setPosition(1, 1);           // Esquina superior izquierda
                players.get(1).setPosition(1, size-2);      // Esquina superior derecha
                players.get(2).setPosition(size-2, 1);      // Esquina inferior izquierda
                players.get(3).setPosition(size-2, size-2); // Esquina inferior derecha
                break;
        }
    }

    public GameConfig getConfig() {
        return config;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getSize() {
        return size;
    }

    // Getters y otros métodos
}