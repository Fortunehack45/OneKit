package com.one.utility.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Segment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.data.PreferencesManager
import com.one.utility.core.designsystem.*
import com.one.utility.core.designsystem.components.BentoBadge
import com.one.utility.core.designsystem.components.BentoCard
import com.one.utility.core.designsystem.components.FrostedGlassBox
import com.one.utility.core.router.ToolCategory
import com.one.utility.core.router.ToolDefinition
import com.one.utility.core.router.ToolIntent
import com.one.utility.core.router.ToolRegistry
import com.one.utility.core.router.ToolRouter
import com.one.utility.core.router.ToolSection
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var recentTools by remember { mutableStateOf(prefs.getRecentTools()) }

    // Layout Mode & Density Customization
    var currentLayout by remember { mutableStateOf(prefs.homeLayout) }
    var currentDensity by remember { mutableStateOf(prefs.homeDensity) }
    var showLayoutSheet by remember { mutableStateOf(false) }

    fun launchTool(id: String, title: String, route: String) {
        prefs.addRecentTool(id, title, route)
        recentTools = prefs.getRecentTools()
        onNavigateToTool(route)
    }

    // 10 Clear Utility Categories matching user expectations
    val categories = listOf(
        "All",
        "Images",
        "PDFs",
        "Scanner",
        "Files",
        "Calculators",
        "Converters",
        "Money & Time",
        "Text",
        "Privacy & Tech"
    )

    // Active categorized sections based on selectedCategory and searchQuery
    val activeSections = remember(selectedCategory, searchQuery) {
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            val matches = ToolRegistry.tools.filter { tool ->
                tool.title.lowercase().contains(q) ||
                        tool.description.lowercase().contains(q) ||
                        tool.keywords.any { it.contains(q) } ||
                        tool.category.title.lowercase().contains(q)
            }
            if (matches.isNotEmpty()) {
                listOf(ToolSection("search", "Search Results", Icons.Outlined.Search, matches.map { it.id }))
            } else {
                emptyList()
            }
        } else when (selectedCategory) {
            "All" -> ToolRegistry.sections
            "Images" -> listOfNotNull(ToolRegistry.findSectionById("images"))
            "PDFs" -> listOfNotNull(ToolRegistry.findSectionById("pdfs"))
            "Scanner" -> listOfNotNull(ToolRegistry.findSectionById("scanner"))
            "Files" -> listOfNotNull(ToolRegistry.findSectionById("files"))
            "Calculators" -> listOfNotNull(ToolRegistry.findSectionById("calculators"))
            "Converters" -> listOfNotNull(ToolRegistry.findSectionById("converters"))
            "Money & Time" -> listOfNotNull(ToolRegistry.findSectionById("money_time"))
            "Text" -> listOfNotNull(ToolRegistry.findSectionById("text"))
            "Privacy & Tech" -> listOfNotNull(ToolRegistry.findSectionById("tech"))
            else -> ToolRegistry.sections
        }
    }

    // Flat list for fallback selection and match count
    val displayedTools = remember(activeSections) {
        activeSections.flatMap { it.getTools() }.distinctBy { it.id }
    }

    // Density-dependent dynamic measurements
    val densityPadding = when (currentDensity) {
        "COMPACT" -> 10.dp
        "SPACIOUS" -> 20.dp
        else -> 15.dp
    }
    val densityIconBoxSize = when (currentDensity) {
        "COMPACT" -> 38.dp
        "SPACIOUS" -> 52.dp
        else -> 46.dp
    }
    val densityIconSize = when (currentDensity) {
        "COMPACT" -> 18.dp
        "SPACIOUS" -> 26.dp
        else -> 22.dp
    }
    val densityTitleSize = when (currentDensity) {
        "COMPACT" -> 14.sp
        "SPACIOUS" -> 17.sp
        else -> 15.sp
    }
    val densityDescSize = when (currentDensity) {
        "COMPACT" -> 11.sp
        "SPACIOUS" -> 13.sp
        else -> 12.sp
    }
    val bentoHeight = when (currentDensity) {
        "COMPACT" -> 210.dp
        "SPACIOUS" -> 300.dp
        else -> 260.dp
    }

    fun getIconForTool(route: String): ImageVector {
        return when (route) {
            "image_to_pdf" -> Icons.Outlined.PictureAsPdf
            "background_remover" -> Icons.Outlined.AutoFixHigh
            "compressor" -> Icons.Outlined.Compress
            "resizer" -> Icons.Outlined.AspectRatio
            "image_cropper" -> Icons.Outlined.Crop
            "image_converter" -> Icons.Outlined.Transform
            "calculator" -> Icons.Outlined.Calculate
            "unit_converter" -> Icons.Outlined.SyncAlt
            "everyday_calculators" -> Icons.Outlined.AttachMoney
            "currency_time" -> Icons.Outlined.Schedule
            "qr" -> Icons.Outlined.QrCode
            "document_scanner" -> Icons.Outlined.DocumentScanner
            "pdf_toolbox" -> Icons.Outlined.FolderZip
            "text_tools" -> Icons.AutoMirrored.Outlined.Segment
            "cool_fonts" -> Icons.Outlined.TextFields
            "password_generator" -> Icons.Outlined.Lock
            "storage_cleaner" -> Icons.Outlined.CleaningServices
            "batch_rename" -> Icons.Outlined.DriveFileRenameOutline
            "dev_tools" -> Icons.Outlined.Code
            else -> Icons.Outlined.Build
        }
    }

    fun getBadgeColorForCategory(category: ToolCategory): Color {
        return when (category) {
            ToolCategory.IMAGES -> BentoSky
            ToolCategory.PDF -> BentoHoney
            ToolCategory.CALCULATOR -> BentoMint
            ToolCategory.SCAN_QR -> BentoPink
            ToolCategory.TEXT -> HeroLavender
            ToolCategory.TECH -> Color(0xFFE0D8FF)
            ToolCategory.FILES -> Color(0xFFFFD5D5)
        }
    }

    fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", num.toLong())
        } else {
            String.format(Locale.US, "%,.2f", num).trimEnd('0').trimEnd('.')
        }
    }

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
            // 1. Refined Brand Header with Layout Customizer & Offline Badge
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "ONE",
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = AppTheme.colors.textPrimary,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Everyday Utility OS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.textSecondary
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Layout Customization Button
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.clickable { showLayoutSheet = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    when (currentLayout) {
                                        "GRID" -> Icons.Outlined.GridView
                                        "COMPACT" -> Icons.Outlined.ViewStream
                                        "COMFORT" -> Icons.Outlined.ViewAgenda
                                        else -> Icons.Outlined.DashboardCustomize
                                    },
                                    contentDescription = "Customize Layout",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = when (currentLayout) {
                                        "GRID" -> "GRID"
                                        "COMPACT" -> "COMPACT"
                                        "COMFORT" -> "EXPANDED"
                                        else -> "BENTO"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // 100% Offline Privacy Badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textPrimary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // 2. Spotlight Minimalist Search Bar & Action Hub
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    border = BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(if (searchQuery.isNotEmpty()) 12.dp else 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Search Text Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            TextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search tools, math, or workflow...",
                                        color = AppTheme.colors.textMuted,
                                        fontSize = 13.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = AppTheme.colors.textPrimary,
                                    unfocusedTextColor = AppTheme.colors.textPrimary
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
                                        tint = AppTheme.colors.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Real-time Action / Workflow Match Card with Instant Answers
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (val intent = intentResult) {
                                            is ToolIntent.PercentageCalculation -> onNavigateToCalcWithExpression("${intent.percent}% of ${intent.total}")
                                            is ToolIntent.MathCalculation -> onNavigateToCalcWithExpression(intent.expression)
                                            is ToolIntent.UnitConversion -> onNavigateToTool("unit_converter")
                                            is ToolIntent.ChainedWorkflow -> onNavigateToChainedWorkflow(intent.stepNames)
                                            is ToolIntent.ImageToPdf -> onNavigateToTool("image_to_pdf")
                                            is ToolIntent.BackgroundRemover -> onNavigateToTool("background_remover")
                                            is ToolIntent.ImageCompressor -> onNavigateToTool("compressor")
                                            is ToolIntent.ImageCropper -> onNavigateToTool("image_cropper")
                                            is ToolIntent.ImageResizer -> onNavigateToTool("resizer")
                                            is ToolIntent.ImageConverter -> onNavigateToTool("image_converter")
                                            is ToolIntent.DocumentScanner -> onNavigateToTool("document_scanner")
                                            is ToolIntent.PdfMerger, is ToolIntent.PdfSplitter -> onNavigateToTool("pdf_toolbox")
                                            is ToolIntent.QrGenerator -> onNavigateToTool("qr")
                                            is ToolIntent.QrScanner -> onNavigateToTool("document_scanner")
                                            is ToolIntent.Calculator -> onNavigateToTool("calculator")
                                            is ToolIntent.UnitConverter -> onNavigateToTool("unit_converter")
                                            is ToolIntent.EverydayCalculators -> onNavigateToTool("everyday_calculators")
                                            is ToolIntent.CurrencyAndTime -> onNavigateToTool("currency_time")
                                            is ToolIntent.CoolFonts -> onNavigateToTool("cool_fonts")
                                            is ToolIntent.TextTools -> onNavigateToTool("text_tools")
                                            is ToolIntent.PasswordGenerator -> onNavigateToTool("password_generator")
                                            is ToolIntent.StorageCleaner -> onNavigateToTool("storage_cleaner")
                                            is ToolIntent.BatchRename -> onNavigateToTool("batch_rename")
                                            is ToolIntent.DeveloperTools -> onNavigateToTool("dev_tools")
                                            else -> {
                                                displayedTools.firstOrNull()?.let { onNavigateToTool(it.route) }
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (val intent = intentResult) {
                                                is ToolIntent.PercentageCalculation -> intent.formattedResult
                                                is ToolIntent.MathCalculation -> "= ${intent.formattedResult}"
                                                is ToolIntent.UnitConversion -> intent.formattedResult
                                                is ToolIntent.ChainedWorkflow -> intent.displayPipeline
                                                is ToolIntent.ImageToPdf -> "Launch: Image → PDF"
                                                is ToolIntent.BackgroundRemover -> "Launch: Remove Background"
                                                is ToolIntent.ImageCompressor -> "Launch: Image Compressor"
                                                is ToolIntent.ImageCropper -> "Launch: Crop & Rotate"
                                                is ToolIntent.ImageResizer -> "Launch: Resize Image"
                                                is ToolIntent.ImageConverter -> "Launch: Image Converter"
                                                is ToolIntent.Calculator -> "Launch: Keypad & Scientific Calculator"
                                                is ToolIntent.UnitConverter -> "Launch: Scientific Unit Converter"
                                                is ToolIntent.EverydayCalculators -> "Launch: Everyday Calculators"
                                                is ToolIntent.CurrencyAndTime -> "Launch: Currency & World Time"
                                                is ToolIntent.CoolFonts -> "Launch: Cool Fonts & Bio Styler"
                                                is ToolIntent.PasswordGenerator -> "Launch: Password Generator"
                                                is ToolIntent.DocumentScanner -> "Launch: Document Scanner"
                                                is ToolIntent.QrGenerator -> "Launch: QR Code Generator"
                                                is ToolIntent.QrScanner -> "Launch: Barcode & QR Scanner"
                                                is ToolIntent.PdfMerger -> "Launch: PDF Toolbox (Merge PDFs)"
                                                is ToolIntent.PdfSplitter -> "Launch: PDF Toolbox (Split / Images)"
                                                is ToolIntent.StorageCleaner -> "Launch: Storage & Cache Cleaner"
                                                is ToolIntent.BatchRename -> "Launch: Batch File Renamer"
                                                is ToolIntent.TextTools -> "Launch: Text Analyzer & Tools"
                                                is ToolIntent.DeveloperTools -> "Launch: Developer Tools"
                                                else -> displayedTools.firstOrNull()?.let { "Launch: ${it.title}" } ?: "Search: \"$searchQuery\""
                                            },
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (val intent = intentResult) {
                                                is ToolIntent.PercentageCalculation -> "${intent.percent}% of ${intent.currencyPrefix}${formatNumber(intent.total)} • Tap to open in Calculator"
                                                is ToolIntent.MathCalculation -> "${intent.expression} • Tap to open in Calculator"
                                                is ToolIntent.UnitConversion -> "${formatNumber(intent.value)} ${intent.fromUnit.uppercase()} = ${intent.formattedResult} • Tap to open Unit Converter"
                                                is ToolIntent.ChainedWorkflow -> "Smart Automated Pipeline • Tap to execute"
                                                else -> "Tap to open instantly"
                                            },
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips (10-category organized bar) with Quick Layout Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = cat,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = AppTheme.colors.surfaceCard,
                                    labelColor = AppTheme.colors.textSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle
                                )
                            )
                        }
                    }

                    // Quick 1-Tap Layout Cycle Button
                    Surface(
                        shape = CircleShape,
                        color = AppTheme.colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                val nextLayout = when (currentLayout) {
                                    "BENTO" -> "GRID"
                                    "GRID" -> "COMPACT"
                                    "COMPACT" -> "COMFORT"
                                    else -> "BENTO"
                                }
                                currentLayout = nextLayout
                                prefs.homeLayout = nextLayout
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                when (currentLayout) {
                                    "GRID" -> Icons.Outlined.GridView
                                    "COMPACT" -> Icons.Outlined.ViewStream
                                    "COMFORT" -> Icons.Outlined.ViewAgenda
                                    else -> Icons.Outlined.DashboardCustomize
                                },
                                contentDescription = "Quick Toggle Layout",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // 4. Recent Tools (if history exists and on All tab without active search)
            if (recentTools.isNotEmpty() && searchQuery.isBlank() && selectedCategory == "All") {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "RECENT",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.textTertiary,
                            letterSpacing = 1.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentTools) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AppTheme.colors.surfaceCard,
                                    modifier = Modifier
                                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                                        .clickable { launchTool(tool.toolId, tool.title, tool.route) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            getIconForTool(tool.route),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
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

            // 5. Featured Section (shown in BENTO mode on "All" when not searching)
            if (searchQuery.isBlank() && selectedCategory == "All" && currentLayout == "BENTO") {
                item {
                    Text(
                        text = "FEATURED ACTIONS",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Left Tall Bento Card: Image -> PDF
                        BentoCard(
                            backgroundColor = BentoHoney,
                            onClick = { launchTool("image_to_pdf", "Image → PDF", "image_to_pdf") },
                            modifier = Modifier
                                .weight(1f)
                                .height(bentoHeight)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                BentoBadge("FAST LOCAL")

                                Column {
                                    Text(
                                        text = "Image → PDF",
                                        fontSize = if (currentDensity == "COMPACT") 17.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "A4, Letter & Margins\n100% on-device",
                                        fontSize = if (currentDensity == "COMPACT") 11.sp else 12.sp,
                                        lineHeight = 16.sp,
                                        color = TextPrimary.copy(alpha = 0.85f),
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
                                            .size(36.dp)
                                            .background(DockObsidian, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Right Column: Stacked Bento Cards (Compress & QR)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(bentoHeight),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BentoCard(
                                backgroundColor = BentoSky,
                                onClick = { launchTool("image_compressor", "Compress Image", "compressor") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Compress", fontSize = if (currentDensity == "COMPACT") 15.sp else 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Icon(Icons.Outlined.Compress, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Text("Shrink photos up to 85%", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.85f))
                                }
                            }

                            BentoCard(
                                backgroundColor = BentoPink,
                                onClick = { launchTool("qr_generator", "QR Code Generator", "qr") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("QR Code", fontSize = if (currentDensity == "COMPACT") 15.sp else 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Icon(Icons.Outlined.QrCode, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Text("Wi-Fi, URLs & Barcodes", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                }
            }

            // 6. Render Categorized Sections According to Selected Layout Mode
            activeSections.forEach { section ->
                val sectionTools = section.getTools()
                if (sectionTools.isNotEmpty()) {
                    // Section Header
                    item(key = "header_${section.id}") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "${section.title.uppercase()} (${sectionTools.size})",
                                    style = AppTheme.typography.labelSmall,
                                    color = AppTheme.colors.textTertiary,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (section.id == activeSections.firstOrNull()?.id) {
                                Text(
                                    text = "100% On-Device",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Section Tools according to currentLayout
                    when (currentLayout) {
                        "GRID" -> {
                            sectionTools.chunked(2).forEachIndexed { chunkIndex, rowTools ->
                                item(key = "grid_${section.id}_$chunkIndex") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowTools.forEach { tool ->
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = AppTheme.colors.surfaceCard,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                                                    .clickable { launchTool(tool.id, tool.title, tool.route) }
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(densityPadding),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(densityIconBoxSize)
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(getBadgeColorForCategory(tool.category).copy(alpha = 0.35f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = getIconForTool(tool.route),
                                                                contentDescription = null,
                                                                tint = AppTheme.colors.textPrimary,
                                                                modifier = Modifier.size(densityIconSize)
                                                            )
                                                        }
                                                        tool.badge?.let { badge ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(6.dp))
                                                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = badge,
                                                                    fontSize = 9.sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = tool.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = densityTitleSize,
                                                        color = AppTheme.colors.textPrimary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = tool.description,
                                                        fontSize = densityDescSize,
                                                        color = AppTheme.colors.textSecondary,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        if (rowTools.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        "COMPACT" -> {
                            items(sectionTools, key = { "compact_${section.id}_${it.id}" }) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = AppTheme.colors.surfaceCard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                                        .clickable { launchTool(tool.id, tool.title, tool.route) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = densityPadding * 0.75f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(densityIconBoxSize * 0.85f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(getBadgeColorForCategory(tool.category).copy(alpha = 0.35f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getIconForTool(tool.route),
                                                    contentDescription = null,
                                                    tint = AppTheme.colors.textPrimary,
                                                    modifier = Modifier.size(densityIconSize * 0.85f)
                                                )
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = tool.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = densityTitleSize,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                    tool.badge?.let { badge ->
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = badge,
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = tool.description,
                                                    fontSize = densityDescSize,
                                                    color = AppTheme.colors.textSecondary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open",
                                            tint = AppTheme.colors.textTertiary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        "COMFORT" -> {
                            items(sectionTools, key = { "comfort_${section.id}_${it.id}" }) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(22.dp),
                                    color = AppTheme.colors.surfaceCard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(22.dp))
                                        .clickable { launchTool(tool.id, tool.title, tool.route) }
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(densityPadding * 1.15f),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(densityIconBoxSize)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(getBadgeColorForCategory(tool.category).copy(alpha = 0.35f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = getIconForTool(tool.route),
                                                        contentDescription = null,
                                                        tint = AppTheme.colors.textPrimary,
                                                        modifier = Modifier.size(densityIconSize)
                                                    )
                                                }
                                                Text(
                                                    text = tool.category.title.uppercase(),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AppTheme.colors.textTertiary,
                                                    letterSpacing = 0.8.sp
                                                )
                                            }
                                            tool.badge?.let { badge ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = badge,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }

                                        Text(
                                            text = tool.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (densityTitleSize.value + 2).sp,
                                            color = AppTheme.colors.textPrimary
                                        )

                                        Text(
                                            text = tool.description,
                                            fontSize = densityDescSize,
                                            color = AppTheme.colors.textSecondary,
                                            lineHeight = 18.sp
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Open Tool",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowForward,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {
                            // Standard BENTO tool list per section
                            items(sectionTools, key = { "bento_${section.id}_${it.id}" }) { tool ->
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = AppTheme.colors.surfaceCard,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                                        .clickable { launchTool(tool.id, tool.title, tool.route) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(densityPadding),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(densityIconBoxSize)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(getBadgeColorForCategory(tool.category).copy(alpha = 0.35f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = getIconForTool(tool.route),
                                                    contentDescription = null,
                                                    tint = AppTheme.colors.textPrimary,
                                                    modifier = Modifier.size(densityIconSize)
                                                )
                                            }

                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        text = tool.title,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = densityTitleSize,
                                                        color = AppTheme.colors.textPrimary
                                                    )
                                                    tool.badge?.let { badge ->
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = badge,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = tool.description,
                                                    fontSize = densityDescSize,
                                                    color = AppTheme.colors.textSecondary,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = "Open",
                                            tint = AppTheme.colors.textTertiary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (displayedTools.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "No tools matching \"${if (searchQuery.isNotBlank()) searchQuery else selectedCategory}\"",
                                color = AppTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Try adjusting your search or switching to \"All\"",
                                color = AppTheme.colors.textSecondary,
                                fontSize = 12.sp
                            )
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Show All Tools")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Customize Home Layout and Card Density
    if (showLayoutSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayoutSheet = false },
            containerColor = AppTheme.colors.surfaceCard,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customize Home Screen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Choose how tools and widgets are displayed",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                    IconButton(onClick = { showLayoutSheet = false }) {
                        Icon(Icons.Default.Clear, contentDescription = "Close", tint = AppTheme.colors.textSecondary)
                    }
                }

                // Section 1: Layout Mode
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "LAYOUT STYLE",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary,
                        letterSpacing = 1.sp
                    )

                    val layoutOptions = listOf(
                        Triple("BENTO", "Dynamic Bento", "Hero widgets + smart utility cards"),
                        Triple("GRID", "2-Column Grid", "Clean grid for fast visual scanning"),
                        Triple("COMPACT", "Compact List", "Minimalist dense list for speed"),
                        Triple("COMFORT", "Expanded Cards", "Detailed cards with full descriptions")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        layoutOptions.forEach { (mode, title, desc) ->
                            val isSelected = currentLayout == mode
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLayout = mode
                                        prefs.homeLayout = mode
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = desc,
                                            fontSize = 11.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Card Size & Density
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CARD SIZE & DENSITY",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary,
                        letterSpacing = 1.sp
                    )

                    val densityOptions = listOf(
                        Pair("COMPACT", "Compact"),
                        Pair("NORMAL", "Standard"),
                        Pair("SPACIOUS", "Spacious")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        densityOptions.forEach { (density, label) ->
                            val isSelected = currentDensity == density
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        currentDensity = density
                                        prefs.homeDensity = density
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
