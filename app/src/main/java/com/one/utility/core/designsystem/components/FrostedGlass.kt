package com.one.utility.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.one.utility.core.designsystem.AppTheme

/**
 * Applies clean, minimalist matte translucent styling:
 * Solid, glare-free, non-flashy surface with a crisp 1dp border.
 * Strictly free of distracting gradients and glare.
 */
fun Modifier.frostedGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    isDark: Boolean = false,
    borderWidth: Dp = 1.dp
): Modifier {
    val surfaceColor = if (isDark) Color(0xF21C1E26) else Color(0xF7FFFFFF)
    val borderColor = if (isDark) Color(0x2EFFFFFF) else Color(0x1F000000)

    return this
        .clip(shape)
        .background(color = surfaceColor, shape = shape)
        .border(width = borderWidth, color = borderColor, shape = shape)
}

/**
 * A reusable container that encapsulates subtle elevation and clean matte surface.
 */
@Composable
fun FrostedGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(36.dp),
    elevation: Dp = 10.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = AppTheme.colors.isDark

    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isDark) Color(0x60000000) else Color(0x18000000),
                ambientColor = if (isDark) Color(0x30000000) else Color(0x0C000000)
            )
            .frostedGlass(
                shape = shape,
                isDark = isDark,
                borderWidth = borderWidth
            ),
        content = content
    )
}
