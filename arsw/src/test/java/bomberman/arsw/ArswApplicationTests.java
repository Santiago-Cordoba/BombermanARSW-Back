package bomberman.arsw;

import bomberman.arsw.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ArswApplicationTests {

	private Player player;

	@Test
	void contextLoads() {
	}

	@BeforeEach
	public void setUp() {
		player = new Player(1, 1, 3, "Santiago", 1);
	}

	/**
	 * Verifica el ciclo completo de colocación y explosión de una bomba
	 * y su impacto en el jugador que la colocó.
	 *
	 * Escenario:
	 * - Configuración de juego con un único jugador en la posición (2,2)
	 *   con 2 vidas iniciales.
	 * - Se invoca placeBomb para dejar la bomba en su celda.
	 * - Se fuerza la explosión inmediata con forceExplodeBombAt.
	 *
	 * Comprobaciones:
	 * 1. Tras placeBomb, la celda (2,2) debe reportar hasBomb() == true.
	 * 2. Tras la explosión forzada:
	 *    - El jugador debe perder exactamente una vida (de 2 a 1).
	 *    - La celda (2,2) ya no debe contener bomba.
	 */
	@Test
	void testBombPlacementAndExplosionAffectsPlayer() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(2, 2, 2, "JugadorA", 1); // Dos vidas iniciales
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// 1. Colocar el jugador en la celda (2,2)
		map.placePlayer(2, 2, player);

		// 2. Colocar la bomba y comprobar su presencia
		board.placeBomb(2, 2, player);
		assertTrue(map.getCell(2, 2).hasBomb(),
				"La celda (2,2) debe contener una bomba tras placeBomb");

		// 3. Forzar la explosión y validar efectos sobre el jugador y la bomba
		board.forceExplodeBombAt(2, 2);
		assertEquals(1, player.getLives(),
				"El jugador debe perder una vida tras la explosión");
		assertFalse(map.getCell(2, 2).hasBomb(),
				"La bomba debe desaparecer de la celda (2,2) después de explotar");
	}

	/**
	 * Verifica que no se pueda colocar una segunda bomba en una celda que ya contiene una bomba.
	 *
	 * Este test cubre la siguiente lógica de negocio:
	 * - Un jugador puede colocar una bomba en una celda vacía.
	 * - Si la celda ya contiene una bomba, la colocación debe ser ignorada.
	 * - Solo debe existir una bomba en dicha celda y en la lista de bombas del tablero.
	 *
	 * Escenario:
	 * - Jugador ubicado en (1,1).
	 * - Coloca una bomba en esa celda.
	 * - Intenta colocar una segunda bomba en el mismo lugar.
	 *
	 * Resultados esperados:
	 * - La primera bomba se coloca exitosamente.
	 * - La segunda bomba es ignorada.
	 * - La celda sigue conteniendo solo una bomba.
	 */
	@Test
	void testCannotPlaceBombOnOccupiedCell() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "JugadorA", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Ubicar al jugador en la celda (1,1)
		map.placePlayer(1, 1, player);

		// Colocar la primera bomba
		board.placeBomb(1, 1, player);
		assertEquals(1, board.getBombs().size(), "Debe haber una bomba en la lista");
		assertTrue(map.getCell(1, 1).hasBomb(), "La celda debe contener la bomba");

		// Intentar colocar una segunda bomba en la misma celda
		board.placeBomb(1, 1, player);

		// Validar que no se añadió una nueva bomba
		assertEquals(1, board.getBombs().size(), "No se debe permitir colocar una segunda bomba en la misma celda");
	}

	/**
	 * Verifica que una explosión destruye correctamente una pared marcada como destructible.
	 *
	 * Escenario:
	 * - Se crea una celda en (2,1) como pared destructible.
	 * - Un jugador coloca una bomba en (1,1), con rango suficiente para alcanzar (2,1).
	 * - Se simula la explosión de la bomba inmediatamente.
	 *
	 * Comprobaciones:
	 * - Antes de la explosión, (2,1) es una pared destructible.
	 * - Después de la explosión, (2,1) ya no debe ser una pared.
	 */
	@Test
	void testExplosionDestroysDestructibleWall() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "JugadorA", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Configurar manualmente pared destructible en (2,1)
		Cell destructibleWall = map.getCell(2, 1);
		destructibleWall.setWall(true);
		destructibleWall.setDestructible(true);
		assertTrue(destructibleWall.isWall(), "La celda (2,1) debe ser una pared al inicio");
		assertTrue(destructibleWall.isDestructible(), "La celda (2,1) debe ser destructible");

		// Ubicar al jugador y colocar bomba en (1,1)
		map.placePlayer(1, 1, player);
		board.placeBomb(1, 1, player);

		// Simular explosión inmediata
		board.forceExplodeBombAt(1, 1);

		// Verificar que la pared fue destruida
		assertFalse(destructibleWall.isWall(), "La celda (2,1) ya no debe ser una pared tras la explosión");
	}




	/**
	 * Verifica que una bomba activa otra bomba cercana al estar en su rango.
	 *
	 * Escenario:
	 * - Se colocan dos bombas:
	 *   - Bomba A en (5,5)
	 *   - Bomba B en (7,5)
	 * - El jugador tiene un rango de explosión de 2.
	 * - Se fuerza la explosión de la bomba A.
	 *
	 * Comprobaciones:
	 * - La bomba B debe ser alcanzada y removida por la reacción en cadena.
	 */
	@Test
	void testChainReactionTriggersAdjacentBombs() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(0, 0, 3, "Bomber", 1);

		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		map.getCell(5, 5).setWall(false);
		map.getCell(6, 5).setWall(false);
		map.getCell(7, 5).setWall(false);

		GameBoard board = new GameBoard(config, players, map);

		// Bomba A (5,5)
		Bomb bombA = new Bomb(5, 5, player);
		board.getBombs().add(bombA);
		map.placeBomb(5, 5, bombA);

		// Bomba B (7,5)
		Bomb bombB = new Bomb(7, 5, player);
		board.getBombs().add(bombB);
		map.placeBomb(7, 5, bombB);

		// Forzar explosión de bomba A
		board.forceExplodeBombAt(5, 5);

		// Validar que la bomba B haya sido detonada (ya no está en el tablero)
		boolean bombBStillExists = board.getBombs().stream()
				.anyMatch(b -> b.getX() == 7 && b.getY() == 5);

		assertFalse(bombBStillExists, "La explosión debe haber alcanzado y detonado la segunda bomba (7,5)");
	}

	/**
	 * Verifica que un jugador no puede recoger un power-up si hay una bomba presente en la misma celda.
	 *
	 * Escenario:
	 * - Se crea un jugador en la posición (1,1) con 3 vidas.
	 * - En esa misma celda se colocan simultáneamente un power-up de tipo LIFE_UP y una bomba.
	 * - Se intenta recolectar el power-up.
	 *
	 * Comprobaciones:
	 * - La recolección del power-up debe fallar si hay una bomba en la celda.
	 * - El jugador no debe aumentar su número de vidas.
	 * - El power-up debe permanecer en el tablero.
	 */


	/**
	 * Verifica que un jugador no pueda moverse a una celda ocupada por una pared.
	 *
	 * Escenario:
	 * - Se inicializa un jugador en la posición (1,1).
	 * - Se configura una pared en la celda adyacente (1,2).
	 * - Se intenta mover al jugador hacia esa celda con pared.
	 *
	 * Comprobaciones:
	 * - El movimiento debe ser rechazado.
	 * - La posición del jugador no debe cambiar.
	 */
	@Test
	void testPlayerCannotMoveIntoWall() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Ubicar jugador en (1,1)
		map.placePlayer(1, 1, player);

		// Establecer una pared en (1,2)
		Cell wallCell = map.getCell(1, 2);
		wallCell.setWall(true);

		// Intentar mover al jugador hacia la pared
		boolean moved = board.movePlayer(player, 1, 2);

		// Validaciones
		assertFalse(moved, "El jugador no debe poder moverse a una celda con pared");
		assertEquals(1, player.getX(), "La posición X del jugador no debe cambiar");
		assertEquals(1, player.getY(), "La posición Y del jugador no debe cambiar");
	}



	/**
	 * Verifica que un jugador con una única vida sea eliminado del juego
	 * tras la explosión de su propia bomba.
	 *
	 * Escenario:
	 * - Configuración de juego con 1 jugador ubicado en (3,3) y 1 vida.
	 * - Se coloca una bomba en la misma posición.
	 * - Se fuerza la explosión inmediatamente.
	 *
	 * Comprobaciones:
	 * - El jugador ya no debe estar presente en la lista de jugadores del tablero.
	 */
	@Test
	void testPlayerLosesAllLivesAndIsRemovedFromGame() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(3, 3, 1, "LastLifePlayer", 1);
		var players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		map.placePlayer(3, 3, player);
		board.placeBomb(3, 3, player);
		board.forceExplodeBombAt(3, 3);

		assertFalse(board.getPlayers().contains(player),
				"El jugador debe ser eliminado del juego al quedarse sin vidas");
	}

	/**
	 * Comprueba que la explosión de una bomba no atraviesa muros indestructibles.
	 *
	 * Escenario:
	 * - Jugador en (3,3) con capacidad de bomba estándar.
	 * - Se coloca un muro indestructible en la celda (4,3) adyacente.
	 * - Se coloca y explota la bomba en (3,3).
	 *
	 * Comprobaciones:
	 * - Una celda situada detrás del muro (5,3) no debe verse afectada
	 *   (no debe contener bomba ni cambios de estado).
	 */
	@Test
	void testExplosionDoesNotCrossIndestructibleWalls() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(3, 3, 3, "WallTester", 1);
		var players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Configuramos el muro indestructible
		Cell wall = map.getCell(4, 3);
		wall.setWall(true);
		wall.setDestructible(false);

		// Colocamos y explotamos la bomba
		map.placePlayer(3, 3, player);
		board.placeBomb(3, 3, player);
		board.forceExplodeBombAt(3, 3);

		// Verificamos que la celda (5,3) permanece intacta
		Cell blockedCell = map.getCell(5, 3);
		assertFalse(blockedCell.hasBomb(),
				"La explosión no debe alcanzar celdas detrás de una pared indestructible");
	}

	/**
	 * Asegura que un jugador pueda colocar varias bombas si su capacidad aumenta.
	 *
	 * Escenario:
	 * - Jugador con capacidad inicial 1, incrementada a 2.
	 * - Se coloca una bomba en (1,1), se mueve y coloca otra en (1,2).
	 *
	 * Comprobaciones:
	 * - El tablero debe reportar 2 bombas activas en total.
	 */
	@Test
	void testPlayerCanPlaceMultipleBombsWhenCapacityIncreases() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "Bomberman", 1);
		player.increaseBombCapacity(); // capacidad ahora 2
		var players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		map.placePlayer(1, 1, player);
		board.placeBomb(1, 1, player);

		// Simulamos movimiento para colocar segunda bomba
		map.getCell(1, 1).setBomb(null);
		player.setPosition(1, 2);
		map.placePlayer(1, 2, player);
		board.placeBomb(1, 2, player);

		assertEquals(2, board.getBombs().size(),
				"El jugador debe poder colocar dos bombas al aumentar su capacidad");
	}

	@Test
	public void testPlayerInitialization() {
		assertNotNull(player.getId());
		assertEquals(1, player.getX());
		assertEquals(1, player.getY());
		assertEquals(3, player.getLives());
		assertEquals("Santiago", player.getName());
		assertEquals(1, player.getBombs());
		assertFalse(player.isReady());
		assertFalse(player.isHost());
		assertEquals(1, player.getBombCapacity());
		assertEquals(1, player.getBombRange());
	}

	@Test
	public void testSetPosition() {
		player.setPosition(5, 7);
		assertEquals(5, player.getX());
		assertEquals(7, player.getY());
	}

	@Test
	public void testIncreaseAndDecreaseLives() {
		player.increaseLives(2);
		assertEquals(5, player.getLives());
	}

	@Test
	public void testIncreaseAndDecreaseBombCapacity() {
		player.increaseBombCapacity();
		assertEquals(2, player.getBombCapacity());

		player.decreaseBombCapacity();
		assertEquals(1, player.getBombCapacity());

		player.decreaseBombCapacity();
		assertEquals(0, player.getBombCapacity());

		// Should not go below 0
		player.decreaseBombCapacity();
		assertEquals(0, player.getBombCapacity());
	}

	@Test
	public void testCanPlaceBomb() {
		assertTrue(player.canPlaceBomb());

		player.decreaseBombCapacity();
		assertFalse(player.canPlaceBomb());
	}

	@Test
	public void testEqualsAndHashCode() {
		Player other = new Player(1, 1, 3, "Other", 1);
		other.setId(player.getId());

		assertEquals(player, other);
		assertEquals(player.hashCode(), other.hashCode());
	}

	@Test
	public void testToJsonStringFormat() {
		String json = player.toJsonString();
		assertTrue(json.contains("\"id\":\"" + player.getId() + "\""));
		assertTrue(json.contains("\"name\":\"Santiago\""));
		assertTrue(json.contains("\"x\":1"));
		assertTrue(json.contains("\"y\":1"));
		assertTrue(json.contains("\"lives\":3"));
		assertTrue(json.contains("\"bombCapacity\":1"));
	}

	@Test
	public void testSetHostAndReady() {
		player.setHost(true);
		assertTrue(player.isHost());

		player.setReady(true);
		assertTrue(player.isReady());
	}

	@Test
	public void testIncreaseBombRange() {
		int original = player.getBombRange();
		player.increaseBombRange(1);
		assertEquals(original + 1, player.getBombRange());
	}

	@Test
	public void testIsNearCorner() {
		int width = 15;
		int height = 13;

		assertTrue(invokeIsNearCorner(0, 0, width, height));       // Esquina superior izquierda
		assertTrue(invokeIsNearCorner(14, 0, width, height));      // Esquina superior derecha
		assertTrue(invokeIsNearCorner(0, 12, width, height));      // Esquina inferior izquierda
		assertTrue(invokeIsNearCorner(14, 12, width, height));     // Esquina inferior derecha
		assertFalse(invokeIsNearCorner(7, 6, width, height));      // Centro
	}

	// Para acceder a método privado estático
	private boolean invokeIsNearCorner(int x, int y, int width, int height) {
		try {
			var method = GameMap.class.getDeclaredMethod("isNearCorner", int.class, int.class, int.class, int.class);
			method.setAccessible(true);
			return (boolean) method.invoke(null, x, y, width, height);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testToJsonStringStructure() {
		GameMap map = new GameMap(3, 3);
		String json = map.toJsonString();
		assertTrue(json.contains("\"width\":3"));
		assertTrue(json.contains("\"height\":3"));
		assertTrue(json.contains("\"cells\":["));
	}

	@Test
	public void testGetCellStates() {
		GameMap map = new GameMap(2, 2);
		map.placeWall(0, 0);


		List<List<Map<String, Object>>> states = map.getCellStates();
		assertEquals(2, states.size());
		assertTrue((Boolean) states.get(0).get(0).get("isWall"));
		assertFalse((Boolean) states.get(1).get(1).get("hasPowerUp"));

	}
}


