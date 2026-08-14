package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.BoardTheme
import com.example.model.CoinType
import com.example.model.Player
import com.example.ui.SoundManager
import com.example.ui.components.CarromBoardCanvas
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = viewModel.engine

    val boardTheme = viewModel.boardTheme
    val coinTheme = viewModel.coinTheme

    // Sleek background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SleekBackground,
            Color(0xFF14141E)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. HEADER PANEL (Score lists & Pause elements)
            GameHeaderPanel(
                players = engine.players,
                activeIndex = engine.activePlayerIndex,
                isMoving = engine.isMoving,
                turnTimeRemaining = engine.turnTimeRemaining,
                turnTimeLimit = engine.turnTimeLimitSeconds,
                isTimerEnabled = engine.isTimerEnabled,
                timerWarning = engine.timerWarningActive,
                onPauseToggle = {
                    SoundManager.playStrikeSound()
                    viewModel.togglePause()
                },
                onRestartClick = {
                    SoundManager.playStrikeSound()
                    viewModel.restartCurrentGame()
                }
            )

            // 1B. TURN COUNTDOWN PROGRESS STRIP
            if (engine.isTimerEnabled && engine.turnTimeLimitSeconds > 0f && !engine.gameCompleted) {
                TurnTimerCountdownStrip(
                    engine = engine,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // 2. CORE CARROM BOARD CANVAS VIEW BOUNDS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                CarromBoardCanvas(
                    engine = engine,
                    strikerPlacedPositionFraction = viewModel.strikerPositionFraction,
                    powerBoostMultiplier = viewModel.powerBoostMultiplier,
                    onAimStart = { offset ->
                        // State transition if needed
                    },
                    onAimDrag = { offset ->
                        // Tracking drag previews
                    },
                    onAimRelease = { offset, power ->
                        // Launch handled internally inside canvas via engine release vectors
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("carrom_board_canvas")
                )
            }

            // 3. STRIKER POWER BOOST SELECTOR & BASELINE POSITIONING SLIDER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Power Boost Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SHOT POWER:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val boosts = listOf(
                            1.0f to "1.0x",
                            1.5f to "⚡ 1.5x",
                            2.0f to "💥 2.0x"
                        )
                        boosts.forEach { (boostVal, label) ->
                            val isSelected = viewModel.powerBoostMultiplier == boostVal
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    SoundManager.playStrikeSound()
                                    viewModel.setPowerBoost(boostVal)
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else SleekTextPrimary
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (boostVal == 2.0f) SleekOrange else SleekAmber,
                                    containerColor = SleekSurface
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) SleekOrange else SleekSurfaceBorder
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activePlayer = engine.players.getOrNull(engine.activePlayerIndex)
                    Text(
                        text = "POSITION: ${activePlayer?.name ?: ""}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextSecondary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = if (engine.isMoving) "COINS IN MOTION..." else "PULL BACK TO AIM & FIRE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (engine.isMoving) SleekOrangeLight else SleekAmber
                    )
                }

                // Slider element
                Slider(
                    value = viewModel.strikerPositionFraction,
                    onValueChange = {
                        if (!engine.isMoving && !engine.gameCompleted) {
                            viewModel.changeStrikerPosition(it)
                        }
                    },
                    valueRange = 0f..1f,
                    enabled = !engine.isMoving && !engine.gameCompleted,
                    colors = SliderDefaults.colors(
                        thumbColor = SleekOrange,
                        activeTrackColor = SleekOrange.copy(alpha = 0.5f),
                        inactiveTrackColor = SleekSurfaceBorder
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("striker_position_slider")
                )
            }

            // 4. LIVE EVENTS LOG PANELS
            MatchEventsLogScroller(
                logs = engine.matchLogs,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .padding(horizontal = 20.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Live Queen alert overlay banner
        if (engine.queenWaitingForCover) {
            val qPlayer = engine.players.getOrNull(engine.queenCoverPlayerIndex ?: 0)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 110.dp, start = 20.dp, end = 20.dp)
                    .background(SleekAmber, shape = MaterialTheme.shapes.medium)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "QUEEN COVER ACTIVE: ${qPlayer?.name ?: ""} must pocket ANY coin next shot to claim +25 pts!",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlays
        // A. PAUSE MODAL DIALOG
        if (viewModel.isPaused) {
            PauseOverlayDialog(
                onResume = { viewModel.togglePause() },
                onRestart = { viewModel.restartCurrentGame() },
                onExit = {
                    SoundManager.playStrikeSound()
                    viewModel.navigateTo(AppScreen.MENU)
                }
            )
        }

        // B. GAME COMPLETED SUCCESS DIALOG
        if (engine.gameCompleted) {
            VictoryOverlayDialog(
                winnerName = engine.winnerName,
                players = engine.players,
                winnerScore = engine.players.maxOfOrNull { it.score } ?: 0,
                onRestart = { viewModel.restartCurrentGame() },
                onExit = {
                    SoundManager.playStrikeSound()
                    viewModel.navigateTo(AppScreen.MENU)
                }
            )
        }
    }
}

@Composable
fun GameHeaderPanel(
    players: List<Player>,
    activeIndex: Int,
    isMoving: Boolean,
    turnTimeRemaining: Float = 0f,
    turnTimeLimit: Float = 0f,
    isTimerEnabled: Boolean = false,
    timerWarning: Boolean = false,
    onPauseToggle: () -> Unit,
    onRestartClick: () -> Unit
) {
    // Pulse animation for urgent countdown under 4 seconds
    val infiniteTransition = rememberInfiniteTransition(label = "timer_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SleekBackground.copy(alpha = 0.6f))
            .border(BorderStroke(1.dp, SleekSurfaceBorder))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Custom layout rows of players in session
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chunks = players.chunked(2)
            chunks.forEach { rowPlayers ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowPlayers.forEach { player ->
                        val isTurn = players.indexOf(player) == activeIndex

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isTurn) {
                                    if (timerWarning) Color(0xFFFF1744).copy(alpha = 0.18f * pulseAlpha)
                                    else SleekOrange.copy(alpha = 0.12f)
                                } else SleekSurface.copy(alpha = 0.4f)
                            ),
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isTurn) {
                                        if (timerWarning) Color(0xFFFF1744).copy(alpha = pulseAlpha)
                                        else SleekOrange
                                    } else Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Turn glowing indicator dot
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                if (isTurn) {
                                                    if (timerWarning) Color(0xFFFF1744) else SleekOrange
                                                } else SleekTextMuted,
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = player.name,
                                        fontSize = 11.sp,
                                        color = if (isTurn) {
                                            if (timerWarning) Color(0xFFFF5252) else SleekOrange
                                        } else SleekTextPrimary,
                                        fontWeight = if (isTurn) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isTurn && isTimerEnabled && turnTimeLimit > 0f) {
                                        Surface(
                                            color = if (timerWarning) Color(0xFFFF1744).copy(alpha = 0.3f) else SleekSurfaceBorder.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = "${turnTimeRemaining.toInt().coerceAtLeast(0)}s",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (timerWarning) Color(0xFFFF5252) else SleekOrangeLight,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${player.score} pts",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isTurn) SleekOrange else SleekTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Right side: Game commands
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconButton(
                onClick = onRestartClick,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = SleekSurface
                ),
                modifier = Modifier
                    .size(38.dp)
                    .border(1.dp, SleekSurfaceBorder, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart match",
                    tint = SleekTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onPauseToggle,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = SleekOrange
                ),
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Pause,
                    contentDescription = "Pause match",
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun TurnTimerCountdownStrip(
    engine: com.example.model.CarromEngine,
    modifier: Modifier = Modifier
) {
    val activePlayer = engine.players.getOrNull(engine.activePlayerIndex)
    val remaining = engine.turnTimeRemaining
    val limit = engine.turnTimeLimitSeconds
    val isWarning = engine.timerWarningActive
    val progress = if (limit > 0f) (remaining / limit).coerceIn(0f, 1f) else 1f

    val infiniteTransition = rememberInfiniteTransition(label = "strip_warning_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val barColor = when {
        isWarning -> Color(0xFFFF1744)
        progress < 0.4f -> SleekOrange
        progress < 0.7f -> SleekAmber
        else -> Color(0xFF00E676)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 80, easing = LinearEasing),
        label = "turn_progress"
    )

    Surface(
        color = if (isWarning) Color(0xFFFF1744).copy(alpha = 0.12f * pulseAlpha) else SleekSurface.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isWarning) Color(0xFFFF1744).copy(alpha = pulseAlpha) else SleekSurfaceBorder
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.Timer,
                        contentDescription = "Turn Timer",
                        tint = barColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isWarning) {
                            "⚠️ TIME RUNNING OUT: ${activePlayer?.name ?: "Player"}"
                        } else if (engine.isMoving) {
                            "🎯 SHOT IN MOTION..."
                        } else {
                            "⏱ ${activePlayer?.name ?: "Player"}'s Turn"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isWarning) Color(0xFFFF5252) else SleekTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (engine.isMoving) "WAIT" else "${remaining.toInt().coerceAtLeast(0)}s left",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = barColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(-5 pts penalty)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = SleekTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Smooth linear countdown track
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = barColor,
                trackColor = SleekSurfaceBorder.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MatchEventsLogScroller(
    logs: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto scroll down to latest log
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder),
        shape = MaterialTheme.shapes.medium
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(logs) { log ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "•",
                            color = SleekOrange,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = log,
                            color = SleekTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PauseOverlayDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, SleekSurfaceBorder),
            colors = CardDefaults.cardColors(
                containerColor = SleekSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PauseCircleFilled,
                    contentDescription = null,
                    tint = SleekOrange,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    "MATCH PAUSED",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary,
                    letterSpacing = 1.sp
                )

                Text(
                    "Take a break, stretch your fingers, and resume when ready.",
                    fontSize = 12.sp,
                    color = SleekTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons list
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekOrange)
                ) {
                    Text("RESUME MATCH", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, SleekOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextPrimary)
                ) {
                    Text("RESTART BATTLE", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("QUIT TO MENU", color = SleekOrangeLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VictoryOverlayDialog(
    winnerName: String,
    players: List<Player>,
    winnerScore: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, SleekOrange, shape = MaterialTheme.shapes.large),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = SleekSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Celebration Icon
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = SleekAmber,
                    modifier = Modifier.size(72.dp)
                )

                Text(
                    "CHAMPION CROWNED!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = SleekTextPrimary,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                // Winner summary box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SleekBackground.copy(alpha = 0.5f), shape = MaterialTheme.shapes.medium)
                        .border(1.dp, SleekSurfaceBorder, shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = winnerName.uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = SleekOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Victor of the Duel with $winnerScore Points",
                            fontSize = 11.sp,
                            color = SleekTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Final tally leaderboard
                Text(
                    "FINAL MATCH SCORE TALLY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    players.sortedByDescending { it.score }.forEachIndexed { idx, player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SleekBackground.copy(alpha = 0.6f), shape = MaterialTheme.shapes.small)
                                .border(1.dp, SleekSurfaceBorder, shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}. ${player.name}",
                                fontSize = 13.sp,
                                color = if (player.name == winnerName) SleekOrange else SleekTextPrimary,
                                fontWeight = if (player.name == winnerName) FontWeight.Bold else FontWeight.Medium
                            )

                            Text(
                                text = "${player.score} pts",
                                fontSize = 13.sp,
                                color = SleekTextPrimary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SleekOrange)
                ) {
                    Text("PLAY AGAIN", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onExit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    border = BorderStroke(1.dp, SleekOrange),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekTextPrimary)
                ) {
                    Text("MAIN MENU", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
