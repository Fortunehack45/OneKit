package com.one.utility.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ComprehensiveUnitsEngine
import com.one.utility.core.processing.UnitCategory
import com.one.utility.core.processing.UnitDefinition
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    initialCategoryId: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val engine = remember { ComprehensiveUnitsEngine() }

    // Resolve initial category if requested
    val initialCategory = remember(initialCategoryId) {
        if (!initialCategoryId.isNullOrBlank()) {
            engine.categories.find { cat ->
                cat.id.equals(initialCategoryId, ignoreCase = true) ||
                        cat.id.contains(initialCategoryId, ignoreCase = true) ||
                        cat.name.contains(initialCategoryId, ignoreCase = true)
            } ?: engine.categories.first()
        } else {
            engine.categories.first()
        }
    }

    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var fromUnit by remember(selectedCategory) { mutableStateOf(selectedCategory.units[0]) }
    var toUnit by remember(selectedCategory) { mutableStateOf(selectedCategory.units.getOrElse(1) { selectedCategory.units[0] }) }
    var inputValue by remember { mutableStateOf("1") }
    var isSwapped by remember { mutableStateOf(false) }

    // Dialog picker state
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "FROM" or "TO" or null
    var pickerSearchQuery by remember { mutableStateOf("") }

    // When category changes, update selection and reset units
    fun selectCategory(category: UnitCategory) {
        selectedCategory = category
        fromUnit = category.units[0]
        toUnit = category.units.getOrElse(1) { category.units[0] }
    }

    // Calculation
    val numericInput = inputValue.toDoubleOrNull() ?: 0.0
    val convertedResult = remember(numericInput, fromUnit, toUnit, selectedCategory) {
        engine.convert(
            value = numericInput,
            fromUnitId = fromUnit.id,
            toUnitId = toUnit.id,
            categoryId = selectedCategory.id
        )
    }

    val swapRotation by animateFloatAsState(
        targetValue = if (isSwapped) 180f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "swapRotation"
    )

    fun formatNumber(value: Double): String {
        return if (value == 0.0) {
            "0"
        } else if (kotlin.math.abs(value) >= 1e9 || (kotlin.math.abs(value) < 1e-4 && kotlin.math.abs(value) > 0.0)) {
            val df = DecimalFormat("0.######E0")
            df.format(value)
        } else {
            val df = DecimalFormat("#,##0.######")
            df.format(value)
        }
    }

    fun copyToClipboard(text: String, label: String = "Conversion Result") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $text to clipboard", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Unit Converter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "200+ Units • Length, Weight, Temp, Volume & More",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Category Selector Chips (Unified horizontal scrollbar)
            item {
                Text(
                    text = "CATEGORY",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.textTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(engine.categories) { cat ->
                        val isSelected = cat.id == selectedCategory.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectCategory(cat) },
                            label = {
                                Text(
                                    cat.name,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
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
                            ),
                            shape = AppTheme.shapes.Chip
                        )
                    }
                }
            }

            // All-in-One Converter Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, AppTheme.shapes.Hero),
                    shape = AppTheme.shapes.Hero,
                    color = AppTheme.colors.surfaceCard
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // FROM SECTION
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FROM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textTertiary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "${selectedCategory.units.size} units in ${selectedCategory.name}",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textTertiary
                                )
                            }

                            // Unit Dropdown Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.shapes.SubCard)
                                    .pressFeedback {
                                        pickerSearchQuery = ""
                                        pickerTarget = "FROM"
                                    },
                                color = AppTheme.colors.canvasBackground,
                                shape = AppTheme.shapes.SubCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = fromUnit.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = AppTheme.colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Symbol: ${fromUnit.symbol}",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit",
                                        tint = AppTheme.colors.textSecondary
                                    )
                                }
                            }

                            // Input Value Field
                            OutlinedTextField(
                                value = inputValue,
                                onValueChange = { inputValue = it },
                                label = { Text("Input Value (${fromUnit.symbol})") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = AppTheme.shapes.SubCard,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                    focusedContainerColor = AppTheme.colors.canvasBackground,
                                    unfocusedContainerColor = AppTheme.colors.canvasBackground,
                                    focusedTextColor = AppTheme.colors.textPrimary,
                                    unfocusedTextColor = AppTheme.colors.textPrimary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = AppTheme.colors.textSecondary
                                ),
                                trailingIcon = {
                                    if (inputValue.isNotEmpty()) {
                                        IconButton(onClick = { inputValue = "" }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            )

                            // Quick Preset chips
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                listOf("1", "5", "10", "50", "100", "1000").forEach { preset ->
                                    SuggestionChip(
                                        onClick = { inputValue = preset },
                                        label = { Text(preset, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                        shape = AppTheme.shapes.Badge,
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = AppTheme.colors.canvasBackground,
                                            labelColor = AppTheme.colors.textSecondary
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = AppTheme.colors.borderSubtle
                                        ),
                                        modifier = Modifier.height(28.dp).pressFeedback()
                                    )
                                }
                            }
                        }

                        // SWAP BUTTON
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.borderSubtle)
                            IconButton(
                                onClick = {
                                    val temp = fromUnit
                                    fromUnit = toUnit
                                    toUnit = temp
                                    isSwapped = !isSwapped
                                },
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(42.dp)
                                    .pressFeedback()
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap Units",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp).graphicsLayer(rotationZ = swapRotation)
                                )
                            }
                            HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.borderSubtle)
                        }

                        // TO SECTION
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "TO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textTertiary,
                                letterSpacing = 1.sp
                            )

                            // Unit Dropdown Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.shapes.SubCard)
                                    .pressFeedback {
                                        pickerSearchQuery = ""
                                        pickerTarget = "TO"
                                    },
                                color = AppTheme.colors.canvasBackground,
                                shape = AppTheme.shapes.SubCard,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = toUnit.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = AppTheme.colors.textPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Symbol: ${toUnit.symbol}",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = "Select Unit",
                                        tint = AppTheme.colors.textSecondary
                                    )
                                }
                            }

                            // Result Display Box
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = AppTheme.shapes.SubCard,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CONVERTED RESULT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                        IconButton(
                                            onClick = {
                                                copyToClipboard(
                                                    "${formatNumber(convertedResult)} ${toUnit.symbol}",
                                                    "Converted Result"
                                                )
                                            },
                                            modifier = Modifier.size(28.dp).pressFeedback()
                                        ) {
                                            Icon(
                                                Icons.Outlined.ContentCopy,
                                                contentDescription = "Copy Result",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    val formatted = formatNumber(convertedResult)
                                    Text(
                                        text = "$formatted ${toUnit.symbol}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = AppTheme.colors.textPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "1 ${fromUnit.symbol} = ${formatNumber(engine.convert(1.0, fromUnit.id, toUnit.id, selectedCategory.id))} ${toUnit.symbol}",
                                        fontSize = 11.5.sp,
                                        color = AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Multi-Unit Equivalent Breakdown Card
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ALL EQUIVALENTS (${selectedCategory.name.uppercase()})",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.textTertiary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Tap any to set as target",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textTertiary
                    )
                }
            }

            items(selectedCategory.units) { unitDef ->
                val eqVal = remember(numericInput, fromUnit, unitDef, selectedCategory) {
                    engine.convert(
                        value = numericInput,
                        fromUnitId = fromUnit.id,
                        toUnitId = unitDef.id,
                        categoryId = selectedCategory.id
                    )
                }
                val isCurrentTarget = unitDef.id == toUnit.id
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AppTheme.shapes.SubCard)
                        .pressFeedback { toUnit = unitDef },
                    shape = AppTheme.shapes.SubCard,
                    color = if (isCurrentTarget) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrentTarget) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = unitDef.name,
                                fontSize = 14.sp,
                                fontWeight = if (isCurrentTarget) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrentTarget) MaterialTheme.colorScheme.primary else AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Symbol: ${unitDef.symbol}",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textTertiary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatNumber(eqVal),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    copyToClipboard("${formatNumber(eqVal)} ${unitDef.symbol}")
                                },
                                modifier = Modifier.size(32.dp).pressFeedback()
                            ) {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy",
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

    // Modal Unit Selector Dialog with Instant Search
    if (pickerTarget != null) {
        val filteredUnits = remember(pickerSearchQuery, selectedCategory) {
            if (pickerSearchQuery.isBlank()) {
                selectedCategory.units
            } else {
                selectedCategory.units.filter {
                    it.name.contains(pickerSearchQuery, ignoreCase = true) ||
                            it.symbol.contains(pickerSearchQuery, ignoreCase = true) ||
                            it.id.contains(pickerSearchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = { pickerTarget = null },
            containerColor = AppTheme.colors.surfaceCard,
            shape = AppTheme.shapes.Hero,
            title = {
                Text(
                    text = "Select ${if (pickerTarget == "FROM") "Source" else "Target"} Unit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AppTheme.colors.textPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pickerSearchQuery,
                        onValueChange = { pickerSearchQuery = it },
                        placeholder = { Text("Search unit (e.g. Parsec, Joule, Byte)") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = AppTheme.colors.textSecondary)
                        },
                        trailingIcon = {
                            if (pickerSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { pickerSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = AppTheme.colors.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppTheme.shapes.Chip,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary
                        )
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredUnits) { unitDef ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.shapes.SubCard)
                                    .pressFeedback {
                                        if (pickerTarget == "FROM") {
                                            fromUnit = unitDef
                                        } else {
                                            toUnit = unitDef
                                        }
                                        pickerTarget = null
                                    },
                                shape = AppTheme.shapes.SubCard,
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = unitDef.name,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "Symbol: ${unitDef.symbol}",
                                            fontSize = 11.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = unitDef.symbol,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerTarget = null }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
