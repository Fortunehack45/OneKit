package com.one.utility.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.AppTheme
import com.one.utility.core.designsystem.DockObsidian
import com.one.utility.core.designsystem.pressFeedback

enum class NavigationTab(val icon: ImageVector, val label: String, val route: String) {
    HOME(Icons.Outlined.Home, "Home", "home"),
    TOOLS(Icons.Outlined.GridView, "Tools", "tools_list"),
    CALCULATOR(Icons.Outlined.Calculate, "Calculators", "calculator"),
    SETTINGS(Icons.Outlined.Settings, "Settings", "settings")
}

/**
 * Flagship floating capsule dock with precise ergonomics:
 * - Constrained max width of 380dp for perfect symmetry across all screen form factors
 * - 64dp standard capsule height with dual ambient/spot elevation shadows
 * - Equal weight distribution across tabs with spring-animated icon scaling and active indicator bars
 * - Tactile press feedback with haptic response
 */
@Composable
fun FloatingDock(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassBox(
            shape = CircleShape,
            elevation = 12.dp,
            borderWidth = 1.dp,
            modifier = Modifier
                .widthIn(max = 380.dp)
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    val isDark = AppTheme.colors.isDark

                    val iconColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) Color.White else DockObsidian
                            else -> if (isDark) Color(0xFF888B9E) else Color(0xFF717588)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockIconColor"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) Color.White else DockObsidian
                            else -> if (isDark) Color(0xFF888B9E) else Color(0xFF717588)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockLabelColor"
                    )

                    val pillBg by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) Color.White.copy(alpha = 0.12f) else DockObsidian.copy(alpha = 0.08f)
                            else -> Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "dockPillBg"
                    )

                    val indicatorWidth by animateDpAsState(
                        targetValue = if (isSelected) 12.dp else 0.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "indicatorWidth"
                    )

                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.12f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "dockIconScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(pillBg)
                            .pressFeedback { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = iconColor,
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = labelColor,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .height(2.5.dp)
                                    .width(indicatorWidth)
                                    .clip(CircleShape)
                                    .background(if (isDark) MaterialTheme.colorScheme.primary else DockObsidian)
                            )
                        }
                    }
                }
            }
        }
    }
}
