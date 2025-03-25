package bomberman.arsw.service;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import bomberman.arsw.Model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService
{
    private Game currentGame;
    private List<Player> connectedPlayers = new ArrayList<>();
    private int expectedPlayers = 0;  // Número esperado de jugadores
    private final int MIN_PLAYERS = 1;
    private final int MAX_PLAYERS = 4;

    public void startGame(GameConfig config)
    {
        int playerCount = config.getPlayerCount();

        if (playerCount < MIN_PLAYERS || playerCount > MAX_PLAYERS)
        {
            throw new IllegalArgumentException("El número de jugadores debe estar entre " + MIN_PLAYERS + " y " + MAX_PLAYERS + ".");
        }
        if (currentGame != null)
        {
            throw new IllegalStateException("El juego ya ha sido iniciado.");
        }

        this.expectedPlayers = playerCount;
        this.currentGame = new Game(config);
        System.out.println("Juego iniciado con configuración: " + config);
    }

    public Game getCurrentGame()
    {
        return currentGame;
    }

    public void addPlayer(String playerName)
    {
        if (connectedPlayers.size() >= expectedPlayers)
        {
            System.out.println("No se pueden agregar más jugadores. Límite alcanzado.");
            return;
        }

        if (!connectedPlayers.contains(playerName))
        {
            connectedPlayers.add(new Player(connectedPlayers.size(),0,0));
            System.out.println("Jugador añadido: " + playerName);
        }
    }

    public List<Player> getConnectedPlayers()
    {
        return connectedPlayers;
    }

    public boolean allPlayersReady()
    {
        return connectedPlayers.size() == expectedPlayers;
    }
}
