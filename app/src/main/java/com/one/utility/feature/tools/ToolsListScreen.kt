package com.one.utility.feature.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.router.ToolCategory
import com.one.utility.core.router.ToolRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsListScreen(
    onNavigateToTool: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("All Tools", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.canvasBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            ToolRegistry.sections.forEach { section ->
                val sectionTools = section.getTools()
                if (sectionTools.isNotEmpty()) {
                    item {
                        Text(
                            text = "${section.emoji} ${section.title} (${sectionTools.size})",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary,
                            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                        )
                    }

                    items(sectionTools) { tool ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                                .clickable { onNavigateToTool(tool.route) }
                        ) {
                            Row(
                                modifier = Modifier.padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(tool.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                        tool.badge?.let { badgeText ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(HeroLavender)
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DockObsidian)
                                            }
                                        }
                                    }
                                    Text(tool.description, fontSize = 12.sp, color = AppTheme.colors.textSecondary, modifier = Modifier.padding(top = 4.dp))
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AppTheme.colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
