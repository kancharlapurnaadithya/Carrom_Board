package com.example.model

import android.animation.ValueAnimator
import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlin.math.*

class CarromEngine(
    var numberOfPlayers: Int = 2,
    var playerNames: List<String> = listOf("Player 1", "Player 2"),
    var boardTheme: BoardTheme = BoardTheme.WOODEN,
    var coinTheme: CoinTheme = CoinTheme.CLASSIC
) {
    companion object {
        const val BOARD_SIZE = 800f
        const val PLAYABLE_MARGIN = 40f
        const val FRICTION = 0.982f // Deceleration multiplier
        const val POCKET_RADIUS = 36f
        const val COIN_RADIUS = 20f
        const val STRIKER_RADIUS = 28f
        const val COIN_MASS = 1.0f
        const val STRIKER_MASS = 3.5f

        // Pockets centers
        val POCKETS = listOf(
            PhysicsVector(55f, 55f),     // Top Left
            PhysicsVector(745f, 55f),    // Top Right
            PhysicsVector(55f, 745f),    // Bottom Left
            PhysicsVector(745f, 745f)    // Bottom Right
        )
    }

    // Players
    var players = mutableListOf<Player>()
    var activePlayerIndex = 0

    // Coins Board Layout
    var coins = mutableListOf<Coin>()
    var striker: Coin = createStriker()

    // Game state rules
    var isStrikerPlaced = true
    var isAiming = false
    var aimStart = PhysicsVector(0f, 0f)
    var aimCurrent = PhysicsVector(0f, 0f)
    var isMoving = false

    // Historical Queen Rules state
    var queenPocketedThisTurn = false
    var queenWaitingForCover = false
    var queenCoverPlayerIndex: Int? = null
    var queenInPocketTemp: Coin? = null

    // Tracking shots & results
    var blackCoinsLeft = 9
    var whiteCoinsLeft = 9
    var queenLeft = true
    var matchLogs = mutableListOf<String>()
    var turnScoreDelta = 0
    var playerPocketedAnyThisTurn = false
    var foulOccurred = false
    var gameCompleted = false
    var winnerName = ""

    // Particles for collection effects
    var particles = mutableListOf<Particle>()

    init {
        resetGame()
    }

    private fun createStriker(): Coin {
        val yPos = getBaselineY(activePlayerIndex)
        val xPos = BOARD_SIZE / 2f
        return Coin(
            id = 999,
            x = xPos,
            y = yPos,
            vx = 0f,
            vy = 0f,
            mass = STRIKER_MASS,
            radius = STRIKER_RADIUS,
            type = CoinType.STRIKER
        )
    }

    fun getBaselineY(playerIdx: Int): Float {
        return when (playerIdx) {
            0 -> 640f // Bottom
            1 -> 160f // Top (in 2-player or clockwise 4-player)
            2 -> {
                if (numberOfPlayers == 3) 160f // Top-Right alternative
                else 400f // Side for 4-player Left or Top depending on configuration.
            }
            3 -> 400f
            else -> 640f
        }
    }

    fun resetGame() {
        players.clear()
        for (i in 0 until numberOfPlayers) {
            val name = if (i < playerNames.size) playerNames[i] else "Player ${i + 1}"
            players.add(Player(id = i, name = name))
        }
        activePlayerIndex = 0
        isMoving = false
        isStrikerPlaced = true
        isAiming = false
        gameCompleted = false
        winnerName = ""
        queenPocketedThisTurn = false
        queenWaitingForCover = false
        queenCoverPlayerIndex = null
        queenInPocketTemp = null
        blackCoinsLeft = 9
        whiteCoinsLeft = 9
        queenLeft = true
        matchLogs.clear()
        matchLogs.add("Match initialized. ${numberOfPlayers} players ready.")
        particles.clear()

        initializeCoins()
        resetStrikerForPlayer(0)
    }

    fun initializeCoins() {
        coins.clear()
        var coinId = 1

        // Center position is (400, 400)
        val cx = 400f
        val cy = 400f

        // Place Red Queen in exact center
        coins.add(Coin(coinId++, cx, cy, 0f, 0f, COIN_MASS, COIN_RADIUS, CoinType.QUEEN))

        // Hexagonal placement around Queen
        // First ring: 6 coins. Distance = 2.05 * COIN_RADIUS to let them sit nicely touch-by-touch
        val dist = COIN_RADIUS * 2.05f
        for (i in 0 until 6) {
            val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
            val x = cx + cos(angle) * dist
            val y = cy + sin(angle) * dist
            val type = if (i % 2 == 0) CoinType.WHITE else CoinType.BLACK
            coins.add(Coin(coinId++, x, y, 0f, 0f, COIN_MASS, COIN_RADIUS, type))
        }

        // Second ring: 12 coins. Distance = 2 * dist
        val dist2 = dist * 2f
        for (i in 0 until 12) {
            val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
            val x = cx + cos(angle) * dist2
            val y = cy + sin(angle) * dist2
            // Alternative distribution of Whites/Blacks on outer circle
            val type = if (i % 2 == 0) CoinType.BLACK else CoinType.WHITE
            coins.add(Coin(coinId++, x, y, 0f, 0f, COIN_MASS, COIN_RADIUS, type))
        }

        // Sync counts
        whiteCoinsLeft = coins.count { it.type == CoinType.WHITE }
        blackCoinsLeft = coins.count { it.type == CoinType.BLACK }
        queenLeft = true
    }

    fun resetStrikerForPlayer(playerIdx: Int) {
        val baseline = getBaselineY(playerIdx)
        // Set striker at standard position along baseline
        when (playerIdx) {
            0 -> { // Bottom
                striker.x = BOARD_SIZE / 2f
                striker.y = 640f
            }
            1 -> { // Top
                striker.x = BOARD_SIZE / 2f
                striker.y = 160f
            }
            2 -> { // Left or Top-Left
                if (numberOfPlayers == 4) {
                    striker.x = 160f
                    striker.y = BOARD_SIZE / 2f
                } else {
                    striker.x = 250f
                    striker.y = 160f
                }
            }
            3 -> { // Right
                striker.x = 640f
                striker.y = BOARD_SIZE / 2f
            }
        }
        striker.vx = 0f
        striker.vy = 0f
        striker.isPocketed = false
        striker.scale = 1.0f
        striker.opacity = 1.0f
        isStrikerPlaced = true
        isMoving = false
        isAiming = false
    }

    fun updateStrikerPlacement(positionFraction: Float) {
        if (!isStrikerPlaced || isMoving) return
        
        // Map 0.0 .. 1.0 slider to available baseline length
        val startX = 180f
        val endX = 620f
        val pos = startX + (endX - startX) * positionFraction

        when (activePlayerIndex) {
            0 -> { // Bottom
                striker.x = pos
                striker.y = 640f
            }
            1 -> { // Top
                striker.x = pos
                striker.y = 160f
            }
            2 -> {
                if (numberOfPlayers == 4) { // Left baseline
                    striker.x = 160f
                    striker.y = pos
                } else { // Top-Right aspect
                    striker.x = pos
                    striker.y = 160f
                }
            }
            3 -> { // Right baseline
                striker.x = 640f
                striker.y = pos
            }
        }
    }

    fun launchStriker(powerX: Float, powerY: Float) {
        if (!isStrikerPlaced || isMoving) return
        
        // Launch striker
        striker.vx = powerX
        striker.vy = powerY
        isStrikerPlaced = false
        isMoving = true
        playerPocketedAnyThisTurn = false
        foulOccurred = false
        queenPocketedThisTurn = false
        turnScoreDelta = 0
    }

    // Single step physics update tick. Run inside Compose Coroutine Canvas loop
    fun updatePhysicsTick() {
        var anyMoved = false

        // Check if striker is active
        if (!isStrikerPlaced && !striker.isPocketed) {
            striker.updatePosition(FRICTION)
            handleBorderBounce(striker)
            if (striker.vx != 0f || striker.vy != 0f) {
                anyMoved = true
            }
        }

        // Update coins
        for (coin in coins) {
            if (!coin.isPocketed) {
                coin.updatePosition(FRICTION)
                handleBorderBounce(coin)
                if (coin.vx != 0f || coin.vy != 0f) {
                    anyMoved = true
                }
            }
        }

        // If something animated sinking, it's also "moving" or updates visual
        updatePocketSinking()

        // Handle coin collisions
        handleCollisions()

        // Handle pocket detection
        handlePocketIntersections()

        // Handle particle updates
        updateParticles()

        // Transition turn if everything completely stops moving
        if (isMoving && !anyMoved && particles.isEmpty() && coins.none { it.scale < 1.0f && !it.isPocketed }) {
            isMoving = false
            concludeTurn()
        }
    }

    private fun handleBorderBounce(coin: Coin) {
        val minLimit = PLAYABLE_MARGIN + coin.radius
        val maxLimit = BOARD_SIZE - PLAYABLE_MARGIN - coin.radius

        val bounceElasticity = 0.88f // Loss of momentum on elastic bounce

        if (coin.x < minLimit) {
            coin.x = minLimit
            coin.vx = -coin.vx * bounceElasticity
        } else if (coin.x > maxLimit) {
            coin.x = maxLimit
            coin.vx = -coin.vx * bounceElasticity
        }

        if (coin.y < minLimit) {
            coin.y = minLimit
            coin.vy = -coin.vy * bounceElasticity
        } else if (coin.y > maxLimit) {
            coin.y = maxLimit
            coin.vy = -coin.vy * bounceElasticity
        }
    }

    private fun handleCollisions() {
        val allEntities = mutableListOf<Coin>()
        if (!isStrikerPlaced && !striker.isPocketed) {
            allEntities.add(striker)
        }
        allEntities.addAll(coins.filter { !it.isPocketed })

        // Check each pair
        for (i in 0 until allEntities.size) {
            for (j in i + 1 until allEntities.size) {
                val c1 = allEntities[i]
                val c2 = allEntities[j]

                val dx = c2.x - c1.x
                val dy = c2.y - c1.y
                val dist = sqrt(dx * dx + dy * dy)
                val minDist = c1.radius + c2.radius

                if (dist < minDist) {
                    // Overlap occurrence! Push them apart
                    val overlap = minDist - dist
                    val nx = dx / if (dist > 0f) dist else 1f
                    val ny = dy / if (dist > 0f) dist else 1f

                    // Displace relative to mass ratio
                    val totalMass = c1.mass + c2.mass
                    val ratio1 = c2.mass / totalMass
                    val ratio2 = c1.mass / totalMass

                    c1.x -= nx * overlap * ratio1
                    c1.y -= ny * overlap * ratio1
                    c2.x += nx * overlap * ratio2
                    c2.y += ny * overlap * ratio2

                    // Calculate 2D Elastic Rebound vectors
                    val kx = c1.vx - c2.vx
                    val ky = c1.vy - c2.vy
                    val p = 2f * (nx * kx + ny * ky) / totalMass

                    val coeffOfRestitution = 0.90f // Energy retention for hard resin striker-coin impact

                    c1.vx -= p * c2.mass * nx * coeffOfRestitution
                    c1.vy -= p * c2.mass * ny * coeffOfRestitution
                    c2.vx += p * c1.mass * nx * coeffOfRestitution
                    c2.vy += p * c1.mass * ny * coeffOfRestitution
                }
            }
        }
    }

    private fun handlePocketIntersections() {
        // Check striker
        if (!isStrikerPlaced && !striker.isPocketed) {
            for (pocket in POCKETS) {
                val dx = striker.x - pocket.x
                val dy = striker.y - pocket.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < POCKET_RADIUS) {
                    triggerStreakParticles(striker.x, striker.y, Color.Red)
                    striker.isPocketed = true
                    striker.vx = 0f
                    striker.vy = 0f
                    foulOccurred = true
                    Log.d("Carrom", "Striker pocketed! Foul!")
                }
            }
        }

        // Check coins
        for (coin in coins) {
            if (!coin.isPocketed && coin.scale == 1.0f) {
                for (pocket in POCKETS) {
                    val dx = coin.x - pocket.x
                    val dy = coin.y - pocket.y
                    val dist = sqrt(dx * dx + dy * dy)
                    if (dist < POCKET_RADIUS) {
                        coin.vx = 0f
                        coin.vy = 0f
                        // Start shrinking animation
                        coin.scale = 0.99f // Flag that it is shrinking
                        Log.d("Carrom", "Coin ${coin.id} (${coin.type}) sinking in pocket!")
                    }
                }
            }
        }
    }

    private fun updatePocketSinking() {
        for (coin in coins) {
            if (coin.scale < 1.0f && !coin.isPocketed) {
                coin.scale -= 0.12f // Shrink rapidly
                coin.opacity -= 0.12f
                if (coin.scale <= 0.1f) {
                    coin.isPocketed = true
                    coin.scale = 0f
                    coin.opacity = 0f
                    onCoinCollected(coin)
                }
            }
        }
    }

    private fun onCoinCollected(coin: Coin) {
        val player = players[activePlayerIndex]
        playerPocketedAnyThisTurn = true

        val coinColor = getCoinColorForTheme(coin.type)
        triggerStreakParticles(coin.x, coin.y, coinColor)

        // Rule assessment
        when (coin.type) {
            CoinType.WHITE -> {
                player.score += 10
                turnScoreDelta += 10
                whiteCoinsLeft = coins.count { it.type == CoinType.WHITE && !it.isPocketed }
                matchLogs.add("${player.name} pocketed a White Coin (+10 pts).")
                // Track achievement
                player.coinsCollected++
            }
            CoinType.BLACK -> {
                player.score += 5
                turnScoreDelta += 5
                blackCoinsLeft = coins.count { it.type == CoinType.BLACK && !it.isPocketed }
                matchLogs.add("${player.name} pocketed a Black Coin (+5 pts).")
                player.coinsCollected++
            }
            CoinType.QUEEN -> {
                queenLeft = false
                queenPocketedThisTurn = true
                queenInPocketTemp = coin
                matchLogs.add("${player.name} pocketed the Red Queen! Must pocket a 'cover' coin next shot.")
            }
            else -> {}
        }

        // Check if there are no coins left
        val playableLeft = coins.count { (it.type == CoinType.WHITE || it.type == CoinType.BLACK || it.type == CoinType.QUEEN) && !it.isPocketed }
        if (playableLeft == 0 && !queenWaitingForCover) {
            endMatch()
        }
    }

    private fun getCoinColorForTheme(type: CoinType): Color {
        return when (type) {
            CoinType.WHITE -> coinTheme.whiteCoinColor
            CoinType.BLACK -> coinTheme.blackCoinColor
            CoinType.QUEEN -> coinTheme.queenCoinColor
            else -> Color.Yellow
        }
    }

    private fun concludeTurn() {
        val player = players[activePlayerIndex]

        // Handle Queen Cover check
        if (queenWaitingForCover) {
            if (activePlayerIndex == queenCoverPlayerIndex) {
                if (playerPocketedAnyThisTurn && !foulOccurred) {
                    // Queen cover successful! Get points!
                    player.score += 25
                    turnScoreDelta += 25
                    queenWaitingForCover = false
                    queenCoverPlayerIndex = null
                    queenInPocketTemp = null
                    matchLogs.add("${player.name} successfully COVERED the Queen! (+25 pts).")
                } else {
                    // Cover failed! Queen returned to center circle
                    queenWaitingForCover = false
                    queenCoverPlayerIndex = null
                    val q = queenInPocketTemp ?: coins.first { it.type == CoinType.QUEEN }
                    q.isPocketed = false
                    q.scale = 1.0f
                    q.opacity = 1.0f
                    q.x = BOARD_SIZE / 2f
                    q.y = BOARD_SIZE / 2f
                    q.vx = 0f
                    q.vy = 0f
                    queenLeft = true
                    matchLogs.add("${player.name} failed to COVER the Queen. Queen returned to center.")
                }
            }
        } else if (queenPocketedThisTurn) {
            // Player just pocketed Queen. Enable cover state
            if (!foulOccurred) {
                queenWaitingForCover = true
                queenCoverPlayerIndex = activePlayerIndex
            } else {
                // Sunk Queen but committed foul. Returns to center
                val q = queenInPocketTemp ?: coins.first { it.type == CoinType.QUEEN }
                q.isPocketed = false
                q.scale = 1.0f
                q.opacity = 1.0f
                q.x = BOARD_SIZE / 2f
                q.y = BOARD_SIZE / 2f
                q.vx = 0f
                q.vy = 0f
                queenLeft = true
                matchLogs.add("${player.name} pocketed Queen but committed a foul. Queen returned to center.")
            }
        }

        if (foulOccurred) {
            // Apply foul penalty: -5 pts and turn switches
            player.score = (player.score - 5).coerceAtLeast(0)
            matchLogs.add("${player.name} committed a FOUL! (-5 pts, turn passed).")
            nextTurn()
        } else if (playerPocketedAnyThisTurn) {
            // Pocketed a valid coin and no foul: Gets another shot!
            matchLogs.add("${player.name} retains turn after collecting coin.")
            resetStrikerForPlayer(activePlayerIndex)
        } else {
            // Pocketed nothing, standard next player turn
            nextTurn()
        }

        // Final completion check
        val coinsLeft = coins.count { (it.type == CoinType.WHITE || it.type == CoinType.BLACK || it.type == CoinType.QUEEN) && !it.isPocketed }
        if (coinsLeft == 0 && !queenWaitingForCover) {
            endMatch()
        }
    }

    private fun nextTurn() {
        activePlayerIndex = (activePlayerIndex + 1) % numberOfPlayers
        resetStrikerForPlayer(activePlayerIndex)
        matchLogs.add("Turn passed to ${players[activePlayerIndex].name}.")
    }

    private fun endMatch() {
        gameCompleted = true
        isMoving = false
        val sorted = players.sortedByDescending { it.score }
        val winningPlayer = sorted.first()
        winnerName = winningPlayer.name
        matchLogs.add("Match completed! Winner: ${winnerName} with ${winningPlayer.score} pts.")
    }

    fun triggerStreakParticles(px: Float, py: Float, color: Color) {
        val count = 25
        for (i in 0 until count) {
            val angle = (Math.random() * 2.0 * Math.PI).toFloat()
            val speed = (2f + Math.random() * 8f).toFloat()
            particles.add(
                Particle(
                    x = px,
                    y = py,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    alpha = 1.0f,
                    size = (4f + Math.random() * 8f).toFloat(),
                    life = 1.0f,
                    decay = (0.02f + Math.random() * 0.04f).toFloat()
                )
            )
        }
    }

    fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.alpha -= p.decay
            p.life -= p.decay
            p.vx *= 0.95f
            p.vy *= 0.95f
            if (p.life <= 0f || p.alpha <= 0f) {
                iterator.remove()
            }
        }
    }
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var alpha: Float,
    val size: Float,
    var life: Float,
    val decay: Float
)
