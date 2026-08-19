package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiDifficulty
import com.example.model.BoardTheme
import com.example.model.CoinTheme
import com.example.model.StrikerDesign
import com.example.ui.SoundManager
import com.example.ui.components.MiniBoardPreview
import com.example.ui.components.MiniStrikerPreview
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

enum class SettingsCategory(val title: String, val icon: ImageVector) {
    BOARDS("Boards", Icons.Default.Dashboard),
    STRIKERS("Strikers", Icons.Default.Stars),
    COINS("Coins", Icons.Default.Circle),
    RULES("Rules & Sound", Icons.Default.Tune),
    AI_BOTS("AI Intelligence", Icons.Default.SmartToy)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Local toggles & selection states initialized from View Model
    var localBoard by remember { mutableStateOf(viewModel.boardTheme) }
    var localCoins by remember { mutableStateOf(viewModel.coinTheme) }
    var localStriker by remember { mutableStateOf(viewModel.strikerDesign) }
    var localDifficulty by remember { mutableStateOf(viewModel.aiDifficulty) }
    var soundsOn by remember { mutableStateOf(SoundManager.isSoundEnabled) }
    var musicOn by remember { mutableStateOf(SoundManager.isMusicEnabled) }
    var vibrationOn by remember { mutableStateOf(SoundManager.isVibrationEnabled) }
    var localTimerLimit by remember { mutableStateOf(viewModel.turnTimerSeconds) }

    var selectedTab by remember { mutableStateOf(SettingsCategory.BOARDS) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SleekBackground,
            Color(0xFF14141E)
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "CUSTOMIZE & SETTINGS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SleekTextPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        SoundManager.playStrikeSound()
                        // Auto save changes before navigating back
                        viewModel.saveSettings(localBoard, localCoins, localStriker, soundsOn, musicOn, vibrationOn, localTimerLimit, localDifficulty)
                        viewModel.navigateTo(AppScreen.MENU)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        SoundManager.playStrikeSound()
                        viewModel.saveSettings(localBoard, localCoins, localStriker, soundsOn, musicOn, vibrationOn, localTimerLimit, localDifficulty)
                        SoundManager.triggerVibration(context)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Save settings",
                            tint = SleekOrange
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = SleekBackground
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Category Filter Scrollable Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = SleekOrange,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = SleekOrange,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    SettingsCategory.values().forEach { cat ->
                        val isSelected = selectedTab == cat
                        Tab(
                            selected = isSelected,
                            onClick = {
                                SoundManager.playStrikeSound()
                                selectedTab = cat
                            },
                            text = {
                                Text(
                                    text = cat.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) SleekOrange else SleekTextMuted
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) SleekOrange else SleekTextMuted
                                )
                            }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (selectedTab) {
                        SettingsCategory.BOARDS -> {
                            // 1. UNIQUE CARROM BOARDS GALLERY
                            Text(
                                "SELECT CARROM BOARD (${BoardTheme.values().size} UNIQUE DESIGNS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            BoardTheme.values().forEach { theme ->
                                val isSelected = localBoard == theme
                                BoardSelectionCard(
                                    theme = theme,
                                    isSelected = isSelected,
                                    onClick = {
                                        SoundManager.playStrikeSound()
                                        localBoard = theme
                                        viewModel.setBoardThemeDirect(theme)
                                        SoundManager.triggerVibration(context)
                                    }
                                )
                            }
                        }

                        SettingsCategory.STRIKERS -> {
                            // 2. UNIQUE STRIKERS GALLERY
                            Text(
                                "SELECT STRIKER DESIGN (${StrikerDesign.values().size} UNIQUE STRIKERS)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            StrikerDesign.values().forEach { striker ->
                                val isSelected = localStriker == striker
                                StrikerSelectionCard(
                                    striker = striker,
                                    isSelected = isSelected,
                                    onClick = {
                                        SoundManager.playStrikeSound()
                                        localStriker = striker
                                        viewModel.setStrikerDesignDirect(striker)
                                        SoundManager.triggerVibration(context)
                                    }
                                )
                            }
                        }

                        SettingsCategory.COINS -> {
                            // 3. COIN PALETTES
                            Text(
                                "COIN PIECE PALETTES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            CoinTheme.values().forEach { cTheme ->
                                val isSelected = localCoins == cTheme
                                CoinSelectionCard(
                                    title = cTheme.displayName,
                                    whiteColor = cTheme.whiteCoinColor,
                                    blackColor = cTheme.blackCoinColor,
                                    queenColor = cTheme.queenCoinColor,
                                    isSelected = isSelected,
                                    onClick = {
                                        SoundManager.playStrikeSound()
                                        localCoins = cTheme
                                        viewModel.saveSettings(localBoard, cTheme, localStriker, soundsOn, musicOn, vibrationOn, localTimerLimit, localDifficulty)
                                    }
                                )
                            }
                        }

                        SettingsCategory.RULES -> {
                            // 4. AUDIO & GAME RULES
                            Text(
                                "AUDIO & HAPTICS CONTROL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SleekSurface.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, SleekSurfaceBorder),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    SettingToggleRow(
                                        title = "Sound Effects",
                                        subtitle = "Collisions, strikes, and pocket sounds",
                                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                                        checked = soundsOn,
                                        onCheckedChange = { 
                                            soundsOn = it
                                            viewModel.saveSettings(localBoard, localCoins, localStriker, it, musicOn, vibrationOn, localTimerLimit, localDifficulty)
                                        }
                                    )

                                    HorizontalDivider(color = SleekSurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

                                    SettingToggleRow(
                                        title = "Ambient Music",
                                        subtitle = "Slow acoustic matches backdrops",
                                        icon = Icons.Default.MusicNote,
                                        checked = musicOn,
                                        onCheckedChange = { 
                                            musicOn = it
                                            viewModel.saveSettings(localBoard, localCoins, localStriker, soundsOn, it, vibrationOn, localTimerLimit, localDifficulty)
                                        }
                                    )

                                    HorizontalDivider(color = SleekSurfaceBorder, modifier = Modifier.padding(vertical = 8.dp))

                                    SettingToggleRow(
                                        title = "Haptic Vibration",
                                        subtitle = "Strike feedback haptic bumps",
                                        icon = Icons.Default.Vibration,
                                        checked = vibrationOn,
                                        onCheckedChange = { 
                                            vibrationOn = it
                                            viewModel.saveSettings(localBoard, localCoins, localStriker, soundsOn, musicOn, it, localTimerLimit, localDifficulty)
                                        }
                                    )
                                }
                            }

                            Text(
                                "TURN TIME LIMIT & RULES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = SleekSurface.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, SleekSurfaceBorder),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = SleekOrange,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    "Shot Countdown Timer",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = SleekTextPrimary
                                                )
                                                Text(
                                                    if (localTimerLimit > 0f) "Expires in ${localTimerLimit.toInt()}s • -5 pts & passes turn" else "Timer disabled (unlimited time)",
                                                    fontSize = 10.sp,
                                                    color = SleekTextMuted
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    val timerOptions = listOf(
                                        10f to "10s Blitz",
                                        15f to "15s Pro",
                                        20f to "20s Relaxed",
                                        30f to "30s Casual",
                                        0f to "Unlimited"
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        timerOptions.forEach { (sec, label) ->
                                            val isSelected = (sec == 0f && localTimerLimit <= 0f) || (sec > 0f && localTimerLimit == sec)
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    SoundManager.playStrikeSound()
                                                    localTimerLimit = sec
                                                    viewModel.saveSettings(localBoard, localCoins, localStriker, soundsOn, musicOn, vibrationOn, sec, localDifficulty)
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
                                                    selectedContainerColor = SleekOrange,
                                                    containerColor = SleekBackground
                                                ),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) SleekOrange else SleekSurfaceBorder
                                                ),
                                                modifier = Modifier.weight(1f).height(32.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        SettingsCategory.AI_BOTS -> {
                            // 5. AI INTELLIGENCE
                            Text(
                                "AI DIFFICULTY & INTELLIGENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekTextMuted,
                                letterSpacing = 1.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            AiDifficulty.values().forEach { diff ->
                                val isSelected = localDifficulty == diff
                                AiDifficultyCard(
                                    difficulty = diff,
                                    isSelected = isSelected,
                                    onClick = {
                                        SoundManager.playStrikeSound()
                                        localDifficulty = diff
                                        viewModel.setAiDifficultyLevel(diff)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Reset Default Button
                    OutlinedButton(
                        onClick = {
                            SoundManager.playStrikeSound()
                            viewModel.resetPreferencesData()
                            localBoard = viewModel.boardTheme
                            localCoins = viewModel.coinTheme
                            localStriker = viewModel.strikerDesign
                            localDifficulty = viewModel.aiDifficulty
                            soundsOn = SoundManager.isSoundEnabled
                            musicOn = SoundManager.isMusicEnabled
                            vibrationOn = SoundManager.isVibrationEnabled
                            localTimerLimit = viewModel.turnTimerSeconds
                            SoundManager.triggerVibration(context)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SleekOrange),
                        border = BorderStroke(1.dp, SleekOrange.copy(alpha = 0.5f)),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_settings_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("RESET DEFAULTS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun BoardSelectionCard(
    theme: BoardTheme,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("board_theme_${theme.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SleekSurface.copy(alpha = 0.9f) else SleekSurface.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) SleekOrange else SleekSurfaceBorder
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Mini Board Preview Canvas
            MiniBoardPreview(
                theme = theme,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        theme.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = SleekOrange,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "ACTIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    theme.subtitle,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 14.sp
                )

                // Pattern Pill
                Row(modifier = Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        color = SleekBackground,
                        shape = RoundedCornerShape(3.dp),
                        border = BorderStroke(0.5.dp, SleekSurfaceBorder)
                    ) {
                        Text(
                            theme.pattern.name.replace("_", " "),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = SleekOrange,
                    unselectedColor = SleekTextMuted
                )
            )
        }
    }
}

@Composable
fun StrikerSelectionCard(
    striker: StrikerDesign,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("striker_${striker.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SleekSurface.copy(alpha = 0.9f) else SleekSurface.copy(alpha = 0.45f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) striker.accentGlowColor else SleekSurfaceBorder
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live Mini Striker Preview Canvas
            MiniStrikerPreview(
                design = striker,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        striker.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = striker.accentGlowColor,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "EQUIPPED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    striker.subtitle,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 14.sp
                )

                // Particle signature preview tag
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(striker.particleColor, shape = CircleShape)
                    )
                    Text(
                        "Particle Trail Signature",
                        fontSize = 9.sp,
                        color = SleekTextMuted
                    )
                }
            }

            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = striker.accentGlowColor,
                    unselectedColor = SleekTextMuted
                )
            )
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SleekBackground, shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekOrange,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary
                )

                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
                SoundManager.playStrikeSound()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = SleekOrange,
                uncheckedThumbColor = SleekTextMuted,
                uncheckedTrackColor = SleekBackground
            )
        )
    }
}

@Composable
fun CoinSelectionCard(
    title: String,
    whiteColor: Color,
    blackColor: Color,
    queenColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) SleekOrange else SleekSurfaceBorder
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SleekTextPrimary)

            // Visual coins group preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // White Coin Preview
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(whiteColor, shape = CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), shape = CircleShape)
                )
                // Black Coin Preview
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(blackColor, shape = CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), shape = CircleShape)
                )
                // Queen Coin Preview
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(queenColor, shape = CircleShape)
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun AiDifficultyCard(
    difficulty: AiDifficulty,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tierColor = when (difficulty) {
        AiDifficulty.NOVICE -> Color(0xFF4CAF50)
        AiDifficulty.BEGINNER -> Color(0xFF29B6F6)
        AiDifficulty.INTERMEDIATE -> Color(0xFFFFB300)
        AiDifficulty.EXPERT -> Color(0xFFFF7043)
        AiDifficulty.GRANDMASTER -> Color(0xFFE040FB)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ai_diff_${difficulty.name.lowercase()}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SleekSurface.copy(alpha = 0.85f) else SleekSurface.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) tierColor else SleekSurfaceBorder
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(tierColor, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        difficulty.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) SleekTextPrimary else SleekTextPrimary.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    color = tierColor.copy(alpha = if (isSelected) 0.25f else 0.12f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Depth ${difficulty.strategicDepth}/5",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = difficulty.description,
                fontSize = 11.sp,
                color = SleekTextMuted,
                lineHeight = 15.sp
            )
        }
    }
}


