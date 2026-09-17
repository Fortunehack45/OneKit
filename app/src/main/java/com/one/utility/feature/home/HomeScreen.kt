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
import com.one.utility.core.router.ToolIntent
import com.one.utility.core.router.ToolRouter

@Composable
fun HomeScreen(
    onNavigateToTool: (String) -> Unit,
    onNavigateToChainedWorkflow: (List<String>) -> Unit,
    onNavigateToCalcWithExpression: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

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

    val categories = listOf("All", "Images", "PDFs", "Calculators", "QR & Scan", "Fonts & Text", "Tech")

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Top Bar: Clean ONE Brand Identity (No avatar profile circle!)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(BentoHoney)
                        )
                        Column {
                            Text(
                                text = "ONE",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = AppTheme.colors.textPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "All-In-One Offline Utility",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }

                    // 100% Offline Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(AppTheme.colors.surfaceVariant)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = HeroLavenderDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "100% OFFLINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // 2. Hero Card: Universal Natural-Language Search & Action Hub
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(HeroLavender)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ONE Hub",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = DockObsidian
                            )

                            BentoBadge("PRIVATE", backgroundColor = HeroBadgeBackground)
                        }

                        Text(
                            text = "Whatever you need to do, do it in ONE.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DockObsidian.copy(alpha = 0.85f)
                        )

                        // Smart Search Bar
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
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
                                            is ToolIntent.CoolFonts -> onNavigateToTool("cool_fonts")
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
                                                is ToolIntent.CoolFonts -> "Action: Cool Fonts & Bio Styler"
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

            // 3. Category Filter Pills
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.cardSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    selectedCategory = cat
                                    when (cat) {
                                        "Calculators" -> onNavigateToTool("calculator")
                                        "Fonts & Text" -> onNavigateToTool("cool_fonts")
                                        "QR & Scan" -> onNavigateToTool("qr")
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3.5. Recent Tools
            if (recentTools.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Recent",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentTools) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppTheme.colors.cardSurface,
                                    modifier = Modifier
                                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
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
                                            color = AppTheme.colors.textPrimary
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
                    color = AppTheme.colors.textPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 5. Asymmetric Bento Grid
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

                    // Right Column: Stacked Bento Cards (Compress & QR)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(265.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Sky Blue: Compress Image
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
                                        fontSize = 18.sp,
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Card: Cutout / Background Removal
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
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
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Local cutout engine",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }

                    // Card: Calculator & Units
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
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
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "% of, tips & units",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 7. NEW: Cool Fonts & Bio Styler Feature Card
            item {
                BentoCard(
                    backgroundColor = BentoMint,
                    onClick = { onNavigateToTool("cool_fonts") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Cool Fonts & Bio Styler",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0C3822)
                                )
                                BentoBadge("NEW", backgroundColor = Color(0xFFC7F2DE))
                            }
                            Text(
                                text = "25+ Aesthetic fonts: Gothic, Cursive, Small Caps & Kaomoji",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5938)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0C3822)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.TextFields,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
