package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoundManager
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    // Smooth pulsating factor for title and board
    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Deep modern charcoal backgrounds
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
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // 1. Sleek Header: Status Bar & Profile
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile/Name Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(SleekAmber, SleekOrange)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Column {
                        Text(
                            text = "GRANDMASTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekOrange,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Jay Dawson",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary
                        )
                    }
                }

                // Balance Chip Representation
                Row(
                    modifier = Modifier
                        .background(SleekSurface.copy(alpha = 0.6f), shape = RoundedCornerShape(100.dp))
                        .border(1.dp, SleekSurfaceBorder, shape = RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(SleekAmber, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                    Text(
                        text = "24,500",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Custom Game Logo / Header Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "CARROM DUEL",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(SleekAmber, SleekOrange)
                        )
                    )
                )

                Text(
                    text = "OFFLINE CHAMPIONSHIP BOARD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Dynamic Board & Striker Stage Showcase
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SleekSurface.copy(alpha = 0.8f)
                ),
                border = BorderStroke(1.dp, SleekSurfaceBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "EQUIPPED GEAR & STAGE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekOrange,
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = {
                                SoundManager.playStrikeSound()
                                viewModel.navigateTo(AppScreen.SETTINGS)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "CHANGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = SleekTextPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Board Column Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    SoundManager.playStrikeSound()
                                    // Cycle to next board
                                    val boards = com.example.model.BoardTheme.values()
                                    val nextIndex = (viewModel.boardTheme.ordinal + 1) % boards.size
                                    viewModel.setBoardThemeDirect(boards[nextIndex])
                                    SoundManager.triggerVibration(context)
                                }
                                .padding(4.dp)
                        ) {
                            com.example.ui.components.MiniBoardPreview(
                                theme = viewModel.boardTheme,
                                modifier = Modifier
                                    .scale(scaleFactor)
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = viewModel.boardTheme.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tap to switch board",
                                fontSize = 9.sp,
                                color = SleekTextMuted
                            )
                        }

                        // Striker / Stager Column Preview
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    SoundManager.playStrikeSound()
                                    // Cycle to next striker
                                    val strikers = com.example.model.StrikerDesign.values()
                                    val nextIndex = (viewModel.strikerDesign.ordinal + 1) % strikers.size
                                    viewModel.setStrikerDesignDirect(strikers[nextIndex])
                                    SoundManager.triggerVibration(context)
                                }
                                .padding(4.dp)
                        ) {
                            com.example.ui.components.MiniStrikerPreview(
                                design = viewModel.strikerDesign,
                                modifier = Modifier
                                    .scale(scaleFactor)
                                    .size(90.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = viewModel.strikerDesign.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = viewModel.strikerDesign.accentGlowColor,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tap to switch striker",
                                fontSize = 9.sp,
                                color = SleekTextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. Core Buttons & Game Modes Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SELECT GAME MODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                // 1. PLAY VS COMPUTER
                MenuItemButton(
                    title = "PLAY VS COMPUTER",
                    subtitle = "Challenge ${viewModel.aiDifficulty.displayName} AI Bot",
                    icon = Icons.Filled.SmartToy,
                    glowColor = SleekOrange,
                    onClick = {
                        SoundManager.playStrikeSound()
                        viewModel.gameMode = com.example.viewmodel.GameMode.VS_COMPUTER
                        viewModel.startNewGame()
                    },
                    modifier = Modifier.testTag("vs_computer_button")
                )

                // 2. PASS & PLAY
                MenuItemButton(
                    title = "PASS & PLAY (LOCAL)",
                    subtitle = "2, 3, or 4 Players offline session",
                    icon = Icons.Filled.Group,
                    glowColor = SleekAmber,
                    onClick = {
                        SoundManager.playStrikeSound()
                        viewModel.gameMode = com.example.viewmodel.GameMode.PASS_AND_PLAY
                        viewModel.navigateTo(AppScreen.PLAYER_SETUP)
                    },
                    modifier = Modifier.testTag("pass_and_play_button")
                )

                // 3. PRACTICE MODE
                MenuItemButton(
                    title = "PRACTICE MODE",
                    subtitle = "Free practice with unlimited shots",
                    icon = Icons.Filled.SportsEsports,
                    glowColor = SleekOrangeLight,
                    onClick = {
                        SoundManager.playStrikeSound()
                        viewModel.gameMode = com.example.viewmodel.GameMode.PRACTICE
                        viewModel.startNewGame()
                    },
                    modifier = Modifier.testTag("practice_button")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Statistics Button
                    Box(modifier = Modifier.weight(1f)) {
                        MenuItemButtonMini(
                            title = "STATISTICS",
                            icon = Icons.Filled.Leaderboard,
                            color = SleekAmber,
                            onClick = {
                                SoundManager.playStrikeSound()
                                viewModel.navigateTo(AppScreen.STATS)
                            }
                        )
                    }

                    // Achievements Button
                    Box(modifier = Modifier.weight(1f)) {
                        MenuItemButtonMini(
                            title = "TROPHIES",
                            icon = Icons.Filled.EmojiEvents,
                            color = SleekOrangeLight,
                            onClick = {
                                SoundManager.playStrikeSound()
                                viewModel.navigateTo(AppScreen.ACHIEVEMENTS)
                            }
                        )
                    }
                }

                // Settings Button
                MenuItemButton(
                    title = "BOARD CUSTOMIZE",
                    subtitle = "Themes, sound packages, and styles",
                    icon = Icons.Filled.Settings,
                    glowColor = SleekTextSecondary,
                    onClick = {
                        SoundManager.playStrikeSound()
                        viewModel.navigateTo(AppScreen.SETTINGS)
                    },
                    modifier = Modifier.testTag("settings_button")
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer Version Details
            Text(
                text = "v1.0.0 ( offline engine enabled )",
                fontSize = 11.sp,
                color = SleekTextMuted.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )
        }
    }
}

@Composable
fun MenuItemButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    glowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(glowColor.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = glowColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = SleekTextMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
fun MenuItemButtonMini(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.85f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextPrimary,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
