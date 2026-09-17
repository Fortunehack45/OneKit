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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.automirrored.outlined.Segment
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
import com.one.utility.core.router.ToolCategory
import com.one.utility.core.router.ToolDefinition
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

    val toolRouter = remember { ToolRouter() }
    val intentResult = remember(searchQuery) { toolRouter.resolve(searchQuery) }

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }
    var recentTools by remember { mutableStateOf(prefs.getRecentTools()) }

    fun launchTool(id: String, title: String, route: String) {
        prefs.addRecentTool(id, title, route)
        recentTools = prefs.getRecentTools()
        onNavigateToTool(route)
    }

    val categories = listOf("All", "Images", "PDFs", "Calculators", "QR & Scan", "Fonts & Text", "Tech", "Files")

    // Filter tools based on selectedCategory and searchQuery
    val displayedTools = remember(selectedCategory, searchQuery) {
        val baseTools = if (selectedCategory == "All") {
            ToolRegistry.tools
        } else {
            when (selectedCategory) {
                "Images" -> ToolRegistry.findByCategory(ToolCategory.IMAGES)
                "PDFs" -> ToolRegistry.findByCategory(ToolCategory.PDF)
                "Calculators" -> ToolRegistry.findByCategory(ToolCategory.CALCULATOR)
                "QR & Scan" -> ToolRegistry.findByCategory(ToolCategory.SCAN_QR)
                "Fonts & Text" -> ToolRegistry.findByCategory(ToolCategory.TEXT)
                "Tech" -> ToolRegistry.findByCategory(ToolCategory.TECH)
                "Files" -> ToolRegistry.findByCategory(ToolCategory.FILES)
                else -> ToolRegistry.tools
            }
        }

        if (searchQuery.isBlank()) {
            baseTools
        } else {
            val q = searchQuery.trim().lowercase()
            ToolRegistry.tools.filter { tool ->
                tool.title.lowercase().contains(q) ||
                        tool.description.lowercase().contains(q) ||
                        tool.keywords.any { it.contains(q) } ||
                        tool.category.title.lowercase().contains(q)
            }
        }
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
            // 1. Refined Brand Header (Clean typography, symmetrical offline badge, no cut-off dots)
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
                            text = "All-In-One Offline Suite",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.textSecondary
                        )
                    }

                    // 100% Offline Privacy Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "100% OFFLINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // 2. High-End Frosted Search Bar & Action Hub
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search Text Field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(AppTheme.colors.canvasBackground)
                                .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp),
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
                                        text = "Search tools, math, or workflow (e.g. compress -> pdf)...",
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

                        // Real-time Action / Workflow Match Card
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
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
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (val intent = intentResult) {
                                                is ToolIntent.PercentageCalculation -> "Calculate: ${intent.percent}% of ${intent.total}"
                                                is ToolIntent.MathCalculation -> "Evaluate Math: ${intent.expression}"
                                                is ToolIntent.UnitConversion -> "Convert: ${intent.value} ${intent.fromUnit} to ${intent.toUnit}"
                                                is ToolIntent.ChainedWorkflow -> "Run Workflow: ${intent.stepNames.joinToString(" ➔ ") { step -> step.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() } }}"
                                                is ToolIntent.ImageToPdf -> "Launch: Image to PDF"
                                                is ToolIntent.BackgroundRemover -> "Launch: AI Background Remover"
                                                is ToolIntent.ImageCompressor -> "Launch: Image Compressor"
                                                is ToolIntent.ImageCropper -> "Launch: Crop & Rotate"
                                                is ToolIntent.Calculator -> "Launch: Keypad Calculator"
                                                is ToolIntent.UnitConverter -> "Launch: 209+ Unit Converter"
                                                is ToolIntent.CoolFonts -> "Launch: Cool Fonts & Text Styler"
                                                is ToolIntent.PasswordGenerator -> "Launch: Password Generator"
                                                is ToolIntent.DocumentScanner -> "Launch: Document Scanner"
                                                is ToolIntent.QrGenerator -> "Launch: QR Code Generator"
                                                else -> "Direct Action for \"$searchQuery\""
                                            },
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Tap to execute instantly ⚡",
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                            fontSize = 11.sp
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

            // 3. Category Filter Chips (In-place filtering across all tools)
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            }

            // 4. Recent Tools (if user has recent history and not actively searching)
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

            // 5. Featured Quick-Action Bento Grid (shown when on "All" and no search query)
            if (searchQuery.isBlank() && selectedCategory == "All") {
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
                                .height(260.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                BentoBadge("FAST LOCAL")

                                Column {
                                    Text(
                                        text = "Image → PDF",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "A4, Letter & Margins\n100% on-device",
                                        fontSize = 12.sp,
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
                                .height(260.dp),
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
                                        Text("Compress", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                                        Text("QR Code", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Icon(Icons.Outlined.QrCode, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(20.dp))
                                    }
                                    Text("Wi-Fi, URLs & Barcodes", fontSize = 11.sp, color = TextPrimary.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                }
            }

            // 6. Complete Tool Catalog Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "SEARCH RESULTS (${displayedTools.size})"
                        else if (selectedCategory == "All") "ALL TOOLS (${displayedTools.size})"
                        else "${selectedCategory.uppercase()} (${displayedTools.size})",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "100% On-Device",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 7. Render Every Tool in the Catalog with Polished Positioning & Sizing
            items(displayedTools) { tool ->
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Category-colored Icon Box
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(getBadgeColorForCategory(tool.category).copy(alpha = 0.35f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getIconForTool(tool.route),
                                    contentDescription = null,
                                    tint = AppTheme.colors.textPrimary,
                                    modifier = Modifier.size(22.dp)
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
                                        fontSize = 15.sp,
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
                                    fontSize = 12.sp,
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

            if (displayedTools.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tools matching \"$searchQuery\"",
                            color = AppTheme.colors.textSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
