package com.one.utility.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.one.utility.core.designsystem.DockIconInactive
import com.one.utility.core.designsystem.DockObsidian

enum class NavigationTab(val icon: ImageVector, val label: String) {
    HOME(Icons.Outlined.Home, "Home"),
    TOOLS(Icons.Outlined.GridView, "Tools"),
    CALCULATOR(Icons.Outlined.Calculate, "Calculators"),
    SETTINGS(Icons.Outlined.Settings, "Settings")
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .height(68.dp)
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(34.dp))
                .clip(RoundedCornerShape(34.dp))
                .background(DockObsidian)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            NavigationTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val iconColor = if (isSelected) DockObsidian else DockIconInactive

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onTabSelected(tab) },
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
