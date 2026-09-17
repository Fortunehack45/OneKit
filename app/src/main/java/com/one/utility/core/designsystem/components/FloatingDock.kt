package com.one.utility.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.AppTheme
import com.one.utility.core.designsystem.pressFeedback

enum class NavigationTab(val icon: ImageVector, val label: String, val route: String) {
    HOME(Icons.Outlined.Home, "Home", "home"),
    TOOLS(Icons.Outlined.GridView, "Tools", "tools_list"),
    CALCULATOR(Icons.Outlined.Calculate, "Calculator", "calculator"),
    SETTINGS(Icons.Outlined.Settings, "Settings", "settings")
}

/**
 * Flagship floating capsule dock with precise ergonomics & minimalist refinement:
 * - Constrained max width of 340dp for symmetrical optical balance on all mobile screens
 * - 62dp streamlined capsule height with subtle frosted glass surface & dual-layer depth
 * - Active pill indicator with spring dampening and tactile click feedback
 * - Balanced typography and unified semantic Electric Indigo accent
 */
@Composable
fun FloatingDock(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.colors.isDark
    val dockSurface = if (isDark) Color(0xF2141622) else Color(0xF7FFFFFF)
    val dockBorder = if (isDark) Color(0x28FFFFFF) else Color(0x12000000)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 348.dp)
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = if (isDark) Color(0x66000000) else Color(0x18000000),
                    ambientColor = if (isDark) Color(0x33000000) else Color(0x0A000000)
                )
                .clip(CircleShape)
                .background(dockSurface)
                .border(0.75.dp, dockBorder, CircleShape)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = tab == selectedTab
                    val activeAccent = MaterialTheme.colorScheme.primary

                    val iconColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> activeAccent
                            else -> if (isDark) Color(0xFF8E92A4) else Color(0xFF6B7280)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockIconColor"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> activeAccent
                            else -> if (isDark) Color(0xFF8E92A4) else Color(0xFF6B7280)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockLabelColor"
                    )

                    val pillBg by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) activeAccent.copy(alpha = 0.16f) else activeAccent.copy(alpha = 0.10f)
                            else -> Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockPillBg"
                    )

                    val pillBorderColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> activeAccent.copy(alpha = if (isDark) 0.28f else 0.16f)
                            else -> Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockPillBorder"
                    )

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "dockIconScale"
                    )

                    val pillShape = RoundedCornerShape(24.dp)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 2.dp)
                            .clip(pillShape)
                            .background(pillBg)
                            .border(0.75.dp, pillBorderColor, pillShape)
                            .pressFeedback(pressedScale = 0.94f) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(21.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = tab.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                color = labelColor,
                                maxLines = 1,
                                letterSpacing = (-0.1).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
