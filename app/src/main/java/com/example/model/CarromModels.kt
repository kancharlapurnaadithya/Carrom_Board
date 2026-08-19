package com.example.model

import androidx.compose.ui.graphics.Color

enum class CoinType {
    WHITE,
    BLACK,
    QUEEN,
    STRIKER
}

enum class BoardPattern {
    WOOD_GRAIN,
    MARBLE_VEIN,
    NEON_GRID,
    TOURNAMENT_BAIZE,
    OBSIDIAN_CARBON,
    SOLAR_FLAME,
    COSMIC_NEBULA,
    ICE_GLACIER
}

enum class BoardTheme(
    val displayName: String,
    val subtitle: String,
    val boardColor: Color,
    val boardGradientSecondary: Color,
    val borderColor: Color,
    val borderInnerTrim: Color,
    val centerCircleColor: Color,
    val cornerCircleColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val pocketRingColor: Color,
    val pattern: BoardPattern,
    val isNeon: Boolean = false
) {
    WOODEN(
        displayName = "Heritage Teak Wood",
        subtitle = "Authentic polished teak with rosewood frame",
        boardColor = Color(0xFFEEDAB2),
        boardGradientSecondary = Color(0xFFDEB887),
        borderColor = Color(0xFF5C2C16),
        borderInnerTrim = Color(0xFFD4AF37),
        centerCircleColor = Color(0xFF9E5C3B),
        cornerCircleColor = Color(0xFFA67153),
        textColor = Color(0xFF42220F),
        accentColor = Color(0xFFC7823B),
        pocketRingColor = Color(0xFF3E1F0D),
        pattern = BoardPattern.WOOD_GRAIN
    ),
    PREMIUM(
        displayName = "Taj White Marble",
        subtitle = "Opulent Italian marble with royal gold filigree",
        boardColor = Color(0xFFF9F7F1),
        boardGradientSecondary = Color(0xFFEFE9DC),
        borderColor = Color(0xFF14213D),
        borderInnerTrim = Color(0xFFD4AF37),
        centerCircleColor = Color(0xFFC5A059),
        cornerCircleColor = Color(0xFF2E7D32),
        textColor = Color(0xFF14213D),
        accentColor = Color(0xFFD4AF37),
        pocketRingColor = Color(0xFF0F4C81),
        pattern = BoardPattern.MARBLE_VEIN
    ),
    NEON(
        displayName = "Cyberpunk Synthwave",
        subtitle = "Luminescent neon gridlines & laser rims",
        boardColor = Color(0xFF0D061A),
        boardGradientSecondary = Color(0xFF1B0B33),
        borderColor = Color(0xFFFF007F),
        borderInnerTrim = Color(0xFF00F5FF),
        centerCircleColor = Color(0xFF00F5FF),
        cornerCircleColor = Color(0xFFD500F9),
        textColor = Color(0xFF00F5FF),
        accentColor = Color(0xFFFF007F),
        pocketRingColor = Color(0xFFFF0055),
        pattern = BoardPattern.NEON_GRID,
        isNeon = true
    ),
    DARK(
        displayName = "Obsidian Gold Luxury",
        subtitle = "Stealth carbon slate with 24K gold accents",
        boardColor = Color(0xFF191A1E),
        boardGradientSecondary = Color(0xFF23252C),
        borderColor = Color(0xFF2B2D35),
        borderInnerTrim = Color(0xFFFFD700),
        centerCircleColor = Color(0xFFFFD700),
        cornerCircleColor = Color(0xFF757575),
        textColor = Color(0xFFF0F0F5),
        accentColor = Color(0xFFFFC107),
        pocketRingColor = Color(0xFFFFD700),
        pattern = BoardPattern.OBSIDIAN_CARBON
    ),
    CUSTOM(
        displayName = "Tournament Emerald",
        subtitle = "Championship velvet green with gilded laurels",
        boardColor = Color(0xFF114227),
        boardGradientSecondary = Color(0xFF1A5835),
        borderColor = Color(0xFF2A180E),
        borderInnerTrim = Color(0xFFFFD700),
        centerCircleColor = Color(0xFFFFD700),
        cornerCircleColor = Color(0xFF81C784),
        textColor = Color(0xFFE8F5E9),
        accentColor = Color(0xFFFFD700),
        pocketRingColor = Color(0xFF1B5E20),
        pattern = BoardPattern.TOURNAMENT_BAIZE
    ),
    SOLAR_CRIMSON(
        displayName = "Solar Sunburst",
        subtitle = "Blazing amber terracotta with sun-wheel mandala",
        boardColor = Color(0xFF3B1207),
        boardGradientSecondary = Color(0xFF5E1B09),
        borderColor = Color(0xFF240A03),
        borderInnerTrim = Color(0xFFFF7043),
        centerCircleColor = Color(0xFFFFAB00),
        cornerCircleColor = Color(0xFFFF5722),
        textColor = Color(0xFFFFE082),
        accentColor = Color(0xFFFF6D00),
        pocketRingColor = Color(0xFFD84315),
        pattern = BoardPattern.SOLAR_FLAME
    ),
    COSMIC_GALAXY(
        displayName = "Celestial Nebula",
        subtitle = "Deep cosmic indigo with stardust constellation",
        boardColor = Color(0xFF090918),
        boardGradientSecondary = Color(0xFF131333),
        borderColor = Color(0xFF1E1F3D),
        borderInnerTrim = Color(0xFF8C9EFF),
        centerCircleColor = Color(0xFF82B1FF),
        cornerCircleColor = Color(0xFFB388FF),
        textColor = Color(0xFFE8EAF6),
        accentColor = Color(0xFF536DFE),
        pocketRingColor = Color(0xFF3D5AFE),
        pattern = BoardPattern.COSMIC_NEBULA
    ),
    FROSTED_GLACIER(
        displayName = "Nordic Ice Glacier",
        subtitle = "Frosty arctic cyan with snowflake crystal rings",
        boardColor = Color(0xFFD6EEF5),
        boardGradientSecondary = Color(0xFFEBF8FC),
        borderColor = Color(0xFF1E3947),
        borderInnerTrim = Color(0xFF4FC3F7),
        centerCircleColor = Color(0xFF0288D1),
        cornerCircleColor = Color(0xFF00ACC1),
        textColor = Color(0xFF01579B),
        accentColor = Color(0xFF00B0FF),
        pocketRingColor = Color(0xFF00838F),
        pattern = BoardPattern.ICE_GLACIER
    )
}

enum class StrikerStyle {
    IVORY_GOLD,
    CYBER_PULSAR,
    ROYAL_RUBY,
    OBSIDIAN_ECLIPSE,
    EMERALD_DRAGON,
    SOLAR_PHOENIX,
    DIAMOND_CRYSTAL,
    STEAMPUNK_CHRONO
}

enum class StrikerDesign(
    val displayName: String,
    val subtitle: String,
    val outerRingColor: Color,
    val innerBodyColor: Color,
    val centerGemColor: Color,
    val accentGlowColor: Color,
    val particleColor: Color,
    val style: StrikerStyle
) {
    IVORY_GOLD_MASTER(
        displayName = "Champion Ivory & Gold",
        subtitle = "Polished 24K gold rim with royal star engraving",
        outerRingColor = Color(0xFFFFD700),
        innerBodyColor = Color(0xFFF9F7F2),
        centerGemColor = Color(0xFFD32F2F),
        accentGlowColor = Color(0xFFFFE082),
        particleColor = Color(0xFFFFD700),
        style = StrikerStyle.IVORY_GOLD
    ),
    CYBER_PULSAR(
        displayName = "Cyber Neon Pulsar",
        subtitle = "Electric neon cyan laser rim & plasma matrix",
        outerRingColor = Color(0xFF00F5FF),
        innerBodyColor = Color(0xFF120826),
        centerGemColor = Color(0xFFFF007F),
        accentGlowColor = Color(0xFF00E5FF),
        particleColor = Color(0xFF00F5FF),
        style = StrikerStyle.CYBER_PULSAR
    ),
    ROYAL_RUBY_SOVEREIGN(
        displayName = "Imperial Ruby Sovereign",
        subtitle = "Translucent faceted ruby crystal with gold crown",
        outerRingColor = Color(0xFFFFB300),
        innerBodyColor = Color(0xFFC62828),
        centerGemColor = Color(0xFFFF5252),
        accentGlowColor = Color(0xFFFF8A80),
        particleColor = Color(0xFFFF1744),
        style = StrikerStyle.ROYAL_RUBY
    ),
    OBSIDIAN_ECLIPSE(
        displayName = "Stealth Titan Obsidian",
        subtitle = "Matte tungsten carbide with glowing ultraviolet runes",
        outerRingColor = Color(0xFF37474F),
        innerBodyColor = Color(0xFF1E1E24),
        centerGemColor = Color(0xFFD500F9),
        accentGlowColor = Color(0xFF7C4DFF),
        particleColor = Color(0xFFD500F9),
        style = StrikerStyle.OBSIDIAN_ECLIPSE
    ),
    EMERALD_DRAGON(
        displayName = "Jade Mystical Dragon",
        subtitle = "Imperial jade with golden spiral talisman",
        outerRingColor = Color(0xFFC5A059),
        innerBodyColor = Color(0xFF1B5E20),
        centerGemColor = Color(0xFF69F0AE),
        accentGlowColor = Color(0xFF00E676),
        particleColor = Color(0xFF00E676),
        style = StrikerStyle.EMERALD_DRAGON
    ),
    SOLAR_PHOENIX(
        displayName = "Blazing Solar Flare",
        subtitle = "Sunburst molten crystal with phoenix radiant rays",
        outerRingColor = Color(0xFFFF6D00),
        innerBodyColor = Color(0xFFE65100),
        centerGemColor = Color(0xFFFFD600),
        accentGlowColor = Color(0xFFFFAB00),
        particleColor = Color(0xFFFF6D00),
        style = StrikerStyle.SOLAR_PHOENIX
    ),
    DIAMOND_CRYSTAL(
        displayName = "Prismatic Diamond Shard",
        subtitle = "Faceted platinum silver with icy sapphire core",
        outerRingColor = Color(0xFFCFD8DC),
        innerBodyColor = Color(0xFFECEFF1),
        centerGemColor = Color(0xFF00B0FF),
        accentGlowColor = Color(0xFF80DEEA),
        particleColor = Color(0xFF80DEEA),
        style = StrikerStyle.DIAMOND_CRYSTAL
    ),
    STEAMPUNK_CHRONO(
        displayName = "Vintage Brass Chrono",
        subtitle = "Antique brushed brass with mechanical gear cogs",
        outerRingColor = Color(0xFFB8860B),
        innerBodyColor = Color(0xFF4A2810),
        centerGemColor = Color(0xFFFFB300),
        accentGlowColor = Color(0xFFD84315),
        particleColor = Color(0xFFFFB300),
        style = StrikerStyle.STEAMPUNK_CHRONO
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

enum class AiDifficulty(
    val displayName: String,
    val description: String,
    val accuracyVariance: Float, // Aim angle variance in radians
    val powerVariance: Float,    // Power scale variance
    val strategicDepth: Int,     // Search breadth & obstacle checking
    val tag: String
) {
    NOVICE(
        displayName = "Novice",
        description = "Low accuracy, simple and mostly random decisions.",
        accuracyVariance = 0.24f,
        powerVariance = 0.35f,
        strategicDepth = 1,
        tag = "Casual"
    ),
    BEGINNER(
        displayName = "Beginner",
        description = "Moderate accuracy with basic decision-making.",
        accuracyVariance = 0.12f,
        powerVariance = 0.18f,
        strategicDepth = 2,
        tag = "Easy"
    ),
    INTERMEDIATE(
        displayName = "Intermediate",
        description = "Good accuracy with improved shot selection and positioning.",
        accuracyVariance = 0.045f,
        powerVariance = 0.07f,
        strategicDepth = 3,
        tag = "Balanced"
    ),
    EXPERT(
        displayName = "Expert",
        description = "High accuracy with strong strategic decision-making.",
        accuracyVariance = 0.015f,
        powerVariance = 0.025f,
        strategicDepth = 4,
        tag = "Hard"
    ),
    GRANDMASTER(
        displayName = "Grandmaster",
        description = "Very high accuracy with advanced strategy, optimal shot selection, and intelligent decision-making.",
        accuracyVariance = 0.000f,
        powerVariance = 0.000f,
        strategicDepth = 5,
        tag = "Master"
    )
}
