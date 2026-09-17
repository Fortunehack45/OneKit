package com.one.utility.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val isDark: Boolean,
    val canvasBackground: Color,
    val cardSurface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val borderSubtle: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val dockBackground: Color,
    val primaryButton: Color,
    val onPrimaryButton: Color,
    val bentoHoneySubtle: Color,
    val bentoSkySubtle: Color,
    val bentoPinkSubtle: Color,
    val bentoMintSubtle: Color
) {
    val surfaceCard: Color get() = cardSurface
    val textTertiary: Color get() = textMuted
}

private val LightAppColors = AppColors(
    isDark = false,
    canvasBackground = CanvasBackground,
    cardSurface = CanvasSurface,
    surfaceElevated = SurfaceElevated,
    surfaceVariant = Color(0xFFF1F0F7),
    borderSubtle = BorderSubtle,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textMuted = TextMuted,
    dockBackground = FrostedGlassLight,
    primaryButton = DockObsidian,
    onPrimaryButton = Color.White,
    bentoHoneySubtle = BentoHoneyLight,
    bentoSkySubtle = BentoSkyLight,
    bentoPinkSubtle = BentoPinkLight,
    bentoMintSubtle = BentoMintLight
)

private val DarkAppColors = AppColors(
    isDark = true,
    canvasBackground = DarkCanvasBackground,
    cardSurface = DarkCanvasSurface,
    surfaceElevated = DarkSurfaceElevated,
    surfaceVariant = DarkSurfaceVariant,
    borderSubtle = DarkBorderSubtle,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textMuted = DarkTextMuted,
    dockBackground = FrostedGlassDark,
    primaryButton = Color(0xFF2E313D),
    onPrimaryButton = Color.White,
    bentoHoneySubtle = BentoHoney.copy(alpha = 0.18f),
    bentoSkySubtle = BentoSky.copy(alpha = 0.18f),
    bentoPinkSubtle = BentoPink.copy(alpha = 0.18f),
    bentoMintSubtle = BentoMint.copy(alpha = 0.18f)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

@Composable
fun obsidianButtonColors(): androidx.compose.material3.ButtonColors = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = if (AppTheme.colors.isDark) Color(0xFF2E313D) else DockObsidian,
    contentColor = Color.White,
    disabledContainerColor = if (AppTheme.colors.isDark) Color(0xFF252834) else Color(0xFFE4E6ED),
    disabledContentColor = if (AppTheme.colors.isDark) Color(0xFF686D80) else Color(0xFF888D9E)
)

@Composable
fun primaryButtonColors(): androidx.compose.material3.ButtonColors = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = AppTheme.colors.primaryButton,
    contentColor = AppTheme.colors.onPrimaryButton,
    disabledContainerColor = if (AppTheme.colors.isDark) Color(0xFF252834) else Color(0xFFE4E6ED),
    disabledContentColor = if (AppTheme.colors.isDark) Color(0xFF686D80) else Color(0xFF888D9E)
)

@Composable
fun accentButtonColors(accentColor: Color): androidx.compose.material3.ButtonColors = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = accentColor,
    contentColor = Color(0xFF14151B),
    disabledContainerColor = if (AppTheme.colors.isDark) Color(0xFF252834) else Color(0xFFE4E6ED),
    disabledContentColor = if (AppTheme.colors.isDark) Color(0xFF686D80) else Color(0xFF888D9E)
)

private val LightColorScheme = lightColorScheme(
    primary = DockObsidian,
    onPrimary = Color.White,
    primaryContainer = HeroLavender,
    onPrimaryContainer = TextPrimary,
    secondary = BentoHoney,
    onSecondary = TextPrimary,
    background = CanvasBackground,
    onBackground = TextPrimary,
    surface = CanvasSurface,
    onSurface = TextPrimary,
    surfaceVariant = Color.White,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle
)

// Matte Slate Grey Palette (Refined charcoal/slate, NOT pitch black)
private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = DockObsidian,
    primaryContainer = Color(0xFF2E313D),
    onPrimaryContainer = Color.White,
    secondary = BentoHoney,
    onSecondary = TextPrimary,
    background = DarkCanvasBackground,
    onBackground = DarkTextPrimary,
    surface = DarkCanvasSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderSubtle
)

@Composable
fun ONETheme(
    themeMode: String = "SYSTEM",
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val appColors = if (isDark) DarkAppColors else LightAppColors

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        androidx.compose.material3.LocalContentColor provides appColors.textPrimary
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
