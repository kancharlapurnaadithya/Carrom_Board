package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Visual power meter HUD component displaying real-time striker shot strength
 * while the user is aiming (pulling back to aim).
 *
 * @param powerFraction Current aiming power normalized between 0.0f (min) and 1.0f (max 100%).
 * @param powerBoostMultiplier Selected shot power boost multiplier (e.g., 1.0x, 1.5x, 2.0x).
 * @param isAiming Whether the player is actively dragging/aiming the striker.
 */
@Composable
fun StrikerPowerMeter(
    powerFraction: Float,
    powerBoostMultiplier: Float = 1.0f,
    isAiming: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isAiming && powerFraction <= 0.01f) return

    val clampedFraction = powerFraction.coerceIn(0f, 1f)
    val percentage = (clampedFraction * 100).toInt()

    // Pulse animation for high power
    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Dynamic color gradient based on power level
    val (powerColor, powerLabel) = when {
        clampedFraction > 0.85f -> Color(0xFFFF1744) to "MAX POWER!"
        clampedFraction > 0.65f -> Color(0xFFFF5252) to "HEAVY STRIKE"
        clampedFraction > 0.40f -> Color(0xFFFF9100) to "MEDIUM POWER"
        clampedFraction > 0.15f -> Color(0xFFFFD600) to "GENTLE TAP"
        else -> Color(0xFF00E676) to "AIMING..."
    }

    val gradientColors = when {
        clampedFraction > 0.85f -> listOf(Color(0xFFFF9100), Color(0xFFFF1744), Color(0xFFFF0055))
        clampedFraction > 0.65f -> listOf(Color(0xFFFFD600), Color(0xFFFF9100), Color(0xFFFF1744))
        clampedFraction > 0.40f -> listOf(Color(0xFF00E676), Color(0xFFFFD600), Color(0xFFFF9100))
        else -> listOf(Color(0xFF00E676), Color(0xFF69F0AE), Color(0xFFFFD600))
    }

    Column(
        modifier = modifier
            .testTag("striker_power_meter")
            .shadow(12.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xEE121420))
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        powerColor.copy(alpha = if (clampedFraction > 0.7f) pulseAlpha else 0.8f),
                        Color(0xFF333854)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Row with Icon, Label, and Power %
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Shot Power",
                    tint = powerColor,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "STRIKER POWER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekTextPrimary,
                    letterSpacing = 0.8.sp
                )
                if (powerBoostMultiplier > 1.0f) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SleekOrange.copy(alpha = 0.25f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${powerBoostMultiplier}x",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekOrange
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = powerLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = powerColor
                )
                Text(
                    text = "$percentage%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (clampedFraction > 0.85f) Color(0xFFFF1744) else SleekTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Segmented Progress Bar Gauge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1E2235))
                .border(0.5.dp, Color(0xFF333854), RoundedCornerShape(6.dp))
        ) {
            // Fill level
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(clampedFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        brush = Brush.horizontalGradient(gradientColors),
                        alpha = if (clampedFraction > 0.75f) pulseAlpha else 1.0f
                    )
            )

            // Tick markers at 25%, 50%, 75%
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                }
            }
        }
    }
}
