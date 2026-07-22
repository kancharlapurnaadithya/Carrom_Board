package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.example.data.database.MatchEntity
import com.example.ui.SoundManager
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val matches by viewModel.matches.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SleekBackground,
            Color(0xFF14141E)
        )
    )

    // Reactive calculations
    val totalPlayed = matches.size

    // Most common winner
    val winnerCounts = matches.groupBy { it.winnerName }.mapValues { it.value.size }
    val dominantWinner = winnerCounts.maxByOrNull { it.value }?.key ?: "No records"
    val dominantWinnerCount = winnerCounts.maxByOrNull { it.value }?.value ?: 0

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "MATCH ANALYSIS",
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
                actions = {
                    if (matches.isNotEmpty()) {
                        IconButton(onClick = {
                            SoundManager.playStrikeSound()
                            viewModel.clearCompletedMatches()
                            SoundManager.triggerVibration(context)
                        }) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = "Clear all logs",
                                tint = SleekOrange
                            )
                        }
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
            if (matches.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.HistoryToggleOff,
                        contentDescription = "No match logs",
                        tint = SleekTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(82.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "NO MATCH HISTORY YET",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextPrimary,
                        letterSpacing = 1.sp
                    )

                    Text(
                        "Start your first Local Duel from the main menu, complete a match, and view your detailed scores and dashboard diagnostics here.",
                        fontSize = 12.sp,
                        color = SleekTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, start = 12.dp, end = 12.dp),
                        lineHeight = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stats Metrics Dashboard Cards
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Total matches
                            MetricItemCard(
                                title = "MATCHES",
                                countVal = totalPlayed.toString(),
                                desc = "Offline duels",
                                icon = Icons.Default.Casino,
                                color = SleekOrange,
                                modifier = Modifier.weight(1f)
                            )

                            // Top winner
                            MetricItemCard(
                                title = "TOP LEADER",
                                countVal = dominantWinner,
                                desc = if (dominantWinnerCount > 0) "$dominantWinnerCount victories" else "No score",
                                icon = Icons.Default.WorkspacePremium,
                                color = SleekAmber,
                                modifier = Modifier.weight(1.2f)
                            )
                        }
                    }

                    // Log list header
                    item {
                        Text(
                            "HISTORICAL DUEL RECORDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Feed match logs
                    items(matches) { record ->
                        MatchHistoryRecordCard(record = record)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricItemCard(
    title: String,
    countVal: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextSecondary,
                    letterSpacing = 0.5.sp
                )

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = countVal,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color,
                maxLines = 1
            )

            Text(
                text = desc,
                fontSize = 11.sp,
                color = SleekTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun MatchHistoryRecordCard(
    record: MatchEntity,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(record.timestamp) {
        val sdf = SimpleDateFormat("MMM d, yyyy - hh:mm a", Locale.getDefault())
        sdf.format(Date(record.timestamp))
    }

    val playersList = remember(record.playersJoined) {
        record.playersJoined.split(",")
    }

    val scoresList = remember(record.scoresJoined) {
        record.scoresJoined.split(",")
    }

    Card(
         modifier = modifier.fillMaxWidth(),
         colors = CardDefaults.cardColors(
             containerColor = SleekSurface.copy(alpha = 0.35f)
         ),
         border = BorderStroke(1.dp, SleekSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.SpaceBetween,
                 verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = SleekTextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = SleekTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .background(SleekOrange.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp))
                        .border(1.dp, SleekOrange.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "WINNER: ${record.winnerName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scores comparative layout
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                playersList.forEachIndexed { index, pName ->
                    val isWinner = pName == record.winnerName
                    val pScore = scoresList.getOrNull(index) ?: "0"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isWinner) SleekOrange else SleekTextMuted,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pName,
                                fontSize = 13.sp,
                                color = if (isWinner) SleekTextPrimary else SleekTextSecondary,
                                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Text(
                            text = "$pScore pts",
                            fontSize = 13.sp,
                            color = if (isWinner) SleekOrange else SleekTextPrimary,
                            fontWeight = if (isWinner) FontWeight.Black else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
