package com.one.utility.feature.tools

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val collapsedSections = remember { mutableStateMapOf<String, Boolean>() }

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
                actions = {
                    IconButton(onClick = {
                        val allCollapsed = ToolRegistry.sections.isNotEmpty() && ToolRegistry.sections.all { collapsedSections[it.id] == true }
                        ToolRegistry.sections.forEach { sec ->
                            collapsedSections[sec.id] = !allCollapsed
                        }
                    }) {
                        val allCollapsed = ToolRegistry.sections.isNotEmpty() && ToolRegistry.sections.all { collapsedSections[it.id] == true }
                        Icon(
                            imageVector = if (allCollapsed) Icons.Default.UnfoldMore else Icons.Default.UnfoldLess,
                            contentDescription = if (allCollapsed) "Expand All" else "Collapse All",
                            tint = AppTheme.colors.textPrimary
                        )
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
            contentPadding = PaddingValues(bottom = 104.dp)
        ) {
            ToolRegistry.sections.forEach { section ->
                val sectionTools = section.getTools()
                if (sectionTools.isNotEmpty()) {
                    val isCollapsed = collapsedSections[section.id] == true
                    item(key = "header_${section.id}") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AppTheme.shapes.Chip)
                                .pressFeedback {
                                    collapsedSections[section.id] = !isCollapsed
                                }
                                .padding(top = 16.dp, bottom = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(AppTheme.shapes.Badge)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${section.title} (${sectionTools.size})",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary
                                )
                            }
                            Icon(
                                imageVector = if (isCollapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                                contentDescription = if (isCollapsed) "Expand ${section.title}" else "Collapse ${section.title}",
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    item(key = "section_content_${section.id}") {
                        AnimatedVisibility(
                            visible = !isCollapsed,
                            enter = AccordionTransitions.expand,
                            exit = AccordionTransitions.collapse
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                sectionTools.forEach { tool ->
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.cardSurface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, AppTheme.colors.borderSubtle, AppTheme.shapes.Card)
                                            .pressFeedback {
                                                val targetRoute = if (tool.route.isNotBlank()) tool.route else "tool/${tool.id}"
                                                onNavigateToTool(targetRoute)
                                            }
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
                                                                .clip(AppTheme.shapes.Badge)
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
        }
    }
}
