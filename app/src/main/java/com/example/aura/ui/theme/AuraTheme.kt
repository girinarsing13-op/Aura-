package com.example.aura.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Bundled Inter font family installed locally in res/font/inter.ttf.
 * Providing the regular face allows Compose's default FontSynthesis to render
 * crisp Regular (400), SemiBold (600), and Bold (700) weights cleanly.
 */
val InterFontFamily = FontFamily(
    Font(R.font.inter)
)

val AuraTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 44.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = (-0.25).sp
    ),
    displaySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Authoritative Centralized Semantic Theme System for AURA.
 * Matches the approved AURA design preview tokens for both Dark and Light modes,
 * while preserving all semantic properties used across the application.
 */
@Immutable
data class AuraColorPalette(
    val isDark: Boolean = true,

    // Core Surfaces
    val background: Color = Color(0xFF0B0B0D),
    val surface: Color = Color(0xFF151518),
    val surface2: Color = Color(0xFF1E1E22),
    val surface3: Color = Color(0xFF26262B),
    val surfaceSecondary: Color = Color(0xFF1E1E22),
    val trackMuted: Color = Color(0xFF2A2A2F),
    val card: Color = Color(0xFF151518),
    val cardBorder: Color = Color(0x14FFFFFF), // white 8%
    val dialogBackground: Color = Color(0xFF151518),
    val sheetBackground: Color = Color(0xFF151518),

    // Typography
    val primaryText: Color = Color(0xFFF4F4F5),
    val secondaryText: Color = Color(0xFFA1A1AA),
    val mutedText: Color = Color(0xFFA1A1AA),

    // Borders & Dividers
    val border: Color = Color(0x14FFFFFF), // white 8%
    val divider: Color = Color(0x14FFFFFF),

    // Icons
    val icon: Color = Color(0xFFF4F4F5),
    val iconMuted: Color = Color(0xFFA1A1AA),

    // Accents & Secondary Data Colors
    val accent: Color = Color(0xFFE8B64C),
    val accentTint: Color = Color(0xFF1A1812),
    val accentSurface: Color = Color(0x29E8B64C),
    val accentContrast: Color = Color(0xFF111111),
    val proteinGreen: Color = Color(0xFF7DD3A8),
    val waterBlue: Color = Color(0xFF6CB4F5),
    val warningCoral: Color = Color(0xFFF58C6C),
    val success: Color = Color(0xFF7DD3A8),
    val warning: Color = Color(0xFFF58C6C),
    val error: Color = Color(0xFFF58C6C),

    // Inputs & Timeline / Ruler Graphics
    val input: Color = Color(0xFF1E1E22),
    val inputBorder: Color = Color(0x1FFFFFFF),
    val tickColor: Color = Color(0xFF4A4A52),
    val sleepBlockColor: Color = Color(0xFF26262B),
    val busyBlockColor: Color = Color(0xFF3A3A40),
    val legendSleepColor: Color = Color(0xFF4A4A52),
    val legendBusyColor: Color = Color(0xFF6A6A72),

    // Floating Pill Navigation
    val navPillBg: Color = Color(0xEB222226), // rgba(34,34,38,.92)
    val navPillBorder: Color = Color(0x1AFFFFFF), // white 10%
    val navigation: Color = Color(0xEB222226),
    val navigationBorder: Color = Color(0x1AFFFFFF),
    val navigationIndicatorBg: Color = Color(0xFFE8B64C),
    val navigationIndicatorBorder: Color = Color(0xFFE8B64C),
    val navigationIconActive: Color = Color(0xFF111111),
    val navigationIconInactive: Color = Color(0xFFA1A1AA),
    val activeNavigation: Color = Color(0xFFE8B64C),

    // Charts & Analytics
    val chartPrimary: Color = Color(0xFFE8B64C),
    val chartSecondary: Color = Color(0xFF6CB4F5),

    // Legacy Speech Bubble styling
    val auraSpeechBubbleBg: Color = Color(0xF5151518),
    val auraSpeechBubbleText: Color = Color(0xFFF4F4F5),

    // Backward compatibility aliases
    val bg: Color = background,
    val elevated: Color = surface2,
    val text: Color = primaryText,
    val textMuted: Color = secondaryText,
    val danger: Color = error
)

val LocalAuraColors = staticCompositionLocalOf { AuraColorPalette() }

object ThemeEngine {
    const val DEFAULT_ACCENT_HEX = "#E8B64C"
    const val LEGACY_ACCENT_HEX = "#C9A876"

    const val MODE_DARK = "DARK"
    const val MODE_LIGHT = "LIGHT"
    const val MODE_SYSTEM = "SYSTEM"

    val MODES = listOf(MODE_DARK, MODE_LIGHT, MODE_SYSTEM)

    val PRESETS = listOf(
        // Luxe & Signature
        ThemePreset("Aura Gold", "#E8B64C", "Luxe"),
        ThemePreset("Champagne Gold", "#C9A876", "Luxe"),
        ThemePreset("Royal Gold", "#FFD700", "Luxe"),
        ThemePreset("Warm Amber", "#E0A96D", "Luxe"),
        ThemePreset("Sunset Bronze", "#CD7F32", "Luxe"),

        // Cyber & Neon
        ThemePreset("Volt Lime", "#D4FF00", "Cyber"),
        ThemePreset("Cyber Cyan", "#00F0FF", "Cyber"),
        ThemePreset("Electric Blue", "#3B82F6", "Cyber"),
        ThemePreset("Neon Violet", "#A855F7", "Cyber"),
        ThemePreset("Hyper Magenta", "#EC4899", "Cyber"),

        // Fire & Energy
        ThemePreset("Coral Pulse", "#F58C6C", "Energy"),
        ThemePreset("Crimson Pulse", "#EF4444", "Energy"),
        ThemePreset("Blaze Orange", "#F97316", "Energy"),
        ThemePreset("Solar Flare", "#F59E0B", "Energy"),

        // Cool & Stealth
        ThemePreset("Sky Blue", "#6CB4F5", "Cool"),
        ThemePreset("Deep Teal", "#14B8A6", "Cool"),
        ThemePreset("Aqua Marine", "#2DD4BF", "Cool"),
        ThemePreset("Stealth Lavender", "#818CF8", "Cool"),
        ThemePreset("Titanium Silver", "#CBD5E1", "Cool"),

        // Nature & Vitality
        ThemePreset("Mint Protein", "#7DD3A8", "Vitality"),
        ThemePreset("Emerald Green", "#10B981", "Vitality"),
        ThemePreset("Sage Green", "#81C784", "Vitality")
    )

    val CATEGORIES = listOf("All", "Luxe", "Cyber", "Energy", "Cool", "Vitality")

    fun resolveAccentHex(rawHex: String): String {
        val trimmed = rawHex.trim()
        if (trimmed.isEmpty() || trimmed.equals(LEGACY_ACCENT_HEX, ignoreCase = true)) {
            return DEFAULT_ACCENT_HEX
        }
        return if (isValidHex(trimmed)) normalizeHex(trimmed) else DEFAULT_ACCENT_HEX
    }

    fun isValidHex(hex: String): Boolean {
        val clean = hex.removePrefix("#").trim()
        return (clean.length == 3 || clean.length == 6) &&
            clean.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
    }

    fun normalizeHex(hex: String): String {
        val clean = hex.removePrefix("#").trim()
        val full = if (clean.length == 3) {
            clean.map { "$it$it" }.joinToString("")
        } else clean
        return "#${full.uppercase()}"
    }

    fun parseHex(hex: String, fallback: Color = Color(0xFFE8B64C)): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val fullHex = if (cleanHex.length == 3) {
                cleanHex.map { "$it$it" }.joinToString("")
            } else cleanHex
            Color(android.graphics.Color.parseColor("#$fullHex"))
        } catch (_: Exception) {
            fallback
        }
    }

    fun getContrastColor(hex: String): Color {
        return try {
            val cleanHex = hex.removePrefix("#")
            val fullHex = if (cleanHex.length == 3) {
                cleanHex.map { "$it$it" }.joinToString("")
            } else cleanHex
            val colorInt = android.graphics.Color.parseColor("#$fullHex")
            val r = (colorInt shr 16) and 0xFF
            val g = (colorInt shr 8) and 0xFF
            val b = colorInt and 0xFF
            val yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000
            if (yiq >= 150) Color(0xFF111111) else Color(0xFFFFFFFF)
        } catch (_: Exception) {
            Color(0xFF111111)
        }
    }

    fun getAdaptiveLightAccent(hex: String): Color {
        val normalized = normalizeHex(hex)
        if (normalized.equals(DEFAULT_ACCENT_HEX, ignoreCase = true)) {
            return Color(0xFFE8B64C)
        }
        return try {
            val baseColor = parseHex(hex)
            val cleanHex = normalized.removePrefix("#")
            val colorInt = android.graphics.Color.parseColor("#$cleanHex")
            val r = (colorInt shr 16) and 0xFF
            val g = (colorInt shr 8) and 0xFF
            val b = colorInt and 0xFF
            val yiq = ((r * 299) + (g * 587) + (b * 114)) / 1000
            if (yiq > 205) {
                Color(
                    red = ((r * 0.76f) / 255f).coerceIn(0f, 1f),
                    green = ((g * 0.76f) / 255f).coerceIn(0f, 1f),
                    blue = ((b * 0.76f) / 255f).coerceIn(0f, 1f),
                    alpha = 1.0f
                )
            } else {
                baseColor
            }
        } catch (_: Exception) {
            Color(0xFFE8B64C)
        }
    }
}

data class ThemePreset(
    val name: String,
    val hex: String,
    val category: String = "Luxe"
)

fun darkAuraPalette(accentColor: Color, accentContrast: Color): AuraColorPalette {
    val isDefaultGold = accentColor == Color(0xFFE8B64C)
    val selectedTint = if (isDefaultGold) {
        Color(0xFF1A1812)
    } else {
        Color(
            red = (0x15 / 255f) * 0.88f + accentColor.red * 0.12f,
            green = (0x15 / 255f) * 0.88f + accentColor.green * 0.12f,
            blue = (0x18 / 255f) * 0.88f + accentColor.blue * 0.12f,
            alpha = 1f
        )
    }
    return AuraColorPalette(
        isDark = true,
        background = Color(0xFF0B0B0D),
        surface = Color(0xFF151518),
        surface2 = Color(0xFF1E1E22),
        surface3 = Color(0xFF26262B),
        surfaceSecondary = Color(0xFF1E1E22),
        trackMuted = Color(0xFF2A2A2F),
        card = Color(0xFF151518),
        cardBorder = Color(0x14FFFFFF), // white 8%
        dialogBackground = Color(0xFF151518),
        sheetBackground = Color(0xFF151518),
        primaryText = Color(0xFFF4F4F5),
        secondaryText = Color(0xFFA1A1AA),
        mutedText = Color(0xFFA1A1AA),
        border = Color(0x14FFFFFF),
        divider = Color(0x14FFFFFF),
        icon = Color(0xFFF4F4F5),
        iconMuted = Color(0xFFA1A1AA),
        accent = accentColor,
        accentTint = selectedTint,
        accentSurface = accentColor.copy(alpha = 0.16f),
        accentContrast = accentContrast,
        proteinGreen = Color(0xFF7DD3A8),
        waterBlue = Color(0xFF6CB4F5),
        warningCoral = Color(0xFFF58C6C),
        success = Color(0xFF7DD3A8),
        warning = Color(0xFFF58C6C),
        error = Color(0xFFF58C6C),
        input = Color(0xFF1E1E22),
        inputBorder = Color(0x1FFFFFFF),
        tickColor = Color(0xFF4A4A52),
        sleepBlockColor = Color(0xFF26262B),
        busyBlockColor = Color(0xFF3A3A40),
        legendSleepColor = Color(0xFF4A4A52),
        legendBusyColor = Color(0xFF6A6A72),
        navPillBg = Color(0xEB222226),
        navPillBorder = Color(0x1AFFFFFF),
        navigation = Color(0xEB222226),
        navigationBorder = Color(0x1AFFFFFF),
        navigationIndicatorBg = accentColor,
        navigationIndicatorBorder = accentColor,
        navigationIconActive = accentContrast,
        navigationIconInactive = Color(0xFFA1A1AA),
        activeNavigation = accentColor,
        chartPrimary = accentColor,
        chartSecondary = Color(0xFF6CB4F5),
        auraSpeechBubbleBg = Color(0xF5151518),
        auraSpeechBubbleText = Color(0xFFF4F4F5)
    )
}

fun lightAuraPalette(accentColor: Color, accentContrast: Color): AuraColorPalette {
    val selectedTint = Color(
        red = 0.99f * 0.91f + accentColor.red * 0.09f,
        green = 0.99f * 0.91f + accentColor.green * 0.09f,
        blue = 0.99f * 0.91f + accentColor.blue * 0.09f,
        alpha = 1f
    )
    return AuraColorPalette(
        isDark = false,
        background = Color(0xFFF6F6F8),
        surface = Color(0xFFFFFFFF),
        surface2 = Color(0xFFEEEEF2),
        surface3 = Color(0xFFE4E4EA),
        surfaceSecondary = Color(0xFFEEEEF2),
        trackMuted = Color(0xFFE2E2E8),
        card = Color(0xFFFFFFFF),
        cardBorder = Color(0x14000000), // black 8%
        dialogBackground = Color(0xFFFFFFFF),
        sheetBackground = Color(0xFFFFFFFF),
        primaryText = Color(0xFF111114),
        secondaryText = Color(0xFF52525B),
        mutedText = Color(0xFF71717A),
        border = Color(0x14000000),
        divider = Color(0x14000000),
        icon = Color(0xFF18181B),
        iconMuted = Color(0xFF71717A),
        accent = accentColor,
        accentTint = selectedTint,
        accentSurface = accentColor.copy(alpha = 0.16f),
        accentContrast = accentContrast,
        proteinGreen = Color(0xFF16A368),
        waterBlue = Color(0xFF2589E6),
        warningCoral = Color(0xFFD96444),
        success = Color(0xFF16A368),
        warning = Color(0xFFD96444),
        error = Color(0xFFDC2626),
        input = Color(0xFFEEEEF2),
        inputBorder = Color(0x1F000000),
        tickColor = Color(0xFFA1A1AA),
        sleepBlockColor = Color(0xFFE4E4EC),
        busyBlockColor = Color(0xFFD4D4DC),
        legendSleepColor = Color(0xFFA1A1AA),
        legendBusyColor = Color(0xFF71717A),
        navPillBg = Color(0xF2FFFFFF),
        navPillBorder = Color(0x18000000),
        navigation = Color(0xF2FFFFFF),
        navigationBorder = Color(0x18000000),
        navigationIndicatorBg = accentColor,
        navigationIndicatorBorder = accentColor,
        navigationIconActive = accentContrast,
        navigationIconInactive = Color(0xFF71717A),
        activeNavigation = accentColor,
        chartPrimary = accentColor,
        chartSecondary = Color(0xFF2589E6),
        auraSpeechBubbleBg = Color(0xF8FFFFFF),
        auraSpeechBubbleText = Color(0xFF111114)
    )
}

@Composable
fun AuraTheme(
    accentHex: String = ThemeEngine.DEFAULT_ACCENT_HEX,
    themeMode: String = ThemeEngine.MODE_SYSTEM,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        ThemeEngine.MODE_DARK -> true
        ThemeEngine.MODE_LIGHT -> false
        else -> systemInDark
    }

    val resolvedHex = ThemeEngine.resolveAccentHex(accentHex)
    val rawAccentColor = ThemeEngine.parseHex(resolvedHex)
    val accentColor = if (isDark) rawAccentColor else ThemeEngine.getAdaptiveLightAccent(resolvedHex)
    val accentContrast = ThemeEngine.getContrastColor(resolvedHex)

    val auraColors = if (isDark) {
        darkAuraPalette(accentColor, accentContrast)
    } else {
        lightAuraPalette(accentColor, accentContrast)
    }

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = accentColor,
            onPrimary = accentContrast,
            secondary = auraColors.proteinGreen,
            tertiary = auraColors.waterBlue,
            error = auraColors.warningCoral,
            background = auraColors.background,
            onBackground = auraColors.primaryText,
            surface = auraColors.surface,
            onSurface = auraColors.primaryText,
            surfaceVariant = auraColors.surface2,
            onSurfaceVariant = auraColors.secondaryText,
            outline = auraColors.border
        )
    } else {
        lightColorScheme(
            primary = accentColor,
            onPrimary = accentContrast,
            secondary = auraColors.proteinGreen,
            tertiary = auraColors.waterBlue,
            error = auraColors.warningCoral,
            background = auraColors.background,
            onBackground = auraColors.primaryText,
            surface = auraColors.surface,
            onSurface = auraColors.primaryText,
            surfaceVariant = auraColors.surface2,
            onSurfaceVariant = auraColors.secondaryText,
            outline = auraColors.border
        )
    }

    CompositionLocalProvider(LocalAuraColors provides auraColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = AuraTypography,
            shapes = AuraShapes,
            content = content
        )
    }
}

object AuraTheme {
    val colors: AuraColorPalette
        @Composable
        get() = LocalAuraColors.current
}
