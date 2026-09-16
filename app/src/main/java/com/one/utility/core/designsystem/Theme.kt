package com.one.utility.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    onSurfaceVariant = TextSecondary
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = DockObsidian,
    primaryContainer = Color(0xFF28243D),
    onPrimaryContainer = Color.White,
    secondary = BentoHoney,
    onSecondary = TextPrimary,
    background = Color(0xFF121217),
    onBackground = Color.White,
    surface = Color(0xFF1A1A22),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF23232E),
    onSurfaceVariant = Color(0xFFB0AFC0)
)

@Composable
fun ONETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
