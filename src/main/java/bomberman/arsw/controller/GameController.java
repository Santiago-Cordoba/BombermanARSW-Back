package bomberman.arsw.controller;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import bomberman.arsw.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/game")
public class GameController {

    private static final Logger logger = Logger.getLogger(GameController.class.getName());
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/placeBomb")
    public ResponseEntity<String> placeBomb() {
        Game game = gameService.getCurrentGame();
        if (game == null) {
            return ResponseEntity.status(404).body("No hay una partida en curso.");
        }

        game.placeBomb();
        return ResponseEntity.ok(game.getMapAsString());
    }

    @PostMapping("/move")
    public ResponseEntity<String> movePlayer(@RequestParam String direction) {
        Game game = gameService.getCurrentGame();
        if (game == null) {
            return ResponseEntity.status(404).body("No hay una partida en curso.");
        }

        game.movePlayer(direction);
        return ResponseEntity.ok(game.getMapAsString());  // Devuelve el nuevo estado del mapa
    }

    @PostMapping("/start")
    public ResponseEntity<String> startGame(@RequestBody GameConfig config) {
        try {
            gameService.startGame(config);
            return ResponseEntity.ok("Juego iniciado con configuración: " + config);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> getGameStatus() {
        Game currentGame = gameService.getCurrentGame();
        if (currentGame == null) {
            return ResponseEntity.status(404).body("No hay una partida en curso.");
        }

        int playersConnected = gameService.getConnectedPlayers().size();
        int requiredPlayers = currentGame.getConfig().getPlayerCount();

        if (gameService.allPlayersReady()) {
            return ResponseEntity.ok("Todos los jugadores están listos. La partida puede iniciar.");
        } else {
            return ResponseEntity.ok("Esperando jugadores... " + playersConnected + "/" + requiredPlayers);
        }
    }

    @GetMapping("/map")
    public ResponseEntity<String> getGameMap() {
        Game currentGame = gameService.getCurrentGame();
        if (currentGame == null || currentGame.getMapAsString() == null) {
            return ResponseEntity.status(404).body("No hay una partida en curso o el mapa no está disponible.");
        }
        return ResponseEntity.ok(currentGame.getMapAsString());
    }
}
