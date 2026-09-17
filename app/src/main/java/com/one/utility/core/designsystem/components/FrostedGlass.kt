package com.one.utility.core.designsystem.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.one.utility.core.designsystem.AppTheme

/**
 * Applies authentic frosted glass styling with:
 * 1. Multi-stop light-scattering diffusion gradient (frosted surface)
 * 2. Specular Fresnel reflection sheen (top-down ambient bounce)
 * 3. Angled refraction specular border simulating light through bevelled glass edge
 * 
 * Note: Child composables remain crisp and razor-sharp on top of the frosted surface.
 */
fun Modifier.frostedGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = false,
    borderWidth: Dp = 1.dp
): Modifier {
    val borderBrush = Brush.linearGradient(
        colors = if (isDark) listOf(
            Color(0x60FFFFFF), // Crisp key-light highlight
            Color(0x20FFFFFF), // Diffuse mid tone
            Color(0x08FFFFFF)  // Glancing angle shadow
        ) else listOf(
            Color(0xEEFFFFFF), // Pure bright specular edge
            Color(0x55FFFFFF),
            Color(0x18FFFFFF)
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    return this
        .clip(shape)
        .drawBehind {
            // 1. Base frosted diffusion surface
            val baseGradient = if (isDark) {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD9222530), // Translucent slate
                        Color(0xC4171822)  // Translucent obsidian
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xEEFFFFFF), // High-opacity frosted milk glass
                        Color(0xD4F2F1F8)
                    )
                )
            }
            drawRect(brush = baseGradient)

            // 2. Specular Fresnel reflection sheen on the top 45%
            val specularGradient = Brush.verticalGradient(
                0.0f to (if (isDark) Color(0x30FFFFFF) else Color(0x75FFFFFF)),
                0.45f to (if (isDark) Color(0x06FFFFFF) else Color(0x10FFFFFF)),
                1.0f to Color.Transparent
            )
            drawRect(brush = specularGradient)
        }
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

/**
 * A reusable container that encapsulates deep shadow elevation and true frosted glass.
 */
@Composable
fun FrostedGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(36.dp),
    elevation: Dp = 20.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = AppTheme.colors.isDark

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isDark) Color(0x90000000) else Color(0x30000000),
                ambientColor = if (isDark) Color(0x50000000) else Color(0x15000000)
            )
            .frostedGlass(
                shape = shape,
                isDark = isDark,
                borderWidth = borderWidth
            ),
        content = content
    )
}
