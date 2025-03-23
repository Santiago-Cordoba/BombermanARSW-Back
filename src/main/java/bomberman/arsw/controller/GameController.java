package bomberman.arsw.controller;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import bomberman.arsw.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/game")
public class GameController
{

    private static final Logger logger = Logger.getLogger(GameService.class.getName());

    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestBody GameConfig config)
    {
        gameService.startGame(config);
        return ResponseEntity.ok("Juego iniciado con configuración: " + config);
    }

    @GetMapping
    public ResponseEntity<Game> getCurrentGame()
    {
        Game game = gameService.getCurrentGame();
        if (game == null)
        {
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.ok(game);
    }



}
