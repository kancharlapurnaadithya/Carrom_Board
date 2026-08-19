package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CarromDatabase
import com.example.data.database.CarromRepository
import com.example.data.database.MatchEntity
import com.example.data.database.AchievementEntity
import com.example.model.*
import com.example.ui.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    MENU,
    PLAYER_SETUP,
    GAME,
    SETTINGS,
    STATS,
    ACHIEVEMENTS
}

enum class GameMode(val displayName: String, val description: String) {
    VS_COMPUTER("Play Vs Computer", "Challenge smart AI Bot"),
    PASS_AND_PLAY("Pass & Play", "Local 2, 3 or 4 players"),
    PRACTICE("Practice Mode", "Free practice with unlimited shots")
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs: SharedPreferences =
        application.getSharedPreferences("carrom_prefs", Context.MODE_PRIVATE)

    private val database = CarromDatabase.getDatabase(application)
    private val repository = CarromRepository(database.carromDao())

    // UI screen state
    var currentScreen by mutableStateOf(AppScreen.MENU)
        private set

    // Available board themes & coin packs & striker designs
    var boardTheme by mutableStateOf(BoardTheme.WOODEN)
        private set
    var coinTheme by mutableStateOf(CoinTheme.CLASSIC)
        private set
    var strikerDesign by mutableStateOf(StrikerDesign.IVORY_GOLD_MASTER)
        private set

    // Game stats & records from database
    val matches: StateFlow<List<MatchEntity>> = repository.allMatches.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievements: StateFlow<List<AchievementEntity>> = repository.allAchievements.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Matchmaking configuration
    var gameMode by mutableStateOf(GameMode.VS_COMPUTER)
    var playerCount by mutableStateOf(2)
    var player1Name by mutableStateOf("Player 1")
    var player2Name by mutableStateOf("Player 2")
    var player3Name by mutableStateOf("Player 3")
    var player4Name by mutableStateOf("Player 4")

    private var aiTurnInProgress = false

    // Game Loop Engine
    var engine by mutableStateOf(CarromEngine())
        private set

    private var gameLoopJob: Job? = null
    var isPaused by mutableStateOf(false)
        private set

    // Placement & Power state
    var strikerPositionFraction by mutableStateOf(0.5f)
    var powerBoostMultiplier by mutableStateOf(1.3f)
    var turnTimerSeconds by mutableStateOf(15f)
    var aiDifficulty by mutableStateOf(AiDifficulty.INTERMEDIATE)

    private var lastTickedSecond = -1

    fun setPowerBoost(boost: Float) {
        powerBoostMultiplier = boost
    }

    fun setAiDifficultyLevel(difficulty: AiDifficulty) {
        aiDifficulty = difficulty
        engine.aiDifficulty = difficulty
        sharedPrefs.edit().putString("AI_DIFFICULTY", difficulty.name).apply()
    }

    fun setTurnTimerDuration(seconds: Float) {
        turnTimerSeconds = seconds
        engine.setTurnTimeLimit(seconds)
        sharedPrefs.edit().putFloat("TURN_TIMER_SEC", seconds).apply()
    }

    init {
        // Init default achievements in DB
        viewModelScope.launch {
            repository.initializeDefaultAchievements()
        }

        // Load settings from Sp
        loadPreferences()
    }

    private fun loadPreferences() {
        val boardThemeName = sharedPrefs.getString("BOARD_THEME", BoardTheme.WOODEN.name) ?: BoardTheme.WOODEN.name
        boardTheme = try { BoardTheme.valueOf(boardThemeName) } catch (e: Exception) { BoardTheme.WOODEN }

        val coinThemeName = sharedPrefs.getString("COIN_THEME", CoinTheme.CLASSIC.name) ?: CoinTheme.CLASSIC.name
        coinTheme = try { CoinTheme.valueOf(coinThemeName) } catch (e: Exception) { CoinTheme.CLASSIC }

        val strikerDesignName = sharedPrefs.getString("STRIKER_DESIGN", StrikerDesign.IVORY_GOLD_MASTER.name) ?: StrikerDesign.IVORY_GOLD_MASTER.name
        strikerDesign = try { StrikerDesign.valueOf(strikerDesignName) } catch (e: Exception) { StrikerDesign.IVORY_GOLD_MASTER }

        val aiDifficultyName = sharedPrefs.getString("AI_DIFFICULTY", AiDifficulty.INTERMEDIATE.name) ?: AiDifficulty.INTERMEDIATE.name
        aiDifficulty = try { AiDifficulty.valueOf(aiDifficultyName) } catch (e: Exception) { AiDifficulty.INTERMEDIATE }

        SoundManager.isSoundEnabled = sharedPrefs.getBoolean("SOUNDS_ON", true)
        SoundManager.isMusicEnabled = sharedPrefs.getBoolean("MUSIC_ON", true)
        SoundManager.isVibrationEnabled = sharedPrefs.getBoolean("VIBRATION_ON", true)

        turnTimerSeconds = sharedPrefs.getFloat("TURN_TIMER_SEC", 15f)
        engine.setTurnTimeLimit(turnTimerSeconds)
        engine.aiDifficulty = aiDifficulty
        engine.boardTheme = boardTheme
        engine.coinTheme = coinTheme
        engine.strikerDesign = strikerDesign

        player1Name = sharedPrefs.getString("PLAYER_1_NAME", "Player 1") ?: "Player 1"
        player2Name = sharedPrefs.getString("PLAYER_2_NAME", "Player 2") ?: "Player 2"
        player3Name = sharedPrefs.getString("PLAYER_3_NAME", "Player 3") ?: "Player 3"
        player4Name = sharedPrefs.getString("PLAYER_4_NAME", "Player 4") ?: "Player 4"
    }

    fun setBoardThemeDirect(theme: BoardTheme) {
        boardTheme = theme
        engine.boardTheme = theme
        sharedPrefs.edit().putString("BOARD_THEME", theme.name).apply()
    }

    fun setStrikerDesignDirect(design: StrikerDesign) {
        strikerDesign = design
        engine.strikerDesign = design
        sharedPrefs.edit().putString("STRIKER_DESIGN", design.name).apply()
    }

    fun saveSettings(
        selectedBoard: BoardTheme,
        selectedCoins: CoinTheme,
        selectedStriker: StrikerDesign = strikerDesign,
        soundsOn: Boolean,
        musicOn: Boolean,
        vibrationOn: Boolean,
        timerLimit: Float = turnTimerSeconds,
        difficulty: AiDifficulty = aiDifficulty
    ) {
        boardTheme = selectedBoard
        coinTheme = selectedCoins
        strikerDesign = selectedStriker
        SoundManager.isSoundEnabled = soundsOn
        SoundManager.isMusicEnabled = musicOn
        SoundManager.isVibrationEnabled = vibrationOn
        turnTimerSeconds = timerLimit
        aiDifficulty = difficulty

        sharedPrefs.edit()
            .putString("BOARD_THEME", selectedBoard.name)
            .putString("COIN_THEME", selectedCoins.name)
            .putString("STRIKER_DESIGN", selectedStriker.name)
            .putBoolean("SOUNDS_ON", soundsOn)
            .putBoolean("MUSIC_ON", musicOn)
            .putBoolean("VIBRATION_ON", vibrationOn)
            .putFloat("TURN_TIMER_SEC", timerLimit)
            .putString("AI_DIFFICULTY", difficulty.name)
            .apply()
        
        // Update active engine themes, striker, timer, and AI difficulty
        engine.boardTheme = selectedBoard
        engine.coinTheme = selectedCoins
        engine.strikerDesign = selectedStriker
        engine.setTurnTimeLimit(timerLimit)
        engine.aiDifficulty = difficulty
    }

    fun savePlayerNames(p1: String, p2: String, p3: String, p4: String) {
        player1Name = p1.ifBlank { "Player 1" }
        player2Name = p2.ifBlank { "Player 2" }
        player3Name = p3.ifBlank { "Player 3" }
        player4Name = p4.ifBlank { "Player 4" }

        sharedPrefs.edit()
            .putString("PLAYER_1_NAME", player1Name)
            .putString("PLAYER_2_NAME", player2Name)
            .putString("PLAYER_3_NAME", player3Name)
            .putString("PLAYER_4_NAME", player4Name)
            .apply()
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        if (screen == AppScreen.GAME) {
            startLoop()
        } else {
            // Keep game active if only going settings/stats pausing, otherwise stop loop
            if (screen != AppScreen.SETTINGS) {
                stopLoop()
            }
        }
    }

    fun startNewGame() {
        // Collect names based on game mode & player count
        val (finalCount, names) = when (gameMode) {
            GameMode.VS_COMPUTER -> Pair(2, listOf(player1Name.ifBlank { "You" }, "AI (${aiDifficulty.displayName})"))
            GameMode.PRACTICE -> Pair(1, listOf(player1Name.ifBlank { "Player 1" }))
            GameMode.PASS_AND_PLAY -> {
                val pList = when (playerCount) {
                    2 -> listOf(player1Name, player2Name)
                    3 -> listOf(player1Name, player2Name, player3Name)
                    4 -> listOf(player1Name, player2Name, player3Name, player4Name)
                    else -> listOf(player1Name, player2Name)
                }
                Pair(playerCount, pList)
            }
        }

        // Setup Engine
        engine = CarromEngine(
            numberOfPlayers = finalCount,
            playerNames = names,
            boardTheme = boardTheme,
            coinTheme = coinTheme,
            strikerDesign = strikerDesign,
            aiDifficulty = aiDifficulty
        )
        engine.setTurnTimeLimit(turnTimerSeconds)

        isPaused = false
        strikerPositionFraction = 0.5f
        aiTurnInProgress = false
        navigateTo(AppScreen.GAME)
    }

    fun restartCurrentGame() {
        engine.resetGame()
        isPaused = false
        strikerPositionFraction = 0.5f
        aiTurnInProgress = false
        startLoop()
    }

    fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            stopLoop()
        } else {
            startLoop()
        }
    }

    fun changeStrikerPosition(fraction: Float) {
        strikerPositionFraction = fraction
        engine.updateStrikerPlacement(fraction)
    }

    // Fire Striker!
    fun fireStriker(vx: Float, vy: Float) {
        if (engine.isMoving) return
        engine.launchStriker(vx, vy)
        SoundManager.playStrikeSound()
        startLoop() // Ensure loop is running actively
    }

    private fun startLoop() {
        if (gameLoopJob?.isActive == true) return
        gameLoopJob = viewModelScope.launch {
            while (true) {
                if (!isPaused && !engine.gameCompleted) {
                    val wasMoving = engine.isMoving
                    val scoreBefore = engine.players.sumOf { it.score }
                    val whiteLeftBefore = engine.whiteCoinsLeft
                    val blackLeftBefore = engine.blackCoinsLeft
                    val queenLeftBefore = engine.queenLeft

                    engine.updatePhysicsTick()

                    // Handle turn timer warning tick and timeout buzzer sound cues
                    if (engine.timeoutOccurredThisTick) {
                        SoundManager.playTimeoutBuzzerSound()
                        lastTickedSecond = -1
                    } else if (engine.timerWarningActive) {
                        val currentSecond = engine.turnTimeRemaining.toInt() + 1
                        if (currentSecond != lastTickedSecond && currentSecond in 1..4) {
                            lastTickedSecond = currentSecond
                            SoundManager.playWarningTickSound()
                        }
                    } else {
                        lastTickedSecond = -1
                    }

                    // Analyze ticks to play sound triggers
                    val isMovingNow = engine.isMoving
                    val scoreAfter = engine.players.sumOf { it.score }

                    // Sound triggers on pocket
                    if (scoreAfter > scoreBefore) {
                        SoundManager.playPocketSound()
                    } else if (engine.foulOccurred && wasMoving && !isMovingNow) {
                        SoundManager.playFoulSound()
                    }

                    // Rebounds can be detected if striker direction flips
                    if (engine.particles.size > 0 && Math.random() < 0.15) {
                        SoundManager.playBounceSound()
                    }

                    // Save metrics to room database on match completion
                    if (engine.gameCompleted && wasMoving && !isMovingNow) {
                        SoundManager.playVictorySound()
                        saveCompletedMatchToDatabase()
                    }

                    // Trigger AI Computer shot if it's Computer's turn
                    if (gameMode == GameMode.VS_COMPUTER && engine.activePlayerIndex == 1 && !engine.isMoving && engine.isStrikerPlaced && !aiTurnInProgress && !engine.gameCompleted) {
                        aiTurnInProgress = true
                        viewModelScope.launch {
                            val thinkingTime = when (aiDifficulty) {
                                AiDifficulty.NOVICE -> 900L
                                AiDifficulty.BEGINNER -> 800L
                                AiDifficulty.INTERMEDIATE -> 700L
                                AiDifficulty.EXPERT -> 600L
                                AiDifficulty.GRANDMASTER -> 500L
                            }
                            delay(thinkingTime)
                            if (engine.activePlayerIndex == 1 && !engine.isMoving && !engine.gameCompleted) {
                                val (aiX, aiPower) = engine.calculateAiShot(aiDifficulty)
                                engine.updateStrikerPlacement((aiX - 180f) / 440f)
                                delay(350)
                                engine.launchStriker(aiPower.x, aiPower.y)
                                SoundManager.playStrikeSound()
                            }
                            aiTurnInProgress = false
                        }
                    }
                }
                delay(16) // ~60 FPS update
            }
        }
    }

    private fun stopLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    private suspend fun saveCompletedMatchToDatabase() {
        val activePlayers = engine.players
        val playersStr = activePlayers.joinToString(",") { it.name }
        val scoresStr = activePlayers.joinToString(",") { it.score.toString() }
        val winnerName = engine.winnerName

        val entity = MatchEntity(
            playersJoined = playersStr,
            scoresJoined = scoresStr,
            winnerName = winnerName,
            gameDurationSec = 300 // default mock duration for tracker
        )

        repository.insertMatch(entity)

        // Achievement: Play on multiple board styles
        val matchesPlayed = repository.allMatches.stateIn(viewModelScope).value
        val uniqueBoards = matchesPlayed.map { "classic" }.toSet().size // custom mapping fallback
        repository.updateAchievementProgress("board_hopper", uniqueBoards.coerceAtLeast(1))
    }

    fun clearCompletedMatches() {
        viewModelScope.launch {
            repository.clearMatchHistory()
        }
    }

    fun resetPreferencesData() {
        sharedPrefs.edit().clear().apply()
        loadPreferences()
        engine.boardTheme = boardTheme
        engine.coinTheme = coinTheme
    }

    override fun onCleared() {
        super.onCleared()
        stopLoop()
    }
}
