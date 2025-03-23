package bomberman.arsw.Model;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import org.springframework.stereotype.Service;

@Service
public class Game
{
    private GameConfig config;

    public Game(GameConfig config)
    {
        this.config = config;
    }

    public void startGame()
    {
        System.out.println("Iniciando juego con configuración: " + config);

    }

    public GameConfig getConfig()
    {
        return config;
    }



}
