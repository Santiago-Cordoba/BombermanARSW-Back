package bomberman.arsw;

import bomberman.arsw.Model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import bomberman.arsw.Model.ExtraFirePowerUp;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class ArswApplicationTests {

	@Test
	void contextLoads() {
	}

	/**
	 * Prueba unitaria para verificar que el jugador incrementa su rango de explosión
	 * al recolectar un power-up de tipo FIRE_PLUS.
	 *
	 * Se simula una partida con un solo jugador ubicado en la celda (1,1), donde también
	 * se coloca el power-up. Al ejecutar la recolección, se espera que:
	 * - El jugador aumente su rango de bomba de 1 a 2.
	 * - El power-up desaparezca del tablero.
	 * - La recolección sea exitosa.
	 */
	@Test
	void testCollectLifeUpPowerUp_increasesPlayerLives() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 2, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		LifeUpPowerUp lifeUp = new LifeUpPowerUp(1);
		lifeUp.setPosition(1, 1);
		board.getPowerUps().add(lifeUp);
		map.getCell(1, 1).setPowerUp(lifeUp); // 👉 necesario

		boolean collected = board.collectPowerUp(player.getId(), 1, 1);

		assertTrue(collected, "El power-up debe ser recogido");
		assertEquals(3, player.getLives(), "La vida del jugador debe incrementarse a 3");
		assertFalse(board.getPowerUps().contains(lifeUp), "El power-up debe eliminarse del tablero");
	}

	/**
	 * Prueba unitaria para verificar que el jugador incrementa su número de vidas
	 * al recolectar un power-up de tipo LIFE_UP.
	 *
	 * El jugador inicia con 2 vidas y un máximo permitido de 3. Se ubica un power-up
	 * de vida en su misma posición (1,1). Al ejecutar la recolección se espera que:
	 * - El jugador aumente sus vidas a 3.
	 * - El power-up sea removido del tablero.
	 * - La recolección sea válida.
	 */
	@Test
	void testCollectFirePlusPowerUp_increasesBombRange() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		ExtraFirePowerUp firePlus = new ExtraFirePowerUp(1, 1);
		board.getPowerUps().add(firePlus);
		board.getGameMap().getCell(1, 1).setPowerUp(firePlus); // 👈 Solución clave

		boolean collected = board.collectPowerUp(player.getId(), 1, 1);

		assertTrue(collected, "El power-up debe ser recogido");
		assertEquals(2, player.getBombRange(), "El rango de la bomba debe incrementarse a 2");
		assertFalse(board.getPowerUps().contains(firePlus), "El power-up debe eliminarse del tablero");
	}

	/**
	 * Prueba unitaria para validar que un jugador se vuelve invencible al
	 * recoger un power-up de tipo INVINCIBILITY.
	 *
	 * Escenario:
	 * - Se crea un jugador ubicado en (1,1) con 3 vidas y rango de bomba 1.
	 * - Se coloca un power-up de invencibilidad en la misma celda del mapa.
	 *
	 * Comprobaciones:
	 * - El power-up debe ser correctamente recolectado.
	 * - El jugador debe quedar en estado invencible.
	 * - El power-up debe desaparecer del tablero tras su recolección.
	 */
	@Test
	void testCollectInvincibilityPowerUp_grantsInvincibility() {
		GameConfig config = new GameConfig(300, 3); // duración 5 minutos, vidas máximas: 3
		Player player = new Player(1, 1, 3, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		InvincibilityPowerUp inv = new InvincibilityPowerUp(1, 1);
		board.getPowerUps().add(inv);
		map.getCell(1, 1).setPowerUp(inv); // 👈 Esencial para que el mapa registre el power-up

		boolean collected = board.collectPowerUp(player.getId(), 1, 1);

		assertTrue(collected, "El power-up debe ser recogido");
		assertTrue(player.isInvincible(), "El jugador debe estar en estado invencible");
		assertFalse(board.getPowerUps().contains(inv), "El power-up debe eliminarse del tablero");
	}

	/**
	 * Prueba unitaria para verificar que un jugador no puede recoger un power-up
	 * si no se encuentra exactamente en la misma posición (x, y) que el power-up.
	 *
	 * Esta prueba es importante para asegurar que la lógica de recolección
	 * depende estrictamente de la coincidencia de coordenadas en el tablero,
	 * evitando efectos no deseados si el jugador se encuentra cerca pero no
	 * sobre el power-up.
	 */
	@Test
	void testPlayerDoesNotCollectPowerUp_ifNotOnSameCell() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(2, 2, 3, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Colocar un power-up en (1,1), pero el jugador está en (2,2)
		LifeUpPowerUp lifeUp = new LifeUpPowerUp(1);
		lifeUp.setPosition(1, 1);
		board.getPowerUps().add(lifeUp);
		map.getCell(1, 1).setPowerUp(lifeUp);

		// El jugador no está en la celda del power-up, no debería recogerlo
		boolean collected = board.collectPowerUp(player.getId(), 2, 2);

		assertFalse(collected, "El power-up no debe ser recogido si el jugador no está en la misma celda");
		assertEquals(3, player.getLives(), "La cantidad de vidas del jugador no debe cambiar");
		assertTrue(board.getPowerUps().contains(lifeUp), "El power-up debe seguir en el tablero");
	}

	/**
	 * Prueba unitaria que valida la colocación de una bomba en el tablero,
	 * su explosión inmediata y los efectos sobre un jugador en la misma celda.
	 */
	@Test
	void testBombPlacementAndExplosionAffectsPlayer() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(2, 2, 2, "JugadorA", 1); // Tiene 2 vidas
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Asegurar que el jugador esté físicamente en la celda
		map.placePlayer(2, 2, player);

		// Colocar bomba
		board.placeBomb(2, 2, player);
		assertTrue(map.getCell(2, 2).hasBomb(), "La celda debe contener una bomba");

		// Explosión simulada
		board.forceExplodeBombAt(2, 2);

		// Validar efectos
		assertEquals(1, player.getLives(), "El jugador debe perder una vida por la explosión");
		assertFalse(map.getCell(2, 2).hasBomb(), "La bomba debe desaparecer tras explotar");
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
	 * Verifica que un jugador con estado de invencibilidad no pierde vidas al estar en el centro de una explosión.
	 *
	 * Escenario:
	 * - Un jugador con 2 vidas se ubica en (5,5).
	 * - Se activa su estado de invencibilidad por 5 segundos.
	 * - Se coloca una bomba en la misma celda.
	 * - Se fuerza la explosión inmediatamente.
	 *
	 * Comprobaciones:
	 * - El jugador no debe perder vidas.
	 * - Sigue teniendo 2 vidas después de la explosión.
	 */
	@Test
	void testInvinciblePlayerSurvivesExplosion() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(5, 5, 2, "JugadorInvencible", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);

		// Limpiar la zona de la explosión
		for (int dx = -1; dx <= 1; dx++) {
			for (int dy = -1; dy <= 1; dy++) {
				int x = 5 + dx;
				int y = 5 + dy;
				if (map.isValidPosition(x, y)) {
					map.getCell(x, y).setWall(false);
				}
			}
		}

		// Activar invencibilidad
		player.activateInvincibility(5000); // 5 segundos

		GameBoard board = new GameBoard(config, players, map);
		map.placePlayer(5, 5, player);
		board.placeBomb(5, 5, player);

		// Forzar explosión
		board.forceExplodeBombAt(5, 5);

		// Validación
		assertEquals(2, player.getLives(), "El jugador invencible no debe perder vidas al recibir una explosión");
		assertTrue(player.isInvincible(), "El jugador debe seguir invencible justo después de la explosión");
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
		player.increaseBombRange(); // Rango = 2
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
	@Test
	void testPowerUpNotCollectedIfBombIsPresentInCell() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 3, "TestPlayer", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		LifeUpPowerUp powerUp = new LifeUpPowerUp(1);
		powerUp.setPosition(1, 1);
		Bomb bomb = new Bomb(1, 1, player);

		// Configurar la celda con bomba y power-up
		map.getCell(1, 1).setPowerUp(powerUp);
		map.getCell(1, 1).setBomb(bomb);
		board.getPowerUps().add(powerUp);

		// Intentar recoger el power-up
		boolean collected = board.collectPowerUp(player.getId(), 1, 1);

		// Validaciones
		assertFalse(collected, "El power-up no debe ser recogido si hay una bomba en la celda");
		assertEquals(3, player.getLives(), "Las vidas del jugador no deben cambiar");
		assertTrue(map.getCell(1, 1).hasPowerUp(), "El power-up debe seguir en la celda");
	}

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
	 * Verifica que un jugador recoge automáticamente un power-up
	 * al moverse a la celda que lo contiene.
	 *
	 * Escenario:
	 * - Jugador inicia en (1,1) con 2 vidas.
	 * - En la celda adyacente (1,2) hay un power-up de vida (LIFE_UP).
	 * - El jugador se mueve a (1,2).
	 *
	 * Comprobaciones:
	 * - El jugador aumenta su número de vidas (de 2 a 3).
	 * - El power-up desaparece del tablero.
	 * - El movimiento es exitoso.
	 */
	@Test
	void testPlayerAutomaticallyCollectsPowerUpOnMove() {
		GameConfig config = new GameConfig(300, 3);
		Player player = new Player(1, 1, 2, "AutoCollector", 1);
		List<Player> players = List.of(player);

		GameMap map = GameMap.createDefaultMap(1);
		GameBoard board = new GameBoard(config, players, map);

		// Crear power-up en (1,2)
		LifeUpPowerUp powerUp = new LifeUpPowerUp(1);
		powerUp.setPosition(1, 2);
		board.getPowerUps().add(powerUp);
		map.getCell(1, 2).setPowerUp(powerUp);

		// Ubicar jugador en (1,1)
		map.placePlayer(1, 1, player);

		// Ejecutar movimiento a la celda con power-up
		boolean moved = board.movePlayer(player, 1, 2);

		assertTrue(moved, "El jugador debe poder moverse a la celda libre");
		assertEquals(3, player.getLives(), "El jugador debe haber recogido el power-up y aumentado sus vidas");
		assertFalse(map.getCell(1, 2).hasPowerUp(), "El power-up debe ser eliminado de la celda tras la recolección");
		assertFalse(board.getPowerUps().contains(powerUp), "El power-up debe ser eliminado del tablero");
	}



}
