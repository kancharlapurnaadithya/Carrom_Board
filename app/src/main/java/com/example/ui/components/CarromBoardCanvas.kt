package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.SoundManager
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CarromBoardCanvas(
    engine: CarromEngine,
    strikerPlacedPositionFraction: Float,
    powerBoostMultiplier: Float = 1.0f,
    onAimStart: (Offset) -> Unit,
    onAimDrag: (Offset) -> Unit,
    onAimRelease: (Offset, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    // Touch interaction drag logic
    var isDraggingStr by remember { mutableStateOf(false) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var dragCurrentOffset by remember { mutableStateOf(Offset.Zero) }

    val theme = engine.boardTheme
    val coinTheme = engine.coinTheme

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(theme.borderColor, shape = MaterialTheme.shapes.medium)
            .padding(12.dp) // Outer border representation
            .background(theme.boardColor)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInteropFilter { event ->
                    if (engine.isMoving || engine.gameCompleted) return@pointerInteropFilter false

                    val scaleX = canvasSize.width / CarromEngine.BOARD_SIZE
                    val scaleY = canvasSize.height / CarromEngine.BOARD_SIZE

                    val touchXInVirtual = event.x / scaleX
                    val touchYInVirtual = event.y / scaleY

                    val touchVector = PhysicsVector(touchXInVirtual, touchYInVirtual)
                    val strikerVector = PhysicsVector(engine.striker.x, engine.striker.y)

                    val dx = touchVector.x - strikerVector.x
                    val dy = touchVector.y - strikerVector.y
                    val distToStriker = sqrt(dx * dx + dy * dy)

                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Can only drag if striker is placed and we touch close to it
                            if (distToStriker < CarromEngine.STRIKER_RADIUS * 1.8f) {
                                isDraggingStr = true
                                dragStartOffset = Offset(event.x, event.y)
                                dragCurrentOffset = Offset(event.x, event.y)
                                onAimStart(dragStartOffset)
                                SoundManager.triggerVibration(context)
                                true
                            } else {
                                false
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            if (isDraggingStr) {
                                dragCurrentOffset = Offset(event.x, event.y)
                                onAimDrag(dragCurrentOffset)
                                true
                            } else {
                                false
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            if (isDraggingStr) {
                                isDraggingStr = false
                                // Calculate launch vector
                                val vxVirtual = (dragStartOffset.x - event.x) / scaleX // Shot direction is opposite of drag pull
                                val vyVirtual = (dragStartOffset.y - event.y) / scaleY

                                // Power bounds
                                val dragDistance = sqrt(vxVirtual * vxVirtual + vyVirtual * vyVirtual)
                                val maxDrag = 200f // Expanded virtual dimension drag limit for powerful shots
                                val clampedDistance = dragDistance.coerceAtMost(maxDrag)

                                val angle = atan2(vyVirtual, vxVirtual)
                                val basePowerFactor = 0.35f // Boosted striker base power
                                val finalFactor = basePowerFactor * powerBoostMultiplier
                                val finalVx = cos(angle) * clampedDistance * finalFactor
                                val finalVy = sin(angle) * clampedDistance * finalFactor

                                // Minimum firing threshold
                                if (clampedDistance > 10f) {
                                    onAimRelease(dragStartOffset, clampedDistance)
                                    engine.launchStriker(finalVx, finalVy)
                                    if (clampedDistance > 130f) {
                                        // Extra particle flash on super shot launch
                                        engine.triggerStreakParticles(engine.striker.x, engine.striker.y, Color(0xFFFFB300))
                                    }
                                    SoundManager.playStrikeSound()
                                    SoundManager.triggerVibration(context)
                                } else {
                                    onAimRelease(dragStartOffset, 0f)
                                }
                                true
                            } else {
                                false
                            }
                        }
                        else -> false
                    }
                }
        ) {
            canvasSize = size
            val scaleX = size.width / CarromEngine.BOARD_SIZE
            val scaleY = size.height / CarromEngine.BOARD_SIZE
            val avgScale = (scaleX + scaleY) / 2f

            // 1. Draw static Board Markings (Center circles, baselines, arrows)
            drawBoardMarkings(this, theme, avgScale)

            // 2. Draw Pockets
            drawPockets(this, theme, avgScale)

            // 3. Draw Coins
            for (coin in engine.coins) {
                if (!coin.isPocketed) {
                    drawCoinEntity(this, coin, coinTheme, avgScale, scaleX, scaleY)
                }
            }

            // 4. Draw Striker
            if (!engine.isStrikerPlaced && !engine.striker.isPocketed) {
                drawCoinEntity(this, engine.striker, coinTheme, avgScale, scaleX, scaleY)
            } else if (engine.isStrikerPlaced) {
                // Highlight placed striker
                drawCoinEntity(this, engine.striker, coinTheme, avgScale, scaleX, scaleY)
                
                // Pulsating golden/neon placement ring
                if (!engine.isMoving) {
                    val pulseRadius = (CarromEngine.STRIKER_RADIUS + 5f) * avgScale
                    drawCircle(
                        color = if (theme.isNeon) Color(0xFF00FFCC) else Color(0xFFFFB300),
                        radius = pulseRadius,
                        center = Offset(engine.striker.x * scaleX, engine.striker.y * scaleY),
                        style = Stroke(width = 2f * avgScale)
                    )
                }
            }

            // 5. Draw Aiming laser preview line & extended ricochet trajectory
            if (isDraggingStr) {
                val sX = engine.striker.x * scaleX
                val sY = engine.striker.y * scaleY

                // Drag current relative vector
                val touchDistX = dragCurrentOffset.x - dragStartOffset.x
                val touchDistY = dragCurrentOffset.y - dragStartOffset.y
                val dist = sqrt(touchDistX * touchDistX + touchDistY * touchDistY)

                if (dist > 8f) {
                    // Line of action of shooting (Opposite of drag)
                    val aimAngle = atan2(-touchDistY, -touchDistX)
                    val maxRay = 450f * avgScale // Extended laser sight
                    val shootLen = (dist * 3.5f).coerceAtMost(maxRay)

                    val endAimX = sX + cos(aimAngle) * shootLen
                    val endAimY = sY + sin(aimAngle) * shootLen

                    // Laser pointer preview line
                    val previewColor = when {
                        dist > 120f -> Color(0xFFFF0055)
                        dist > 60f -> Color(0xFFFF9100)
                        else -> Color(0xFF00E676)
                    }

                    drawLine(
                        color = previewColor.copy(alpha = 0.85f),
                        start = Offset(sX, sY),
                        end = Offset(endAimX, endAimY),
                        strokeWidth = 3.5f * avgScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                    )

                    // Target impact reticle
                    drawCircle(
                        color = previewColor,
                        radius = 8f * avgScale,
                        center = Offset(endAimX, endAimY),
                        style = Stroke(width = 2.5f * avgScale)
                    )

                    // Extended Rebound reflection line preview
                    val boardMinX = CarromEngine.PLAYABLE_MARGIN * scaleX
                    val boardMaxX = (CarromEngine.BOARD_SIZE - CarromEngine.PLAYABLE_MARGIN) * scaleX
                    val boardMinY = CarromEngine.PLAYABLE_MARGIN * scaleY
                    val boardMaxY = (CarromEngine.BOARD_SIZE - CarromEngine.PLAYABLE_MARGIN) * scaleY

                    // Simple bounce calculation if ray hits border
                    var bounceAngle = aimAngle
                    var hitBorder = false
                    if (endAimX <= boardMinX || endAimX >= boardMaxX) {
                        bounceAngle = Math.PI.toFloat() - aimAngle
                        hitBorder = true
                    } else if (endAimY <= boardMinY || endAimY >= boardMaxY) {
                        bounceAngle = -aimAngle
                        hitBorder = true
                    }

                    if (hitBorder) {
                        val reboundLen = 120f * avgScale
                        val reboundEndX = endAimX + cos(bounceAngle) * reboundLen
                        val reboundEndY = endAimY + sin(bounceAngle) * reboundLen

                        drawLine(
                            color = Color(0xFFFFD700).copy(alpha = 0.7f),
                            start = Offset(endAimX, endAimY),
                            end = Offset(reboundEndX, reboundEndY),
                            strokeWidth = 2.5f * avgScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }

                    // Draw dynamic power ring gauge around striker
                    val powerFraction = (dist / 150f).coerceIn(0f, 1f)
                    drawCircle(
                        color = previewColor.copy(alpha = 0.8f),
                        radius = (CarromEngine.STRIKER_RADIUS + 8f + powerFraction * 14f) * avgScale,
                        center = Offset(sX, sY),
                        style = Stroke(width = 3f * avgScale, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
                    )

                    // Draw drag back cursor circle
                    drawCircle(
                        color = if (theme.isNeon) Color(0xFF00FFCC) else Color(0xFF424242).copy(alpha = 0.6f),
                        radius = (CarromEngine.STRIKER_RADIUS - 4f) * avgScale,
                        center = dragCurrentOffset,
                        style = Stroke(width = 2.5f * avgScale)
                    )

                    drawLine(
                        color = if (theme.isNeon) Color(0xFF00FFCC) else Color(0xFFE5A93C).copy(alpha = 0.8f),
                        start = Offset(sX, sY),
                        end = dragCurrentOffset,
                        strokeWidth = 2f * avgScale
                    )
                }
            }

            // 6. Draw Particle effects
            for (p in engine.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size * avgScale / 2.5f,
                    center = Offset(p.x * scaleX, p.y * scaleY)
                )
            }
        }
    }
}

private fun drawBoardMarkings(drawScope: DrawScope, theme: BoardTheme, scale: Float) {
    val cx = 400f * scale
    val cy = 400f * scale

    // Draw center concentric circles
    // Outer circle
    drawScope.drawCircle(
        color = theme.centerCircleColor.copy(alpha = 0.35f),
        radius = 80f * scale,
        center = Offset(cx, cy),
        style = Stroke(width = 1.5f * scale)
    )

    // Secondary decorative circle
    drawScope.drawCircle(
        color = theme.centerCircleColor.copy(alpha = 0.50f),
        radius = 50f * scale,
        center = Offset(cx, cy),
        style = Stroke(width = 2f * scale)
    )

    // Solid center red queen circle
    drawScope.drawCircle(
        color = if (theme.isNeon) Color(0xFFFF0055) else Color(0xFFD32F2F),
        radius = 20f * scale,
        center = Offset(cx, cy)
    )

    // Inner details center spikes/star
    for (i in 0 until 8) {
        val angle = (i * 45.0 * Math.PI / 180.0).toFloat()
        val sX = cx + cos(angle) * 20f * scale
        val sY = cy + sin(angle) * 20f * scale
        val eX = cx + cos(angle) * 75f * scale
        val eY = cy + sin(angle) * 75f * scale
        drawScope.drawLine(
            color = theme.centerCircleColor.copy(alpha = 0.40f),
            start = Offset(sX, sY),
            end = Offset(eX, eY),
            strokeWidth = 1f * scale
        )
    }

    // Baselines on 4 sides
    val baselineColor = theme.textColor.copy(alpha = 0.5f)

    // Draw baselines
    // Bottom Line
    drawScope.drawLine(baselineColor, Offset(170f * scale, 640f * scale), Offset(630f * scale, 640f * scale), strokeWidth = 1.5f * scale)
    drawScope.drawLine(baselineColor, Offset(170f * scale, 625f * scale), Offset(630f * scale, 625f * scale), strokeWidth = 1f * scale)

    // Top Line
    drawScope.drawLine(baselineColor, Offset(170f * scale, 160f * scale), Offset(630f * scale, 160f * scale), strokeWidth = 1.5f * scale)
    drawScope.drawLine(baselineColor, Offset(170f * scale, 175f * scale), Offset(630f * scale, 175f * scale), strokeWidth = 1f * scale)

    // Left Line
    drawScope.drawLine(baselineColor, Offset(160f * scale, 170f * scale), Offset(160f * scale, 630f * scale), strokeWidth = 1.5f * scale)
    drawScope.drawLine(baselineColor, Offset(175f * scale, 170f * scale), Offset(175f * scale, 630f * scale), strokeWidth = 1f * scale)

    // Right Line
    drawScope.drawLine(baselineColor, Offset(640f * scale, 170f * scale), Offset(640f * scale, 630f * scale), strokeWidth = 1.5f * scale)
    drawScope.drawLine(baselineColor, Offset(625f * scale, 170f * scale), Offset(625f * scale, 630f * scale), strokeWidth = 1f * scale)

    // Circle disks at endpoints of baselines (Red spots)
    val redSpotColor = if (theme.isNeon) Color(0xFF00FFCC) else Color(0xFFE5A93C)
    val baselineCirclesPos = listOf(
        // Bottom Endpoints
        Offset(170f * scale, 632.5f * scale), Offset(630f * scale, 632.5f * scale),
        // Top Endpoints
        Offset(170f * scale, 167.5f * scale), Offset(630f * scale, 167.5f * scale),
        // Left Endpoints
        Offset(167.5f * scale, 170f * scale), Offset(167.5f * scale, 630f * scale),
        // Right Endpoints
        Offset(632.5f * scale, 170f * scale), Offset(632.5f * scale, 630f * scale)
    )

    for (pt in baselineCirclesPos) {
        drawScope.drawCircle(
            color = redSpotColor,
            radius = 12f * scale,
            center = pt
        )
        drawScope.drawCircle(
            color = theme.boardColor,
            radius = 6f * scale,
            center = pt
        )
    }

    // Diagonal lines from center extending to pockets
    val cornerDirections = listOf(
        Offset(55f * scale, 55f * scale),
        Offset(745f * scale, 55f * scale),
        Offset(55f * scale, 745f * scale),
        Offset(745f * scale, 745f * scale)
    )
    for (pocket in cornerDirections) {
        val dxVal = pocket.x - cx
        val dyVal = pocket.y - cy
        val len = sqrt(dxVal * dxVal + dyVal * dyVal)
        val nx = dxVal / len
        val ny = dyVal / len

        // Draw line starting 140f away from center to 90f away from pocket
        val stX = cx + nx * 140f * scale
        val stY = cy + ny * 140f * scale
        val etX = pocket.x - nx * 90f * scale
        val etY = pocket.y - ny * 90f * scale

        drawScope.drawLine(
            color = theme.textColor.copy(alpha = 0.25f),
            start = Offset(stX, stY),
            end = Offset(etX, etY),
            strokeWidth = 1.5f * scale
        )
    }
}

private fun drawPockets(drawScope: DrawScope, theme: BoardTheme, scale: Float) {
    for (pocketCenter in CarromEngine.POCKETS) {
        val px = pocketCenter.x * scale
        val py = pocketCenter.y * scale
        val r = CarromEngine.POCKET_RADIUS * scale

        // Outer pocket ring
        val ringColor = if (theme.isNeon) Color(0xFFFF0055) else Color(0xFF3E2723)
        drawScope.drawCircle(
            color = ringColor,
            radius = r + 4f * scale,
            center = Offset(px, py),
            style = Stroke(width = 3f * scale)
        )

        // Pocket hole (deep black)
        drawScope.drawCircle(
            color = Color(0xFF0F0F12),
            radius = r,
            center = Offset(px, py)
        )

        // Decorative pocket netting net representation (cross lines)
        val netColor = Color.Gray.copy(alpha = 0.35f)
        drawScope.drawLine(netColor, Offset(px - r * 0.7f, py - r * 0.7f), Offset(px + r * 0.7f, py + r * 0.7f), strokeWidth = 1f * scale)
        drawScope.drawLine(netColor, Offset(px + r * 0.7f, py - r * 0.7f), Offset(px - r * 0.7f, py + r * 0.7f), strokeWidth = 1f * scale)
    }
}

private fun drawCoinEntity(
    drawScope: DrawScope,
    coin: Coin,
    theme: CoinTheme,
    avgScale: Float,
    scaleX: Float,
    scaleY: Float
) {
    val cx = coin.x * scaleX
    val cy = coin.y * scaleY
    val radius = coin.radius * avgScale * coin.scale
    val alphaValue = coin.opacity

    if (radius <= 0.1f) return

    val (primaryColor, centerColor) = when (coin.type) {
        CoinType.WHITE -> Pair(theme.whiteCoinColor, theme.whiteCoinInner)
        CoinType.BLACK -> Pair(theme.blackCoinColor, theme.blackCoinInner)
        CoinType.QUEEN -> Pair(theme.queenCoinColor, theme.queenCoinInner)
        CoinType.STRIKER -> Pair(Color.White, Color.Transparent)
    }

    if (coin.type == CoinType.STRIKER) {
        // Draw highly premium striker disc
        // Chrome metallic outer ring
        drawScope.drawCircle(
            color = Color(0xFFECEFF1).copy(alpha = alphaValue),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Colored core (based on neon or gold themes)
        val strikerCore = if (theme == CoinTheme.NEON_GLOW) Color(0xFF00FFCC) else Color(0xFF1B263B)
        drawScope.drawCircle(
            color = strikerCore.copy(alpha = alphaValue),
            radius = radius * 0.78f,
            center = Offset(cx, cy)
        )

        // Center ring / design star
        drawScope.drawCircle(
            color = Color(0xFFFDD835).copy(alpha = alphaValue),
            radius = radius * 0.45f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f * avgScale)
        )

        // Tiny center spot
        drawScope.drawCircle(
            color = Color(0xFFFF3D00).copy(alpha = alphaValue),
            radius = radius * 0.20f,
            center = Offset(cx, cy)
        )

        // Radial indicators
        for (i in 0 until 4) {
            val angle = (i * 90.0 * Math.PI / 180.0).toFloat()
            val sx = cx + cos(angle) * radius * 0.45f
            val sy = cy + sin(angle) * radius * 0.45f
            val ex = cx + cos(angle) * radius * 0.78f
            val ey = cy + sin(angle) * radius * 0.78f
            drawScope.drawLine(
                color = Color.White.copy(alpha = alphaValue),
                start = Offset(sx, sy),
                end = Offset(ex, ey),
                strokeWidth = 1.5f * avgScale
            )
        }
    } else {
        // Draw normal resin carrommen coin with concentric groove ridges
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = alphaValue), primaryColor.copy(alpha = alphaValue * 0.7f)),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Concentric grooved ring ridge
        drawScope.drawCircle(
            color = centerColor.copy(alpha = alphaValue),
            radius = radius * 0.65f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f * avgScale)
        )

        // Concentric inner ring
        drawScope.drawCircle(
            color = centerColor.copy(alpha = alphaValue),
            radius = radius * 0.35f,
            center = Offset(cx, cy),
            style = Stroke(width = 1f * avgScale)
        )

        // Center tiny dot
        drawScope.drawCircle(
            color = primaryColor.copy(alpha = alphaValue),
            radius = radius * 0.15f,
            center = Offset(cx, cy)
        )
    }
}
