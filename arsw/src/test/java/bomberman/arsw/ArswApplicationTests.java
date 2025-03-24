/*package bomberman.arsw;

import bomberman.arsw.Model.Game;
import bomberman.arsw.Model.GameConfig;
import bomberman.arsw.service.GameService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ArswApplicationTests
{

	@Test
	void contextLoads()
	{

	}

	@Test
	public void testValidConfig() {
		GameConfig config = new GameConfig(5, 4, 10, 3);
		assertEquals(5, config.getDuracion());
		assertEquals(4, config.getJugadores());
		assertEquals(10, config.getBloques());
		assertEquals(3, config.getVidas());
	}

	@Test
	public void testInvalidConfig() {
		assertThrows(IllegalArgumentException.class, () -> new GameConfig(-5, 4, 10, 3));
	}

	@Test
	void testDuracionInvalida() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new GameConfig(8, 2, 10, 3));
		assertEquals("Duración inválida. Debe ser 2, 5 o 10 minutos.", exception.getMessage());
	}

	@Test
	void testJugadoresInvalidos() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new GameConfig(5, 5, 10, 3));
		assertEquals("Número de jugadores inválido. Debe estar entre 1 y 4.", exception.getMessage());
	}

	@Test
	void testBloquesInvalidos() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new GameConfig(5, 2, 50, 3));
		assertEquals("Número de bloques inválido. Debe estar entre 1 y 30.", exception.getMessage());
	}

	@Test
	void testVidasInvalidas() {
		Exception exception = assertThrows(IllegalArgumentException.class, () -> new GameConfig(5, 2, 10, 15));
		assertEquals("Número de vidas inválido. Debe estar entre 1 y 10.", exception.getMessage());
	}

	@Test
	void testSettersValidos() {
		GameConfig config = new GameConfig(5, 2, 10, 3);

		config.setDuracion(10);
		assertEquals(10, config.getDuracion());

		config.setJugadores(4);
		assertEquals(4, config.getJugadores());

		config.setBloques(20);
		assertEquals(20, config.getBloques());

		config.setVidas(8);
		assertEquals(8, config.getVidas());
	}

	@Test
	void testSettersInvalidos() {
		GameConfig config = new GameConfig(5, 2, 10, 3);

		assertThrows(IllegalArgumentException.class, () -> config.setDuracion(7));
		assertThrows(IllegalArgumentException.class, () -> config.setJugadores(0));
		assertThrows(IllegalArgumentException.class, () -> config.setBloques(35));
		assertThrows(IllegalArgumentException.class, () -> config.setVidas(12));
	}

	@Test
	void testIsValidConfig() {
		GameConfig configValida = new GameConfig(5, 2, 10, 3);
		assertTrue(configValida.isValidConfig());

		GameConfig configInvalida = new GameConfig(5, 2, 10, 3);
		configInvalida.setVidas(12);
		assertFalse(configInvalida.isValidConfig());
	}

	@Test
	void testToString() {
		GameConfig config = new GameConfig(5, 2, 10, 3);
		String esperado = "Configuración de la partida: Duración = 5 minutos, Jugadores = 2, Bloques = 10, Vidas = 3";
		assertEquals(esperado, config.toString());
	}

	/*
	@Test
	void testGameInitialization()
	{
		GameConfig config = new GameConfig(10, 4, 20, 3);
		Game game = new Game(config);

		assertNotNull(game);
		assertEquals(10, game.getConfig().getDuracion());
		assertEquals(4, game.getConfig().getJugadores());
		assertEquals(20, game.getConfig().getBloques());
		assertEquals(3, game.getConfig().getVidas());
	}

	@Test
	void testStartGame() {
		GameService gameService = new GameService();
		GameConfig config = new GameConfig(15, 2, 10, 5);

		gameService.startGame(config);
		Game game = gameService.getCurrentGame();

		assertNotNull(game);
		assertEquals(15, game.getConfig().getDuracion());
		assertEquals(2, game.getConfig().getJugadores());
		assertEquals(10, game.getConfig().getBloques());
		assertEquals(5, game.getConfig().getVidas());
	}

	@Test
	void testGetCurrentGameWhenNoGameStarted() {
		GameService gameService = new GameService();
		assertNull(gameService.getCurrentGame());
	}


}
*/