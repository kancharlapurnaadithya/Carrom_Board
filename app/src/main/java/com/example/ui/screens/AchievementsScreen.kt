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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.AchievementEntity
import com.example.ui.SoundManager
import com.example.ui.theme.*
import com.example.viewmodel.AppScreen
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val achievements by viewModel.achievements.collectAsState()

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            SleekBackground,
            Color(0xFF14141E)
        )
    )

    val totalUnlocked = achievements.count { it.unlocked }
    val totalCount = achievements.size

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "AWARD TROPHIES",
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Completion Scorecard Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = SleekSurface.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(1.dp, SleekSurfaceBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .background(SleekOrange.copy(alpha = 0.12f), shape = MaterialTheme.shapes.medium),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = SleekOrange,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "MEDAL COMPLETION RATE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekTextMuted,
                                    letterSpacing = 1.sp
                                )

                                Text(
                                    text = "$totalUnlocked / $totalCount Trophies Unlocked",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = SleekTextPrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                // Linear progress bar
                                val progressRatio = if (totalCount > 0) totalUnlocked.toFloat() / totalCount else 0f
                                LinearProgressIndicator(
                                    progress = { progressRatio },
                                    trackColor = SleekBackground,
                                    color = SleekOrange,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                        .height(6.dp)
                                        .clip(MaterialTheme.shapes.small)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "OFFLINE MEDAL LIST",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Render achievements list
                items(achievements) { award ->
                    AchievementAwardRowCard(award = award)
                }
            }
        }
    }
}

@Composable
fun AchievementAwardRowCard(
    award: AchievementEntity,
    modifier: Modifier = Modifier
) {
    val tintBadge = if (award.unlocked) SleekAmber else SleekTextMuted.copy(alpha = 0.4f)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SleekSurface.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, SleekSurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trophy badge circular emblem
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (award.unlocked) SleekAmber.copy(alpha = 0.10f) else Color(0xFF2C2C2E).copy(alpha = 0.10f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (award.unlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                    contentDescription = null,
                    tint = tintBadge,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = award.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (award.unlocked) SleekAmber else SleekTextPrimary
                )

                Text(
                    text = award.description,
                    fontSize = 11.sp,
                    color = SleekTextSecondary,
                    modifier = Modifier.padding(top = 2.dp),
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Progress row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val scaleRatio = if (award.maxProgress > 0) award.progress.toFloat() / award.maxProgress else 1f
                    LinearProgressIndicator(
                        progress = { scaleRatio },
                        trackColor = SleekBackground,
                        color = tintBadge,
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(MaterialTheme.shapes.small)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "${award.progress}/${award.maxProgress}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (award.unlocked) SleekAmber else SleekTextSecondary
                    )
                }
            }
        }
    }
}
