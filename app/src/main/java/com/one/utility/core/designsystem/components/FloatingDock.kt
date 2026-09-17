package com.one.utility.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.one.utility.core.designsystem.AppTheme
import com.one.utility.core.designsystem.DockObsidian

enum class NavigationTab(val icon: ImageVector, val label: String, val route: String) {
    HOME(Icons.Outlined.Home, "Home", "home"),
    TOOLS(Icons.Outlined.GridView, "Tools", "tools_list"),
    CALCULATOR(Icons.Outlined.Calculate, "Calculators", "calculator"),
    SETTINGS(Icons.Outlined.Settings, "Settings", "settings")
}

@Composable
fun FloatingDock(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        FrostedGlassBox(
            shape = RoundedCornerShape(36.dp),
            elevation = 24.dp,
            borderWidth = 1.25.dp,
            modifier = Modifier.height(66.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NavigationTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    val isDark = AppTheme.colors.isDark

                    val iconColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) Color(0xFF14151B) else Color.White
                            else -> if (isDark) Color(0xFFA5A8BA) else Color(0xFF6C6F82)
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "iconColor"
                    )

                    val badgeBg by animateColorAsState(
                        targetValue = when {
                            isSelected -> if (isDark) Color.White else DockObsidian
                            else -> Color.Transparent
                        },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "badgeBg"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(badgeBg)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onTabSelected(tab) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
