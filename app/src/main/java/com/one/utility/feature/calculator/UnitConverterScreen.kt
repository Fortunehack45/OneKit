package com.one.utility.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val engine = remember { ComprehensiveUnitsEngine() }

    var selectedCategory by remember { mutableStateOf(engine.categories.first()) }
    var fromUnit by remember { mutableStateOf(selectedCategory.units[0]) }
    var toUnit by remember { mutableStateOf(selectedCategory.units.getOrElse(1) { selectedCategory.units[0] }) }
    var inputValue by remember { mutableStateOf("1") }

    // Dialog picker state
    var pickerTarget by remember { mutableStateOf<String?>(null) } // "FROM" or "TO" or null
    var pickerSearchQuery by remember { mutableStateOf("") }

    // When category changes, reset units
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
                            text = "Scientific Unit Converter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "${engine.categories.sumOf { it.units.size }}+ Units (Astronomy, Physics, CS)",
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
            // Category Selector Chips
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
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
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
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Converter Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // FROM SECTION
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    text = "${selectedCategory.units.size} options",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textTertiary
                                )
                            }

                            // Unit Dropdown Button
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        pickerSearchQuery = ""
                                        pickerTarget = "FROM"
                                    },
                                color = AppTheme.colors.canvasBackground,
                                shape = RoundedCornerShape(14.dp),
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
                                label = { Text("Input Value") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
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
                                listOf("1", "10", "100", "1000").forEach { preset ->
                                    SuggestionChip(
                                        onClick = { inputValue = preset },
                                        label = { Text(preset, fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = AppTheme.colors.canvasBackground,
                                            labelColor = AppTheme.colors.textSecondary
                                        ),
                                        border = SuggestionChipDefaults.suggestionChipBorder(
                                            enabled = true,
                                            borderColor = AppTheme.colors.borderSubtle
                                        ),
                                        modifier = Modifier.height(28.dp)
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
                                },
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(38.dp)
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap Units",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            HorizontalDivider(modifier = Modifier.weight(1f), color = AppTheme.colors.borderSubtle)
                        }

                        // TO SECTION
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        pickerSearchQuery = ""
                                        pickerTarget = "TO"
                                    },
                                color = AppTheme.colors.canvasBackground,
                                shape = RoundedCornerShape(14.dp),
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
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "RESULT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.textTertiary,
                                        letterSpacing = 1.sp
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val formatted = formatNumber(convertedResult)
                                        Text(
                                            text = "$formatted ${toUnit.symbol}",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        IconButton(
                                            onClick = {
                                                copyToClipboard(
                                                    "$formatted ${toUnit.symbol}",
                                                    "Converted Result"
                                                )
                                            }
                                        ) {
                                            Icon(
                                                Icons.Outlined.ContentCopy,
                                                contentDescription = "Copy Result",
                                                tint = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "1 ${fromUnit.symbol} = ${formatNumber(engine.convert(1.0, fromUnit.id, toUnit.id, selectedCategory.id))} ${toUnit.symbol}",
                                        fontSize = 11.sp,
                                        color = AppTheme.colors.textTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Multi-Unit Equivalent Breakdown Card
            item {
                Text(
                    text = "ALL EQUIVALENTS (${selectedCategory.name.uppercase()})",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.textTertiary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { toUnit = unitDef },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCurrentTarget) AppTheme.colors.surfaceCard.copy(alpha = 0.9f) else AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isCurrentTarget) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
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
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    copyToClipboard("${formatNumber(eqVal)} ${unitDef.symbol}")
                                },
                                modifier = Modifier.size(32.dp)
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

    // Modal Unit Selector Dialog
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
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                            focusedTextColor = AppTheme.colors.textPrimary,
                            unfocusedTextColor = AppTheme.colors.textPrimary
                        )
                    )

                    Text(
                        text = "${filteredUnits.size} matching units",
                        fontSize = 11.sp,
                        color = AppTheme.colors.textTertiary
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredUnits) { unitDef ->
                            val isSelected = if (pickerTarget == "FROM") fromUnit.id == unitDef.id else toUnit.id == unitDef.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (pickerTarget == "FROM") {
                                            fromUnit = unitDef
                                        } else {
                                            toUnit = unitDef
                                        }
                                        pickerTarget = null
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = unitDef.name,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = AppTheme.colors.textPrimary
                                        )
                                        Text(
                                            text = "Symbol: ${unitDef.symbol}",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.textSecondary
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickerTarget = null }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}
