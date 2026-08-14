package com.example

import com.example.model.AiDifficulty
import com.example.model.CarromEngine
import com.example.model.CoinType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ExampleUnitTest {

    private lateinit var engine: CarromEngine

    @Before
    fun setUp() {
        engine = CarromEngine(
            numberOfPlayers = 2,
            playerNames = listOf("Player 1", "AI Bot"),
            aiDifficulty = AiDifficulty.INTERMEDIATE
        )
    }

    @Test
    fun testNormalCoinScoring() {
        val player1 = engine.players[0]
        assertEquals(0, player1.score)

        // Find a white coin and simulate pocketing
        val whiteCoin = engine.coins.first { it.type == CoinType.WHITE }
        engine.simulateCoinPocketedForTesting(whiteCoin)

        assertEquals(10, player1.score)
        assertEquals(1, player1.coinsCollected)

        // Find a black coin and simulate pocketing
        val blackCoin = engine.coins.first { it.type == CoinType.BLACK }
        engine.simulateCoinPocketedForTesting(blackCoin)

        assertEquals(15, player1.score)
        assertEquals(2, player1.coinsCollected)
    }

    @Test
    fun testRedCoinPocketedAndCovered_awardsDoublePointsAndQueenBonus() {
        val player1 = engine.players[0]
        val queen = engine.coins.first { it.type == CoinType.QUEEN }
        val whiteCoin = engine.coins.first { it.type == CoinType.WHITE }

        // Step 1: Pocket Queen in Turn 1
        engine.simulateCoinPocketedForTesting(queen)
        assertTrue(engine.queenPocketedThisTurn)
        assertEquals(0, player1.score) // No points before covering

        // Turn ends after sinking Queen -> sets queenWaitingForCover
        engine.isMoving = false
        engine.concludeTurnForTesting()
        assertTrue(engine.queenWaitingForCover)
        assertEquals(0, engine.queenCoverPlayerIndex)
        assertEquals(0, player1.score)

        // Step 2: In cover turn, player sinks a White Coin
        engine.resetStrikerForPlayerForTesting(0)
        engine.simulateCoinPocketedForTesting(whiteCoin)
        assertTrue(engine.queenCoveredThisTurn)
        // White coin scored with double points = 20 pts
        assertEquals(20, player1.score)

        // Turn concludes -> awards Queen 25 pts bonus, clears cover state, keeps Queen pocketed
        engine.isMoving = false
        engine.concludeTurnForTesting()

        assertEquals(45, player1.score) // 20 (white double) + 25 (queen bonus) = 45 pts
        assertFalse(engine.queenWaitingForCover)
        assertFalse(engine.queenLeft)
        assertTrue(queen.isPocketed)

        // Step 3: Verify subsequent coins only award normal points (double points only awarded once)
        val blackCoin = engine.coins.first { it.type == CoinType.BLACK && !it.isPocketed }
        engine.resetStrikerForPlayerForTesting(0)
        engine.simulateCoinPocketedForTesting(blackCoin)
        engine.isMoving = false
        engine.concludeTurnForTesting()

        assertEquals(50, player1.score) // 45 + 5 = 50 pts (normal black coin)
    }

    @Test
    fun testRedCoinPocketedAndFailedCover_returnsQueenToCenter() {
        val player1 = engine.players[0]
        val queen = engine.coins.first { it.type == CoinType.QUEEN }

        // Step 1: Pocket Queen
        engine.simulateCoinPocketedForTesting(queen)
        assertTrue(engine.queenPocketedThisTurn)
        engine.isMoving = false
        engine.concludeTurnForTesting()
        assertTrue(engine.queenWaitingForCover)

        // Step 2: Cover shot misses (no coins pocketed)
        engine.resetStrikerForPlayerForTesting(0)
        engine.isMoving = false
        engine.concludeTurnForTesting()

        // Queen must return to board center, score remains 0, queenLeft remains true
        assertFalse(engine.queenWaitingForCover)
        assertTrue(engine.queenLeft)
        assertFalse(queen.isPocketed)
        assertEquals(CarromEngine.BOARD_SIZE / 2f, queen.x, 0.01f)
        assertEquals(CarromEngine.BOARD_SIZE / 2f, queen.y, 0.01f)
        assertEquals(0, player1.score)
    }

    @Test
    fun testAiScoringAndQueenCoverLogic() {
        // Switch to AI turn (index 1)
        engine.activePlayerIndex = 1
        val aiPlayer = engine.players[1]
        val queen = engine.coins.first { it.type == CoinType.QUEEN }
        val blackCoin = engine.coins.first { it.type == CoinType.BLACK }

        // AI pockets Queen
        engine.simulateCoinPocketedForTesting(queen)
        engine.isMoving = false
        engine.concludeTurnForTesting()
        assertTrue(engine.queenWaitingForCover)
        assertEquals(1, engine.queenCoverPlayerIndex)

        // AI pockets Black coin as cover
        engine.resetStrikerForPlayerForTesting(1)
        engine.simulateCoinPocketedForTesting(blackCoin)
        assertEquals(10, aiPlayer.score) // Black coin double points = 10 pts

        engine.isMoving = false
        engine.concludeTurnForTesting()
        assertEquals(35, aiPlayer.score) // 10 (black double) + 25 (queen bonus) = 35 pts
        assertFalse(engine.queenWaitingForCover)
        assertTrue(queen.isPocketed)
    }

    @Test
    fun testAiDifficultyLevelsAndConfigurations() {
        val difficulties = AiDifficulty.values()
        assertEquals(5, difficulties.size)

        val novice = AiDifficulty.NOVICE
        assertEquals(1, novice.strategicDepth)
        assertTrue(novice.accuracyVariance > 0.1f)

        val grandmaster = AiDifficulty.GRANDMASTER
        assertEquals(5, grandmaster.strategicDepth)
        assertEquals(0.0f, grandmaster.accuracyVariance, 0.001f)

        // Test calculateAiShot runs for all difficulty levels without throwing
        for (diff in difficulties) {
            engine.aiDifficulty = diff
            val shot = engine.calculateAiShot(diff)
            assertNotNull(shot)
            assertTrue(shot.first in 180f..620f)
            assertTrue(shot.second.length() > 0f)
        }
    }
}

