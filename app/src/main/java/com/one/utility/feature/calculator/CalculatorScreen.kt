package com.one.utility.feature.calculator

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ComprehensiveUnitsEngine
import com.one.utility.core.processing.UniversalCalculatorEngine

enum class CalculatorMode(val title: String) {
    NATURAL("Smart Math"),
    PERCENTAGE("Percentage"),
    SPLIT_BILL("Tip & Bill"),
    UNITS("Unit Converter")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    initialExpression: String? = null,
    initialMode: CalculatorMode = CalculatorMode.NATURAL,
    onNavigateToUnitConverter: (() -> Unit)? = null,
    onNavigateBack: () -> Unit
) {
    val engine = remember { UniversalCalculatorEngine() }
    val unitsEngine = remember { ComprehensiveUnitsEngine() }
    var selectedMode by remember { mutableStateOf(initialMode) }

    // Natural Math State
    var expressionInput by remember { mutableStateOf(initialExpression ?: "") }
    var mathResult by remember { mutableStateOf<String?>(null) }

    // Percentage State
    var percentInput by remember { mutableStateOf("17") }
    var totalInput by remember { mutableStateOf("850000") }
    var percentResult by remember { mutableStateOf<String?>(null) }

    // Split Bill State
    var billAmountInput by remember { mutableStateOf("120") }
    var tipPercentInput by remember { mutableStateOf("15") }
    var peopleCountInput by remember { mutableStateOf("3") }

    // Unit Converter State
    var selectedCategory by remember { mutableStateOf(unitsEngine.categories.first()) }
    var fromUnit by remember { mutableStateOf(selectedCategory.units[0]) }
    var toUnit by remember { mutableStateOf(selectedCategory.units.getOrElse(1) { selectedCategory.units[0] }) }
    var unitValueInput by remember { mutableStateOf("25") }
    var unitPickerTarget by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"
    var unitSearchQuery by remember { mutableStateOf("") }

    // Auto-calculate on initial load if expression is passed
    LaunchedEffect(initialExpression) {
        if (!initialExpression.isNullOrBlank()) {
            if (initialExpression.contains("% of")) {
                val parts = initialExpression.split("% of")
                if (parts.size == 2) {
                    val p = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val t = parts[1].trim().toDoubleOrNull() ?: 0.0
                    percentInput = p.toString()
                    totalInput = t.toString()
                    selectedMode = CalculatorMode.PERCENTAGE
                    val res = engine.calculatePercentage(p, t)
                    percentResult = "%,.2f".format(res)
                }
            } else {
                runCatching {
                    val res = engine.evaluateExpression(initialExpression)
                    mathResult = "%,.4f".format(res)
                }
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Universal Calculator", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Horizontally Scrollable Calculator Mode Selector Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(CalculatorMode.values()) { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.cardSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 2. Mode Content
            when (selectedMode) {
                CalculatorMode.NATURAL -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Enter Math Expression", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = expressionInput,
                                    onValueChange = { expressionInput = it },
                                    placeholder = { Text("e.g. 450000 * 0.17 or (800000 / 12)", color = AppTheme.colors.textMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                Button(
                                    onClick = {
                                        mathResult = try {
                                            val res = engine.evaluateExpression(expressionInput)
                                            "%,.4f".format(res)
                                        } catch (e: Exception) {
                                            "Error: ${e.message}"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Calculate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Calculate",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                mathResult?.let { res ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .padding(18.dp)
                                    ) {
                                        Column {
                                            Text("Result", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                            Text(res, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AppTheme.colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                CalculatorMode.PERCENTAGE -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("What is X% of Y?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = percentInput,
                                        onValueChange = { percentInput = it },
                                        label = { Text("Percentage (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                    OutlinedTextField(
                                        value = totalInput,
                                        onValueChange = { totalInput = it },
                                        label = { Text("Total Amount") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                }

                                Button(
                                    onClick = {
                                        val p = percentInput.toDoubleOrNull() ?: 0.0
                                        val t = totalInput.toDoubleOrNull() ?: 0.0
                                        val res = engine.calculatePercentage(p, t)
                                        percentResult = "%,.2f".format(res)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        "Calculate Percentage",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }

                                percentResult?.let { res ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .padding(18.dp)
                                    ) {
                                        Column {
                                            Text("${percentInput}% of ${totalInput} is:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                            Text(res, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AppTheme.colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                CalculatorMode.SPLIT_BILL -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Tip & Bill Splitter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = billAmountInput,
                                    onValueChange = { billAmountInput = it },
                                    label = { Text("Bill Amount ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = tipPercentInput,
                                        onValueChange = { tipPercentInput = it },
                                        label = { Text("Tip %") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                    OutlinedTextField(
                                        value = peopleCountInput,
                                        onValueChange = { peopleCountInput = it },
                                        label = { Text("People") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                }

                                val b = billAmountInput.toDoubleOrNull() ?: 0.0
                                val t = tipPercentInput.toDoubleOrNull() ?: 0.0
                                val p = peopleCountInput.toIntOrNull() ?: 1
                                val splitRes = engine.calculateSplitBill(b, t, p)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(18.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Total Tip: \$${"%.2f".format(splitRes.totalTip)}", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        Text("Grand Total: \$${"%.2f".format(splitRes.grandTotal)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                                        HorizontalDivider(color = AppTheme.colors.borderSubtle, modifier = Modifier.padding(vertical = 4.dp))
                                        Text("Per Person Pays:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                        Text("\$${"%.2f".format(splitRes.perPersonAmount)}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                CalculatorMode.UNITS -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Scientific Unit Converter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                        Text("200+ Units (Physics, Astronomy, CS)", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    }
                                    if (onNavigateToUnitConverter != null) {
                                        IconButton(onClick = onNavigateToUnitConverter) {
                                            Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = "Full Screen", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                // Category Selection Pills
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(unitsEngine.categories) { cat ->
                                        val isSel = cat.id == selectedCategory.id
                                        FilterChip(
                                            selected = isSel,
                                            onClick = {
                                                selectedCategory = cat
                                                fromUnit = cat.units[0]
                                                toUnit = cat.units.getOrElse(1) { cat.units[0] }
                                            },
                                            label = { Text(cat.name, fontSize = 12.sp) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }

                                // Numeric Value Input
                                OutlinedTextField(
                                    value = unitValueInput,
                                    onValueChange = { unitValueInput = it },
                                    label = { Text("Input Value") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                // From & To Dropdown Selectors
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // FROM Selector Button
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                unitSearchQuery = ""
                                                unitPickerTarget = "FROM"
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("FROM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textTertiary)
                                                Text(fromUnit.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppTheme.colors.textSecondary)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            val temp = fromUnit
                                            fromUnit = toUnit
                                            toUnit = temp
                                        },
                                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                                    ) {
                                        Icon(Icons.Default.SwapVert, contentDescription = "Swap", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                                    }

                                    // TO Selector Button
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                unitSearchQuery = ""
                                                unitPickerTarget = "TO"
                                            },
                                        shape = RoundedCornerShape(14.dp),
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("TO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textTertiary)
                                                Text(toUnit.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = AppTheme.colors.textSecondary)
                                        }
                                    }
                                }

                                val v = unitValueInput.toDoubleOrNull() ?: 0.0
                                val converted = unitsEngine.convert(v, fromUnit.id, toUnit.id, selectedCategory.id)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                        .padding(18.dp)
                                ) {
                                    Column {
                                        Text("$v ${fromUnit.symbol} equals:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                        Text(
                                            "${"%,.6f".format(converted).trimEnd('0').trimEnd('.')} ${toUnit.symbol}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
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

    // Modal Unit Selector Dialog
    if (unitPickerTarget != null) {
        val filtered = remember(unitSearchQuery, selectedCategory) {
            if (unitSearchQuery.isBlank()) selectedCategory.units
            else selectedCategory.units.filter { it.name.contains(unitSearchQuery, ignoreCase = true) || it.symbol.contains(unitSearchQuery, ignoreCase = true) }
        }

        AlertDialog(
            onDismissRequest = { unitPickerTarget = null },
            title = { Text("Select ${if (unitPickerTarget == "FROM") "Source" else "Target"} Unit", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitSearchQuery,
                        onValueChange = { unitSearchQuery = it },
                        placeholder = { Text("Search unit...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (unitSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { unitSearchQuery = "" }) { Icon(Icons.Default.Close, contentDescription = null) }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(filtered) { u ->
                            val isSel = if (unitPickerTarget == "FROM") fromUnit.id == u.id else toUnit.id == u.id
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (unitPickerTarget == "FROM") fromUnit = u else toUnit = u
                                        unitPickerTarget = null
                                    },
                                color = if (isSel) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(u.name, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, color = AppTheme.colors.textPrimary)
                                        Text("Symbol: ${u.symbol}", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                    }
                                    if (isSel) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { unitPickerTarget = null }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}
