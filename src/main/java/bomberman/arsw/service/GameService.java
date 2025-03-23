package bomberman.arsw.service;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService
{
    private Game currentGame;

    public void startGame(GameConfig config)
    {
        this.currentGame = new Game(config);
        this.currentGame.startGame();
    }

    public Game getCurrentGame()
    {
        return currentGame;
    }

}
