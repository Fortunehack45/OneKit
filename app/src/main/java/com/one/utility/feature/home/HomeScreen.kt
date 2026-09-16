package com.one.utility.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.designsystem.components.BentoBadge
import com.one.utility.core.designsystem.components.BentoCard
import com.one.utility.core.designsystem.components.FloatingDock
import com.one.utility.core.designsystem.components.NavigationTab
import com.one.utility.core.router.ToolIntent
import com.one.utility.core.router.ToolRegistry
import com.one.utility.core.router.ToolRouter

@Composable
fun HomeScreen(
    onNavigateToTool: (String) -> Unit,
    onNavigateToChainedWorkflow: (List<String>) -> Unit,
    onNavigateToCalcWithExpression: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

    val toolRouter = remember { ToolRouter() }
    val intentResult = remember(searchQuery) { toolRouter.resolve(searchQuery) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { com.one.utility.core.data.PreferencesManager(context) }
    var recentTools by remember { mutableStateOf(prefs.getRecentTools()) }

    fun launchTool(id: String, title: String, route: String) {
        prefs.addRecentTool(id, title, route)
        recentTools = prefs.getRecentTools()
        onNavigateToTool(route)
    }

    val categories = listOf("All", "Images", "PDFs", "Calculators", "QR & Scan", "Tech")

    Scaffold(
        containerColor = CanvasBackground,
        bottomBar = {
            FloatingDock(
                selectedTab = currentTab,
                onTabSelected = { tab ->
                    currentTab = tab
                    when (tab) {
                        NavigationTab.TOOLS -> onNavigateToTool("tools_list")
                        NavigationTab.CALCULATOR -> onNavigateToTool("calculator")
                        NavigationTab.SETTINGS -> onNavigateToTool("settings")
                        NavigationTab.HOME -> { /* Already on Home */ }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Top Bar: App Identity & Quick Search Icon
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HeroLavender),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = DockObsidian
                            )
                        }
                        Column {
                            Text(
                                text = "ONE",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "All-In-One Offline Utility",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, BorderSubtle, CircleShape)
                            .clickable { },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 2. Hero Card: Universal Natural-Language Search & Action Hub
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(HeroLavender)
                        .padding(22.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ONE Hub",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    lineHeight = 30.sp
                                )
                                Text(
                                    text = "Whatever you need to do, do it in ONE.",
                                    fontSize = 13.sp,
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "100% OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DockObsidian
                                )
                            }
                        }

                        // Search Input Bar inside the Hero Card
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = {
                                        Text(
                                            "Describe a task (e.g. 17% of 850k)",
                                            color = TextMuted,
                                            fontSize = 13.sp
                                        )
                                    },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Real-time Action Match Preview
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DockObsidian,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (val intent = intentResult) {
                                            is ToolIntent.ImageToPdf -> onNavigateToTool("image_to_pdf")
                                            is ToolIntent.BackgroundRemover -> onNavigateToTool("background_remover")
                                            is ToolIntent.ImageCompressor -> onNavigateToTool("compressor")
                                            is ToolIntent.ImageResizer -> onNavigateToTool("resizer")
                                            is ToolIntent.QrGenerator -> onNavigateToTool("qr")
                                            is ToolIntent.PdfMerger -> onNavigateToTool("pdf_toolbox")
                                            is ToolIntent.PdfSplitter -> onNavigateToTool("pdf_toolbox")
                                            is ToolIntent.TextTools -> onNavigateToTool("text_tools")
                                            is ToolIntent.DeveloperTools -> onNavigateToTool("dev_tools")
                                            is ToolIntent.PasswordGenerator -> onNavigateToTool("password_generator")
                                            is ToolIntent.PercentageCalculation -> onNavigateToCalcWithExpression("${intent.percent}% of ${intent.total}")
                                            is ToolIntent.MathCalculation -> onNavigateToCalcWithExpression(intent.expression)
                                            is ToolIntent.UnitConversion -> onNavigateToTool("unit_converter")
                                            is ToolIntent.ChainedWorkflow -> onNavigateToChainedWorkflow(intent.stepNames)
                                            else -> onNavigateToTool("tools_list")
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = when (intentResult) {
                                                is ToolIntent.PercentageCalculation -> "Calculate ${intentResult.percent}% of ${intentResult.total}"
                                                is ToolIntent.MathCalculation -> "Evaluate Math: ${intentResult.expression}"
                                                is ToolIntent.UnitConversion -> "Convert ${intentResult.value} ${intentResult.fromUnit} to ${intentResult.toUnit}"
                                                is ToolIntent.ImageToPdf -> "Action: Convert Images to PDF"
                                                is ToolIntent.BackgroundRemover -> "Action: Remove Background"
                                                is ToolIntent.ImageCompressor -> "Action: Compress Images"
                                                is ToolIntent.PdfMerger -> "Action: Open PDF Toolbox"
                                                is ToolIntent.TextTools -> "Action: Text Analyzer & Tools"
                                                is ToolIntent.DeveloperTools -> "Action: Developer Tools"
                                                is ToolIntent.PasswordGenerator -> "Action: Password Generator"
                                                is ToolIntent.ChainedWorkflow -> "Chained Workflow: Compress & PDF"
                                                else -> "Search Tools for \"$searchQuery\""
                                            },
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap to open workflow instantly",
                                            color = Color.White.copy(alpha = 0.7f),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Pills (Active is Solid Obsidian, Inactive is White)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) PillActiveBackground else PillInactiveBackground)
                                .border(1.dp, if (isSelected) Color.Transparent else BorderSubtle, RoundedCornerShape(20.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) PillActiveText else PillInactiveText,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3.5. Recent Tools (when enabled and populated)
            if (recentTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Recent",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentTools) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.White,
                                    modifier = Modifier
                                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                                        .clickable { launchTool(tool.toolId, tool.title, tool.route) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(BentoHoney)
                                        )
                                        Text(
                                            text = tool.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section Title
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 5. Asymmetric Bento Grid (Matches reference image visual layout)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Left Column: Warm Honey Tall Card (Image -> PDF)
                    BentoCard(
                        backgroundColor = BentoHoney,
                        onClick = { onNavigateToTool("image_to_pdf") },
                        modifier = Modifier
                            .weight(1f)
                            .height(265.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            BentoBadge("FAST LOCAL")

                            Column {
                                Text(
                                    text = "Image → PDF",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "A4, Letter & Margins\n100% on-device",
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = TextPrimary.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Outlined.PictureAsPdf,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(DockObsidian),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Two Stacked Bento Cards (Sky Blue Top, Bubblegum Pink Bottom)
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sky Blue: Compress & Optimize
                        BentoCard(
                            backgroundColor = BentoSky,
                            onClick = { onNavigateToTool("compressor") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Compress",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        Icons.Outlined.Compress,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Shrink photos up to 85%",
                                    fontSize = 11.sp,
                                    color = TextPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Bubblegum Pink: QR & Barcode Generator
                        BentoCard(
                            backgroundColor = BentoPink,
                            onClick = { onNavigateToTool("qr") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(125.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "QR Code",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Icon(
                                        Icons.Outlined.QrCode,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = "Wi-Fi, URLs & vCards",
                                    fontSize = 11.sp,
                                    color = TextPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Secondary Bento Row: Background Remover & Calculator
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // White Surface Card: Cutout / Background Removal
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTool("background_remover") }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AutoFixHigh,
                                contentDescription = null,
                                tint = HeroLavenderDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Remove BG",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Local cutout engine",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // White Surface Card: Calculator & Units
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                            .clickable { onNavigateToTool("calculator") }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Calculate,
                                contentDescription = null,
                                tint = BentoHoney,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Calculators",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "% of, tips & units",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
