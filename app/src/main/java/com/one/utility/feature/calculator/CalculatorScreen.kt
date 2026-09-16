package com.one.utility.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    onNavigateBack: () -> Unit
) {
    val engine = remember { UniversalCalculatorEngine() }
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
    var unitValueInput by remember { mutableStateOf("25") }
    var fromUnit by remember { mutableStateOf("miles") }
    var toUnit by remember { mutableStateOf("km") }

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
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Horizontally Scrollable Mode Selector Chips (Eliminates awkward wrapping)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(CalculatorMode.values()) { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.cardSurface)
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedMode = mode }
                                .padding(horizontal = 18.dp, vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary,
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
                                        focusedBorderColor = BentoHoney,
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
                                        containerColor = AppTheme.colors.primaryButton,
                                        contentColor = AppTheme.colors.onPrimaryButton
                                    )
                                ) {
                                    Text("Calculate", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                mathResult?.let { res ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BentoHoneyLight)
                                            .padding(18.dp)
                                    ) {
                                        Column {
                                            Text("Result", fontSize = 12.sp, color = Color(0xFF6B4E1B))
                                            Text(res, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF2E1C00))
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
                                            focusedBorderColor = BentoHoney,
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
                                            focusedBorderColor = BentoHoney,
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
                                        containerColor = AppTheme.colors.primaryButton,
                                        contentColor = AppTheme.colors.onPrimaryButton
                                    )
                                ) {
                                    Text("Calculate Percentage", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                percentResult?.let { res ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(BentoHoneyLight)
                                            .padding(18.dp)
                                    ) {
                                        Column {
                                            Text("$percentInput% of $totalInput is:", fontSize = 12.sp, color = Color(0xFF6B4E1B))
                                            Text(res, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF2E1C00))
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
                                Text("Tip & Split Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = billAmountInput,
                                    onValueChange = { billAmountInput = it },
                                    label = { Text("Bill Amount ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoSky,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = tipPercentInput,
                                        onValueChange = { tipPercentInput = it },
                                        label = { Text("Tip (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BentoSky,
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
                                            focusedBorderColor = BentoSky,
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
                                        .background(BentoSkyLight)
                                        .padding(18.dp)
                                    ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Total Tip: \$${"%.2f".format(splitRes.totalTip)}", fontSize = 13.sp, color = Color(0xFF1A4968))
                                        Text("Grand Total: \$${"%.2f".format(splitRes.grandTotal)}", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0C2B40))
                                        HorizontalDivider(color = Color(0x33000000), modifier = Modifier.padding(vertical = 4.dp))
                                        Text("Per Person Pays:", fontSize = 12.sp, color = Color(0xFF1A4968))
                                        Text("\$${"%.2f".format(splitRes.perPersonAmount)}", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0C2B40))
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
                                Text("Universal Unit Converter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = unitValueInput,
                                    onValueChange = { unitValueInput = it },
                                    label = { Text("Value") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BentoPink,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                        focusedTextColor = AppTheme.colors.textPrimary,
                                        unfocusedTextColor = AppTheme.colors.textPrimary
                                    )
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = fromUnit,
                                        onValueChange = { fromUnit = it },
                                        label = { Text("From (miles, kg, c, gb)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BentoPink,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                    OutlinedTextField(
                                        value = toUnit,
                                        onValueChange = { toUnit = it },
                                        label = { Text("To (km, lbs, f, mb)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BentoPink,
                                            unfocusedBorderColor = AppTheme.colors.borderSubtle,
                                            focusedTextColor = AppTheme.colors.textPrimary,
                                            unfocusedTextColor = AppTheme.colors.textPrimary
                                        )
                                    )
                                }

                                val v = unitValueInput.toDoubleOrNull() ?: 0.0
                                val converted = engine.convertUnits(v, fromUnit, toUnit)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoPinkLight)
                                        .padding(18.dp)
                                ) {
                                    Column {
                                        Text("$v $fromUnit equals:", fontSize = 12.sp, color = Color(0xFF6B1B4A))
                                        Text(
                                            "${"%,.4f".format(converted)} $toUnit",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 24.sp,
                                            color = Color(0xFF3B0024)
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
}
