package bomberman.arsw;

import bomberman.arsw.Model.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import bomberman.arsw.Model.ExtraFirePowerUp;

import java.util.List;

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

}
