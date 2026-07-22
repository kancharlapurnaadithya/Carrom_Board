package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoundManager
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSetupScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Local input states initialized from VM
    var p1Name by remember { mutableStateOf(viewModel.player1Name) }
    var p2Name by remember { mutableStateOf(viewModel.player2Name) }
    var p3Name by remember { mutableStateOf(viewModel.player3Name) }
    var p4Name by remember { mutableStateOf(viewModel.player4Name) }

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
                        "MATCH SETUP",
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
                        viewModel.navigateTo(AppScreen.MENU)
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextPrimary
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
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title info card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SleekSurface.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, SleekSurfaceBorder),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = SleekOrange,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Configure your players counts and names below. Local multiplayer supports up to 4 players simultaneously on a single phone screen.",
                                fontSize = 12.sp,
                                color = SleekTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // 1. SELECT PLAYER COUNT
                    Text(
                        "CHOOSE PLAYER COUNT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(2, 3, 4).forEach { count ->
                            val isSelected = viewModel.playerCount == count
                            val cardColor = if (isSelected) SleekOrange else SleekSurface
                            val textColor = if (isSelected) Color.Black else SleekTextPrimary
                            val borderSpec = if (isSelected) null else BorderStroke(1.dp, SleekSurfaceBorder)

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(cardColor)
                                    .clickable {
                                        SoundManager.playStrikeSound()
                                        viewModel.playerCount = count
                                        SoundManager.triggerVibration(context)
                                    }
                                    .then(if (borderSpec != null) Modifier.border(borderSpec, MaterialTheme.shapes.medium) else Modifier),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${count} Players",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                            }
                        }
                    }

                    // 2. NAME INPUT FOR CHOSEN PLAYERS
                    Text(
                        "SET PLAYER NAMES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 12.dp)
                    )

                    // Input Player 1
                    PlayerNameRowInput(
                        label = "Player 1 (Bottom)",
                        value = p1Name,
                        onValueChange = { p1Name = it },
                        colorLabel = SleekOrange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Player 2
                    PlayerNameRowInput(
                        label = "Player 2 (Top)",
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        colorLabel = SleekAmber
                    )

                    // Inputs for 3 and 4 players
                    if (viewModel.playerCount >= 3) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PlayerNameRowInput(
                            label = "Player 3 (${if (viewModel.playerCount == 3) "Top-Right" else "Left"})",
                            value = p3Name,
                            onValueChange = { p3Name = it },
                            colorLabel = SleekOrangeLight
                        )
                    }

                    if (viewModel.playerCount == 4) {
                        Spacer(modifier = Modifier.height(12.dp))
                        PlayerNameRowInput(
                            label = "Player 4 (Right)",
                            value = p4Name,
                            onValueChange = { p4Name = it },
                            colorLabel = SleekAmberLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Launch Match!
                Button(
                    onClick = {
                        SoundManager.playStrikeSound()
                        // Save names to preferences
                        viewModel.savePlayerNames(p1Name, p2Name, p3Name, p4Name)
                        // Trigger match start
                        viewModel.startNewGame()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("launch_match_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SleekOrange
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text(
                        "START BATTLE MATCH",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerNameRowInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    colorLabel: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = colorLabel,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorLabel,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text("Enter custom name", color = SleekTextMuted) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = SleekTextPrimary,
                    unfocusedTextColor = SleekTextPrimary,
                    focusedContainerColor = SleekBackground,
                    unfocusedContainerColor = SleekBackground,
                    focusedIndicatorColor = colorLabel,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )
        }
    }
}
