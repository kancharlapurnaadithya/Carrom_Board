package com.example.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    val strikerDesign = engine.strikerDesign

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(theme.borderColor, shape = MaterialTheme.shapes.medium)
            .padding(10.dp) // Outer border representation
            .border(2.dp, theme.borderInnerTrim, shape = MaterialTheme.shapes.small)
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
                                        // Extra particle flash on super shot launch with striker's signature particle color
                                        engine.triggerStreakParticles(engine.striker.x, engine.striker.y, strikerDesign.particleColor)
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

            // 1. Draw unique Board Texture/Pattern & Gradient background
            drawBoardSurfacePattern(this, theme, size, avgScale)

            // 2. Draw static Board Markings (Center mandala, baselines, corner decorations)
            drawBoardMarkings(this, theme, avgScale)

            // 3. Draw Pockets
            drawPockets(this, theme, avgScale)

            // 4. Draw Coins
            for (coin in engine.coins) {
                if (!coin.isPocketed) {
                    drawCoinEntity(this, coin, coinTheme, strikerDesign, avgScale, scaleX, scaleY)
                }
            }

            // 5. Draw Striker
            if (!engine.isStrikerPlaced && !engine.striker.isPocketed) {
                drawStrikerEntity(this, engine.striker, strikerDesign, avgScale, scaleX, scaleY)
            } else if (engine.isStrikerPlaced) {
                // Highlight placed striker
                drawStrikerEntity(this, engine.striker, strikerDesign, avgScale, scaleX, scaleY)
                
                // Pulsating golden/neon placement ring
                if (!engine.isMoving) {
                    val pulseRadius = (CarromEngine.STRIKER_RADIUS + 5f) * avgScale
                    drawCircle(
                        color = strikerDesign.accentGlowColor,
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

            // 6. Draw Particle effects with glowing aura & crisp highlight core
            for (p in engine.particles) {
                val px = p.x * scaleX
                val py = p.y * scaleY
                val baseRadius = p.size * avgScale / 2.2f
                val clampedAlpha = p.alpha.coerceIn(0f, 1f)

                // Outer soft glowing aura
                drawCircle(
                    color = p.color.copy(alpha = (clampedAlpha * 0.35f).coerceIn(0f, 1f)),
                    radius = baseRadius * 1.8f,
                    center = Offset(px, py)
                )

                // Core colored spark
                drawCircle(
                    color = p.color.copy(alpha = clampedAlpha),
                    radius = baseRadius,
                    center = Offset(px, py)
                )

                // Inner bright white highlight core for extra brilliance
                if (clampedAlpha > 0.35f) {
                    drawCircle(
                        color = Color.White.copy(alpha = (clampedAlpha * 0.85f).coerceIn(0f, 1f)),
                        radius = (baseRadius * 0.45f).coerceAtLeast(1f),
                        center = Offset(px, py)
                    )
                }
            }
        }
    }
}

private fun drawBoardSurfacePattern(
    drawScope: DrawScope,
    theme: BoardTheme,
    size: Size,
    scale: Float
) {
    // 1. Base Gradient Fill
    drawScope.drawRect(
        brush = Brush.radialGradient(
            colors = listOf(theme.boardColor, theme.boardGradientSecondary),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = size.width * 0.75f
        ),
        size = size
    )

    // 2. Specialized Pattern Rendering per Board Style
    when (theme.pattern) {
        BoardPattern.WOOD_GRAIN -> {
            // Authentic fine wood plank lines & grain streaks
            val plankWidth = 50f * scale
            var x = 0f
            while (x < size.width) {
                drawScope.drawLine(
                    color = Color(0xFF42220F).copy(alpha = 0.05f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f * scale
                )
                x += plankWidth
            }
            // Fine subtle grain waves
            for (i in 0 until 12) {
                val y = (i * 70f + 25f) * scale
                drawScope.drawLine(
                    color = Color(0xFF42220F).copy(alpha = 0.035f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y + 15f * scale),
                    strokeWidth = 1.5f * scale
                )
            }
        }
        BoardPattern.MARBLE_VEIN -> {
            // Elegant gold and subtle slate marble veins
            val veinGold = Color(0xFFD4AF37).copy(alpha = 0.12f)
            val veinGrey = Color(0xFF9E9E9E).copy(alpha = 0.08f)
            
            // Vein 1
            val path1 = Path().apply {
                moveTo(0f, size.height * 0.2f)
                cubicTo(size.width * 0.3f, size.height * 0.15f, size.width * 0.6f, size.height * 0.45f, size.width, size.height * 0.35f)
            }
            drawScope.drawPath(path1, veinGold, style = Stroke(width = 2.5f * scale))
            
            // Vein 2
            val path2 = Path().apply {
                moveTo(size.width * 0.1f, size.height)
                cubicTo(size.width * 0.4f, size.height * 0.7f, size.width * 0.7f, size.height * 0.85f, size.width * 0.9f, 0f)
            }
            drawScope.drawPath(path2, veinGrey, style = Stroke(width = 2f * scale))
        }
        BoardPattern.NEON_GRID -> {
            // Futuristic glowing cyber matrix grid
            val gridStep = 40f * scale
            val gridColor = Color(0xFF00F5FF).copy(alpha = 0.08f)
            var gx = 0f
            while (gx < size.width) {
                drawScope.drawLine(gridColor, Offset(gx, 0f), Offset(gx, size.height), strokeWidth = 1f * scale)
                gx += gridStep
            }
            var gy = 0f
            while (gy < size.height) {
                drawScope.drawLine(gridColor, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1f * scale)
                gy += gridStep
            }
            // Center cybernetic hexagon ring
            val hexPath = Path().apply {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = 130f * scale
                for (i in 0 until 6) {
                    val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
                    val px = cx + cos(angle) * r
                    val py = cy + sin(angle) * r
                    if (i == 0) moveTo(px, py) else lineTo(px, py)
                }
                close()
            }
            drawScope.drawPath(hexPath, Color(0xFFFF007F).copy(alpha = 0.25f), style = Stroke(width = 1.5f * scale))
        }
        BoardPattern.TOURNAMENT_BAIZE -> {
            // Fine textured championship felt weave
            val weaveColor = Color(0xFFFFFFFF).copy(alpha = 0.03f)
            var w = 0f
            while (w < size.width + size.height) {
                drawScope.drawLine(weaveColor, Offset(w, 0f), Offset(0f, w), strokeWidth = 1f * scale)
                w += 20f * scale
            }
        }
        BoardPattern.OBSIDIAN_CARBON -> {
            // Carbon fiber diagonal weave
            val carbonColor = Color(0xFF000000).copy(alpha = 0.20f)
            var d = -size.height
            while (d < size.width + size.height) {
                drawScope.drawLine(carbonColor, Offset(d, 0f), Offset(d + size.height, size.height), strokeWidth = 2f * scale)
                d += 24f * scale
            }
        }
        BoardPattern.SOLAR_FLAME -> {
            // Radial sunflare rays
            val cx = size.width / 2f
            val cy = size.height / 2f
            for (i in 0 until 16) {
                val angle = (i * 22.5 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * 90f * scale
                val sy = cy + sin(angle) * 90f * scale
                val ex = cx + cos(angle) * 320f * scale
                val ey = cy + sin(angle) * 320f * scale
                drawScope.drawLine(
                    color = Color(0xFFFF6D00).copy(alpha = 0.08f),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 3f * scale
                )
            }
        }
        BoardPattern.COSMIC_NEBULA -> {
            // Stardust sparkles and planetary constellation orbit lines
            val starColor = Color(0xFF82B1FF).copy(alpha = 0.35f)
            val stars = listOf(
                Offset(size.width * 0.22f, size.height * 0.28f),
                Offset(size.width * 0.78f, size.height * 0.24f),
                Offset(size.width * 0.25f, size.height * 0.76f),
                Offset(size.width * 0.75f, size.height * 0.72f),
                Offset(size.width * 0.50f, size.height * 0.22f),
                Offset(size.width * 0.50f, size.height * 0.78f)
            )
            for (pt in stars) {
                drawScope.drawCircle(starColor, radius = 2.5f * scale, center = pt)
                drawScope.drawCircle(Color.White.copy(alpha = 0.6f), radius = 1.2f * scale, center = pt)
            }
        }
        BoardPattern.ICE_GLACIER -> {
            // Crystalline frosty ice facets
            val frostColor = Color(0xFF00B0FF).copy(alpha = 0.09f)
            val frostPath = Path().apply {
                moveTo(size.width * 0.5f, 20f * scale)
                lineTo(size.width - 20f * scale, size.height * 0.5f)
                lineTo(size.width * 0.5f, size.height - 20f * scale)
                lineTo(20f * scale, size.height * 0.5f)
                close()
            }
            drawScope.drawPath(frostPath, frostColor, style = Stroke(width = 1.5f * scale))
        }
    }
}

private fun drawBoardMarkings(drawScope: DrawScope, theme: BoardTheme, scale: Float) {
    val cx = 400f * scale
    val cy = 400f * scale

    // 1. UNIQUE CENTER MANDALA PER THEME
    drawCenterMandalaForTheme(drawScope, theme, cx, cy, scale)

    // 2. Baselines on 4 sides
    val baselineColor = theme.textColor.copy(alpha = 0.6f)
    val baselineInnerColor = theme.textColor.copy(alpha = 0.35f)

    // Bottom Lines
    drawScope.drawLine(baselineColor, Offset(170f * scale, 640f * scale), Offset(630f * scale, 640f * scale), strokeWidth = 1.6f * scale)
    drawScope.drawLine(baselineInnerColor, Offset(170f * scale, 625f * scale), Offset(630f * scale, 625f * scale), strokeWidth = 1.2f * scale)

    // Top Lines
    drawScope.drawLine(baselineColor, Offset(170f * scale, 160f * scale), Offset(630f * scale, 160f * scale), strokeWidth = 1.6f * scale)
    drawScope.drawLine(baselineInnerColor, Offset(170f * scale, 175f * scale), Offset(630f * scale, 175f * scale), strokeWidth = 1.2f * scale)

    // Left Lines
    drawScope.drawLine(baselineColor, Offset(160f * scale, 170f * scale), Offset(160f * scale, 630f * scale), strokeWidth = 1.6f * scale)
    drawScope.drawLine(baselineInnerColor, Offset(175f * scale, 170f * scale), Offset(175f * scale, 630f * scale), strokeWidth = 1.2f * scale)

    // Right Lines
    drawScope.drawLine(baselineColor, Offset(640f * scale, 170f * scale), Offset(640f * scale, 630f * scale), strokeWidth = 1.6f * scale)
    drawScope.drawLine(baselineInnerColor, Offset(625f * scale, 170f * scale), Offset(625f * scale, 630f * scale), strokeWidth = 1.2f * scale)

    // 3. Baseline Endpoint Target Spots
    val redSpotColor = theme.accentColor
    val baselineCirclesPos = listOf(
        Offset(170f * scale, 632.5f * scale), Offset(630f * scale, 632.5f * scale),
        Offset(170f * scale, 167.5f * scale), Offset(630f * scale, 167.5f * scale),
        Offset(167.5f * scale, 170f * scale), Offset(167.5f * scale, 630f * scale),
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
        drawScope.drawCircle(
            color = if (theme.isNeon) Color(0xFFFF007F) else Color(0xFFD32F2F),
            radius = 3f * scale,
            center = pt
        )
    }

    // 4. Diagonal Pocket Arrows with Distinct Arrowheads
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

        val stX = cx + nx * 135f * scale
        val stY = cy + ny * 135f * scale
        val etX = pocket.x - nx * 90f * scale
        val etY = pocket.y - ny * 90f * scale

        // Main diagonal arrow shaft
        drawScope.drawLine(
            color = theme.textColor.copy(alpha = 0.35f),
            start = Offset(stX, stY),
            end = Offset(etX, etY),
            strokeWidth = 1.6f * scale
        )

        // Arrowhead pointing towards corner pocket
        val arrowTip = Offset(pocket.x - nx * 82f * scale, pocket.y - ny * 82f * scale)
        val perpX = -ny
        val perpY = nx
        val arrowLeft = Offset(etX + perpX * 8f * scale, etY + perpY * 8f * scale)
        val arrowRight = Offset(etX - perpX * 8f * scale, etY - perpY * 8f * scale)

        val arrowPath = Path().apply {
            moveTo(arrowTip.x, arrowTip.y)
            lineTo(arrowLeft.x, arrowLeft.y)
            lineTo(arrowRight.x, arrowRight.y)
            close()
        }
        drawScope.drawPath(arrowPath, color = theme.accentColor.copy(alpha = 0.75f))
    }

    // 5. Decorative Corner Brackets / Motifs
    drawCornerBrackets(drawScope, theme, scale)
}

private fun drawCenterMandalaForTheme(
    drawScope: DrawScope,
    theme: BoardTheme,
    cx: Float,
    cy: Float,
    scale: Float
) {
    // Outer boundary circle
    drawScope.drawCircle(
        color = theme.centerCircleColor.copy(alpha = 0.35f),
        radius = 80f * scale,
        center = Offset(cx, cy),
        style = Stroke(width = 1.8f * scale)
    )

    // Mid decorative ring
    drawScope.drawCircle(
        color = theme.accentColor.copy(alpha = 0.55f),
        radius = 50f * scale,
        center = Offset(cx, cy),
        style = Stroke(width = 2.2f * scale)
    )

    // Queen Red Center Spot
    val centerQueenColor = if (theme.isNeon) Color(0xFFFF007F) else Color(0xFFD32F2F)
    drawScope.drawCircle(
        color = centerQueenColor,
        radius = 20f * scale,
        center = Offset(cx, cy)
    )
    drawScope.drawCircle(
        color = Color.White.copy(alpha = 0.8f),
        radius = 5f * scale,
        center = Offset(cx, cy)
    )

    // Unique Pattern Spokes/Petals per Theme
    when (theme.pattern) {
        BoardPattern.WOOD_GRAIN -> {
            // Traditional 8-petal floral carrom star
            for (i in 0 until 8) {
                val angle = (i * 45.0 * Math.PI / 180.0).toFloat()
                val sX = cx + cos(angle) * 20f * scale
                val sY = cy + sin(angle) * 20f * scale
                val eX = cx + cos(angle) * 76f * scale
                val eY = cy + sin(angle) * 76f * scale
                drawScope.drawLine(theme.centerCircleColor.copy(alpha = 0.6f), Offset(sX, sY), Offset(eX, eY), strokeWidth = 1.4f * scale)
            }
        }
        BoardPattern.MARBLE_VEIN -> {
            // Royal Fleur-de-lis gold 12-point rosette
            for (i in 0 until 12) {
                val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
                val px = cx + cos(angle) * 65f * scale
                val py = cy + sin(angle) * 65f * scale
                drawScope.drawCircle(theme.accentColor.copy(alpha = 0.7f), radius = 3f * scale, center = Offset(px, py))
            }
        }
        BoardPattern.NEON_GRID -> {
            // Cyberpunk concentric segmented radar rings
            for (i in 0 until 4) {
                val angle = (i * 90.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * 25f * scale
                val sy = cy + sin(angle) * 25f * scale
                val ex = cx + cos(angle) * 78f * scale
                val ey = cy + sin(angle) * 78f * scale
                drawScope.drawLine(Color(0xFF00F5FF), Offset(sx, sy), Offset(ex, ey), strokeWidth = 2.2f * scale)
            }
        }
        BoardPattern.TOURNAMENT_BAIZE -> {
            // Gilded Laurel Championship Star
            for (i in 0 until 16) {
                val angle = (i * 22.5 * Math.PI / 180.0).toFloat()
                val len = if (i % 2 == 0) 75f else 55f
                val ex = cx + cos(angle) * len * scale
                val ey = cy + sin(angle) * len * scale
                drawScope.drawLine(Color(0xFFFFD700).copy(alpha = 0.5f), Offset(cx, cy), Offset(ex, ey), strokeWidth = 1.2f * scale)
            }
        }
        BoardPattern.OBSIDIAN_CARBON -> {
            // 24K Gold luxury geometric diamond rosette
            for (i in 0 until 8) {
                val angle = (i * 45.0 * Math.PI / 180.0).toFloat()
                val px = cx + cos(angle) * 60f * scale
                val py = cy + sin(angle) * 60f * scale
                drawScope.drawCircle(Color(0xFFFFD700), radius = 4f * scale, center = Offset(px, py))
            }
        }
        BoardPattern.SOLAR_FLAME -> {
            // 12-point blazing solar corona rays
            for (i in 0 until 12) {
                val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * 22f * scale
                val sy = cy + sin(angle) * 22f * scale
                val ex = cx + cos(angle) * 78f * scale
                val ey = cy + sin(angle) * 78f * scale
                drawScope.drawLine(Color(0xFFFFAB00), Offset(sx, sy), Offset(ex, ey), strokeWidth = 2.5f * scale)
            }
        }
        BoardPattern.COSMIC_NEBULA -> {
            // Orbital constellation ellipses & stellar burst
            for (i in 0 until 6) {
                val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
                val ex = cx + cos(angle) * 75f * scale
                val ey = cy + sin(angle) * 75f * scale
                drawScope.drawCircle(Color(0xFF82B1FF), radius = 3.5f * scale, center = Offset(ex, ey))
            }
        }
        BoardPattern.ICE_GLACIER -> {
            // 6-fold Nordic snowflake crystal spikes
            for (i in 0 until 6) {
                val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * 22f * scale
                val sy = cy + sin(angle) * 22f * scale
                val ex = cx + cos(angle) * 76f * scale
                val ey = cy + sin(angle) * 76f * scale
                drawScope.drawLine(Color(0xFF00B0FF), Offset(sx, sy), Offset(ex, ey), strokeWidth = 2f * scale)
            }
        }
    }
}

private fun drawCornerBrackets(drawScope: DrawScope, theme: BoardTheme, scale: Float) {
    val corners = listOf(
        Pair(Offset(35f * scale, 35f * scale), Pair(1f, 1f)),
        Pair(Offset(765f * scale, 35f * scale), Pair(-1f, 1f)),
        Pair(Offset(35f * scale, 765f * scale), Pair(1f, -1f)),
        Pair(Offset(765f * scale, 765f * scale), Pair(-1f, -1f))
    )

    val bracketColor = theme.borderInnerTrim.copy(alpha = 0.65f)
    for ((pt, dir) in corners) {
        val (dx, dy) = dir
        val sizeVal = 24f * scale
        val path = Path().apply {
            moveTo(pt.x, pt.y + dy * sizeVal)
            lineTo(pt.x, pt.y)
            lineTo(pt.x + dx * sizeVal, pt.y)
        }
        drawScope.drawPath(path, color = bracketColor, style = Stroke(width = 2f * scale))
    }
}

private fun drawPockets(drawScope: DrawScope, theme: BoardTheme, scale: Float) {
    for (pocketCenter in CarromEngine.POCKETS) {
        val px = pocketCenter.x * scale
        val py = pocketCenter.y * scale
        val r = CarromEngine.POCKET_RADIUS * scale

        // 1. Outer pocket metallic/neon ring
        drawScope.drawCircle(
            color = theme.pocketRingColor,
            radius = r + 4.5f * scale,
            center = Offset(px, py),
            style = Stroke(width = 3.5f * scale)
        )

        // 2. Deep pocket hole gradient (pitch black depth)
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF050508), Color(0xFF181820)),
                center = Offset(px, py),
                radius = r
            ),
            radius = r,
            center = Offset(px, py)
        )

        // 3. Decorative pocket mesh/cross netting
        val netColor = if (theme.isNeon) Color(0xFF00F5FF).copy(alpha = 0.35f) else Color.LightGray.copy(alpha = 0.35f)
        drawScope.drawLine(netColor, Offset(px - r * 0.7f, py - r * 0.7f), Offset(px + r * 0.7f, py + r * 0.7f), strokeWidth = 1f * scale)
        drawScope.drawLine(netColor, Offset(px + r * 0.7f, py - r * 0.7f), Offset(px - r * 0.7f, py + r * 0.7f), strokeWidth = 1f * scale)
    }
}

private fun drawCoinEntity(
    drawScope: DrawScope,
    coin: Coin,
    theme: CoinTheme,
    strikerDesign: StrikerDesign,
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
        drawStrikerEntity(drawScope, coin, strikerDesign, avgScale, scaleX, scaleY)
    } else {
        // Draw resin carrommen coin with concentric 3D bevels and groove ridges
        drawScope.drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = alphaValue), primaryColor.copy(alpha = alphaValue * 0.75f)),
                center = Offset(cx, cy),
                radius = radius
            ),
            radius = radius,
            center = Offset(cx, cy)
        )

        // Outer coin edge rim highlight
        drawScope.drawCircle(
            color = Color.White.copy(alpha = 0.25f * alphaValue),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 1f * avgScale)
        )

        // Concentric grooved ring ridge
        drawScope.drawCircle(
            color = centerColor.copy(alpha = alphaValue),
            radius = radius * 0.65f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.6f * avgScale)
        )

        // Concentric inner ring
        drawScope.drawCircle(
            color = centerColor.copy(alpha = alphaValue),
            radius = radius * 0.35f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f * avgScale)
        )

        // Center dot
        drawScope.drawCircle(
            color = primaryColor.copy(alpha = alphaValue),
            radius = radius * 0.15f,
            center = Offset(cx, cy)
        )
    }
}

fun drawStrikerEntity(
    drawScope: DrawScope,
    striker: Coin,
    design: StrikerDesign,
    avgScale: Float,
    scaleX: Float,
    scaleY: Float
) {
    val cx = striker.x * scaleX
    val cy = striker.y * scaleY
    val radius = striker.radius * avgScale * striker.scale
    val alphaValue = striker.opacity

    if (radius <= 0.1f) return

    when (design.style) {
        StrikerStyle.IVORY_GOLD -> {
            // 1. Outer 24K Gold Beveled Rim
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE082).copy(alpha = alphaValue), Color(0xFFFFB300).copy(alpha = alphaValue)),
                    center = Offset(cx, cy),
                    radius = radius
                ),
                radius = radius,
                center = Offset(cx, cy)
            )

            // 2. Hand-carved Ivory Body
            drawScope.drawCircle(
                color = design.innerBodyColor.copy(alpha = alphaValue),
                radius = radius * 0.82f,
                center = Offset(cx, cy)
            )

            // 3. 8-Point Engraved Gold Star
            for (i in 0 until 8) {
                val angle = (i * 45.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * radius * 0.35f
                val sy = cy + sin(angle) * radius * 0.35f
                val ex = cx + cos(angle) * radius * 0.78f
                val ey = cy + sin(angle) * radius * 0.78f
                drawScope.drawLine(
                    color = Color(0xFFFFB300).copy(alpha = alphaValue),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 2f * avgScale
                )
            }

            // 4. Center Crimson Ruby Gem
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.32f,
                center = Offset(cx, cy)
            )
            drawScope.drawCircle(
                color = Color.White.copy(alpha = 0.8f * alphaValue),
                radius = radius * 0.12f,
                center = Offset(cx - radius * 0.08f, cy - radius * 0.08f)
            )
        }
        StrikerStyle.CYBER_PULSAR -> {
            // Glowing Neon Cyan Rim
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy),
                style = Stroke(width = 3f * avgScale)
            )
            // Dark Carbon Core
            drawScope.drawCircle(
                color = design.innerBodyColor.copy(alpha = alphaValue),
                radius = radius * 0.90f,
                center = Offset(cx, cy)
            )
            // Neon Magenta Reactor Ring
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.55f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f * avgScale)
            )
            // 4 Cross Plasma Nodes
            for (i in 0 until 4) {
                val angle = (i * 90.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * radius * 0.25f
                val sy = cy + sin(angle) * radius * 0.25f
                val ex = cx + cos(angle) * radius * 0.80f
                val ey = cy + sin(angle) * radius * 0.80f
                drawScope.drawLine(
                    color = Color(0xFF00F5FF).copy(alpha = alphaValue),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 2f * avgScale
                )
            }
            // Radiant Center Node
            drawScope.drawCircle(
                color = Color.White.copy(alpha = alphaValue),
                radius = radius * 0.20f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.ROYAL_RUBY -> {
            // Gilded Victorian Gold Bezel
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Translucent Faceted Ruby Core
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF5252).copy(alpha = alphaValue), Color(0xFFB71C1C).copy(alpha = alphaValue)),
                    center = Offset(cx, cy),
                    radius = radius * 0.82f
                ),
                radius = radius * 0.82f,
                center = Offset(cx, cy)
            )
            // Ornate Gold Crown Rosette
            drawScope.drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alphaValue),
                radius = radius * 0.45f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.2f * avgScale)
            )
            // Brilliant Diamond Sparkle Core
            drawScope.drawCircle(
                color = Color.White.copy(alpha = alphaValue),
                radius = radius * 0.18f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.OBSIDIAN_ECLIPSE -> {
            // Matte Tungsten Carbide Rim
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Dark Obsidian Slate
            drawScope.drawCircle(
                color = design.innerBodyColor.copy(alpha = alphaValue),
                radius = radius * 0.85f,
                center = Offset(cx, cy)
            )
            // Ultraviolet Geometric Runes
            for (i in 0 until 6) {
                val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * radius * 0.35f
                val sy = cy + sin(angle) * radius * 0.35f
                val ex = cx + cos(angle) * radius * 0.75f
                val ey = cy + sin(angle) * radius * 0.75f
                drawScope.drawLine(
                    color = Color(0xFFD500F9).copy(alpha = alphaValue),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 2f * avgScale
                )
            }
            // Glowing Plasma Center
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.28f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.EMERALD_DRAGON -> {
            // Imperial Bronze Scale Rim
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Polished Jade Stone Body
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2E7D32).copy(alpha = alphaValue), Color(0xFF1B5E20).copy(alpha = alphaValue)),
                    center = Offset(cx, cy),
                    radius = radius * 0.82f
                ),
                radius = radius * 0.82f,
                center = Offset(cx, cy)
            )
            // Golden Talisman Spiral Ring
            drawScope.drawCircle(
                color = Color(0xFFFFD700).copy(alpha = alphaValue),
                radius = radius * 0.50f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.2f * avgScale)
            )
            // Glowing Jade Pip
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.25f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.SOLAR_PHOENIX -> {
            // Molten Copper Ring
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Radiant Amber-Orange Sunstone
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFF9100).copy(alpha = alphaValue), Color(0xFFDD2C00).copy(alpha = alphaValue)),
                    center = Offset(cx, cy),
                    radius = radius * 0.82f
                ),
                radius = radius * 0.82f,
                center = Offset(cx, cy)
            )
            // 8 Solar Flare Spikes
            for (i in 0 until 8) {
                val angle = (i * 45.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * radius * 0.35f
                val sy = cy + sin(angle) * radius * 0.35f
                val ex = cx + cos(angle) * radius * 0.78f
                val ey = cy + sin(angle) * radius * 0.78f
                drawScope.drawLine(
                    color = Color(0xFFFFD600).copy(alpha = alphaValue),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 2.4f * avgScale
                )
            }
            // Blazing White-Hot Core
            drawScope.drawCircle(
                color = Color.White.copy(alpha = alphaValue),
                radius = radius * 0.22f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.DIAMOND_CRYSTAL -> {
            // Faceted Platinum Silver Rim
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // Prismatic Diamond Crystal Core
            drawScope.drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFFFFF).copy(alpha = alphaValue), Color(0xFFB0BEC5).copy(alpha = alphaValue)),
                    center = Offset(cx, cy),
                    radius = radius * 0.82f
                ),
                radius = radius * 0.82f,
                center = Offset(cx, cy)
            )
            // Diamond Geometric Facet Lines
            for (i in 0 until 6) {
                val angle = (i * 60.0 * Math.PI / 180.0).toFloat()
                val sx = cx + cos(angle) * radius * 0.25f
                val sy = cy + sin(angle) * radius * 0.25f
                val ex = cx + cos(angle) * radius * 0.78f
                val ey = cy + sin(angle) * radius * 0.78f
                drawScope.drawLine(
                    color = Color(0xFF00B0FF).copy(alpha = alphaValue),
                    start = Offset(sx, sy),
                    end = Offset(ex, ey),
                    strokeWidth = 1.8f * avgScale
                )
            }
            // Sapphire Drop Core
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.25f,
                center = Offset(cx, cy)
            )
        }
        StrikerStyle.STEAMPUNK_CHRONO -> {
            // Antique Brass Cog Gear Rim
            drawScope.drawCircle(
                color = design.outerRingColor.copy(alpha = alphaValue),
                radius = radius,
                center = Offset(cx, cy)
            )
            // 12 Gear Teeth Notches
            for (i in 0 until 12) {
                val angle = (i * 30.0 * Math.PI / 180.0).toFloat()
                val ex = cx + cos(angle) * radius * 0.98f
                val ey = cy + sin(angle) * radius * 0.98f
                drawScope.drawCircle(
                    color = Color(0xFF5D4037).copy(alpha = alphaValue),
                    radius = 2.5f * avgScale,
                    center = Offset(ex, ey)
                )
            }
            // Dark Bronze Body
            drawScope.drawCircle(
                color = design.innerBodyColor.copy(alpha = alphaValue),
                radius = radius * 0.80f,
                center = Offset(cx, cy)
            )
            // Interlocking Clockwork Gear Ring
            drawScope.drawCircle(
                color = Color(0xFFFFB300).copy(alpha = alphaValue),
                radius = radius * 0.50f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5f * avgScale)
            )
            // Center Brass Bolt
            drawScope.drawCircle(
                color = design.centerGemColor.copy(alpha = alphaValue),
                radius = radius * 0.26f,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun MiniBoardPreview(
    theme: BoardTheme,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(theme.borderColor, shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
            .border(1.dp, theme.borderInnerTrim, shape = RoundedCornerShape(4.dp))
            .background(theme.boardColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scale = size.width / 400f
            // Background surface
            drawBoardSurfacePattern(this, theme, size, scale)

            val cx = size.width / 2f
            val cy = size.height / 2f

            // Mini center mandala
            drawCenterMandalaForTheme(this, theme, cx, cy, scale * 0.5f)

            // Mini Pockets
            val pRadius = 7f * scale
            val pockets = listOf(
                Offset(pRadius + 3f, pRadius + 3f),
                Offset(size.width - pRadius - 3f, pRadius + 3f),
                Offset(pRadius + 3f, size.height - pRadius - 3f),
                Offset(size.width - pRadius - 3f, size.height - pRadius - 3f)
            )
            for (p in pockets) {
                drawCircle(theme.pocketRingColor, radius = pRadius + 1.5f, center = p, style = Stroke(width = 1.5f))
                drawCircle(Color(0xFF0F0F12), radius = pRadius, center = p)
            }
        }
    }
}

@Composable
fun MiniStrikerPreview(
    design: StrikerDesign,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(Color(0xFF16171D), shape = CircleShape)
            .padding(4.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dummyStriker = Coin(
                id = 999,
                x = 200f,
                y = 200f,
                mass = 3.5f,
                radius = 70f,
                type = CoinType.STRIKER
            )
            val scale = size.width / 400f
            drawStrikerEntity(this, dummyStriker, design, scale, scale, scale)
        }
    }
}
