package com.example.model

import androidx.compose.ui.graphics.Color

enum class CoinType {
    WHITE,
    BLACK,
    QUEEN,
    STRIKER
}

enum class BoardTheme(
    val displayName: String,
    val boardColor: Color,
    val borderColor: Color,
    val centerCircleColor: Color,
    val cornerCircleColor: Color,
    val textColor: Color,
    val isNeon: Boolean = false
) {
    WOODEN(
        displayName = "Classic Wooden",
        boardColor = Color(0xFFEEDAB2),
        borderColor = Color(0xFF6B4226),
        centerCircleColor = Color(0xFF9E5C3B),
        cornerCircleColor = Color(0xFFA67153),
        textColor = Color(0xFF42220F)
    ),
    PREMIUM(
        displayName = "Premium Royal",
        boardColor = Color(0xFF0D1B2A),
        borderColor = Color(0xFFE5A93C),
        centerCircleColor = Color(0xFF1B263B),
        cornerCircleColor = Color(0xFF415A77),
        textColor = Color(0xFFE0E1DD)
    ),
    NEON(
        displayName = "Cyberpunk Neon",
        boardColor = Color(0xFF110022),
        borderColor = Color(0xFFFF0055),
        centerCircleColor = Color(0xFF00FFCC),
        cornerCircleColor = Color(0xFF390099),
        textColor = Color(0xFF00FFCC),
        isNeon = true
    ),
    DARK(
        displayName = "Carbon Minimal",
        boardColor = Color(0xFF1C1C1E),
        borderColor = Color(0xFF2C2C2E),
        centerCircleColor = Color(0xFF333333),
        cornerCircleColor = Color(0xFF444444),
        textColor = Color(0xFFF2F2F7)
    ),
    CUSTOM(
        displayName = "Turquoise Mint",
        boardColor = Color(0xFFE0F2F1),
        borderColor = Color(0xFF00796B),
        centerCircleColor = Color(0xFF80CBC4),
        cornerCircleColor = Color(0xFF4DB6AC),
        textColor = Color(0xFF004D40)
    )
}

enum class CoinTheme(
    val displayName: String,
    val whiteCoinColor: Color,
    val whiteCoinInner: Color,
    val blackCoinColor: Color,
    val blackCoinInner: Color,
    val queenCoinColor: Color,
    val queenCoinInner: Color
) {
    CLASSIC(
        displayName = "Classic Ivory",
        whiteCoinColor = Color(0xFFF7EBE1),
        whiteCoinInner = Color(0xFFC7B198),
        blackCoinColor = Color(0xFF212121),
        blackCoinInner = Color(0xFF424242),
        queenCoinColor = Color(0xFFD32F2F),
        queenCoinInner = Color(0xFFFF8A80)
    ),
    RED_BLUE(
        displayName = "Crimson & Indigo",
        whiteCoinColor = Color(0xFFFF3D00),
        whiteCoinInner = Color(0xFFFFC400),
        blackCoinColor = Color(0xFF2979FF),
        blackCoinInner = Color(0xFF00E5FF),
        queenCoinColor = Color(0xFFD500F9),
        queenCoinInner = Color(0xFFF50057)
    ),
    GREEN_YELLOW(
        displayName = "Retro Arcade",
        whiteCoinColor = Color(0xFF00E676),
        whiteCoinInner = Color(0xFF1DE9B6),
        blackCoinColor = Color(0xFFFFEA00),
        blackCoinInner = Color(0xFFFF9100),
        queenCoinColor = Color(0xFFFF1744),
        queenCoinInner = Color(0xFFD500F9)
    ),
    NEON_GLOW(
        displayName = "Grid Glow",
        whiteCoinColor = Color(0xFF00FFFF),
        whiteCoinInner = Color(0xFF008080),
        blackCoinColor = Color(0xFFFF00FF),
        blackCoinInner = Color(0xFF800080),
        queenCoinColor = Color(0xFFFFFF00),
        queenCoinInner = Color(0xFF808000)
    ),
    GOLD_SILVER(
        displayName = "Precious Metal",
        whiteCoinColor = Color(0xFFCFD8DC),
        whiteCoinInner = Color(0xFFECEFF1),
        blackCoinColor = Color(0xFFFFE082),
        blackCoinInner = Color(0xFFFFB300),
        queenCoinColor = Color(0xFFFA5050),
        queenCoinInner = Color(0xFFFFEFEF)
    )
}

data class PhysicsVector(var x: Float, var y: Float) {
    operator fun plus(other: PhysicsVector) = PhysicsVector(this.x + other.x, this.y + other.y)
    operator fun minus(other: PhysicsVector) = PhysicsVector(this.x - other.x, this.y - other.y)
    operator fun times(scalar: Float) = PhysicsVector(this.x * scalar, this.y * scalar)
    fun length() = kotlin.math.sqrt(x * x + y * y)
    fun normalize(): PhysicsVector {
        val len = length()
        return if (len != 0f) PhysicsVector(x / len, y / len) else PhysicsVector(0f, 0f)
    }
}

data class Coin(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val mass: Float,
    val radius: Float,
    val type: CoinType,
    var isPocketed: Boolean = false,
    var scale: Float = 1.0f,
    var opacity: Float = 1.0f
) {
    fun updatePosition(friction: Float) {
        if (isPocketed) return
        x += vx
        y += vy
        vx *= friction
        vy *= friction

        if (kotlin.math.abs(vx) < 0.05f) vx = 0f
        if (kotlin.math.abs(vy) < 0.05f) vy = 0f
    }
}

data class Player(
    val id: Int,
    var name: String,
    var score: Int = 0,
    var coinsCollected: Int = 0
)
