package com.one.utility.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.one.utility.core.processing.EverydayCalculatorsEngine

enum class EverydayCalcTab(val label: String) {
    MARGIN("Profit & Margin"),
    TAX("Sales Tax"),
    INTEREST("Interest"),
    BMI("BMI Health"),
    FUEL("Fuel Trip")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EverydayCalculatorsScreen(
    onNavigateBack: () -> Unit
) {
    val engine = remember { EverydayCalculatorsEngine() }
    var selectedTab by remember { mutableStateOf(EverydayCalcTab.MARGIN) }

    // State for Profit & Margin
    var costPriceInput by remember { mutableStateOf("120") }
    var sellingPriceInput by remember { mutableStateOf("180") }

    // State for Sales Tax
    var taxAmountInput by remember { mutableStateOf("250") }
    var taxRateInput by remember { mutableStateOf("8.5") }

    // State for Interest
    var principalInput by remember { mutableStateOf("10000") }
    var interestRateInput by remember { mutableStateOf("5.5") }
    var yearsInput by remember { mutableStateOf("3") }

    // State for BMI
    var weightInput by remember { mutableStateOf("70") }
    var heightInput by remember { mutableStateOf("175") }

    // State for Fuel
    var distanceInput by remember { mutableStateOf("450") }
    var fuelRateInput by remember { mutableStateOf("7.2") }
    var pricePerLiterInput by remember { mutableStateOf("1.65") }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Everyday Calculators", fontWeight = FontWeight.Bold, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CanvasBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tab Selector Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EverydayCalcTab.values().size) { idx ->
                        val tab = EverydayCalcTab.values()[idx]
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = { Text(tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DockObsidian,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            when (selectedTab) {
                EverydayCalcTab.MARGIN -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Profit, Margin & Markup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = costPriceInput,
                                        onValueChange = { costPriceInput = it },
                                        label = { Text("Cost Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = sellingPriceInput,
                                        onValueChange = { sellingPriceInput = it },
                                        label = { Text("Selling Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val c = costPriceInput.toDoubleOrNull() ?: 0.0
                                val s = sellingPriceInput.toDoubleOrNull() ?: 0.0
                                val res = engine.calculateProfitAndMargin(c, s)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoHoneyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Net Profit: \$${"%.2f".format(res.profit)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DockObsidian)
                                        Text("Profit Margin: ${"%.1f".format(res.profitMarginPercent)}%", fontSize = 14.sp, color = TextPrimary)
                                        Text("Markup: ${"%.1f".format(res.markupPercent)}%", fontSize = 14.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.TAX -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Sales Tax / VAT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = taxAmountInput,
                                        onValueChange = { taxAmountInput = it },
                                        label = { Text("Base Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = taxRateInput,
                                        onValueChange = { taxRateInput = it },
                                        label = { Text("Tax Rate (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val a = taxAmountInput.toDoubleOrNull() ?: 0.0
                                val r = taxRateInput.toDoubleOrNull() ?: 0.0
                                val (tax, total) = engine.calculateTax(a, r)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoSkyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Total with Tax: \$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DockObsidian)
                                        Text("Tax Amount: \$${"%.2f".format(tax)}", fontSize = 14.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.INTEREST -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Compound Interest", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                OutlinedTextField(
                                    value = principalInput,
                                    onValueChange = { principalInput = it },
                                    label = { Text("Principal Investment ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = interestRateInput,
                                        onValueChange = { interestRateInput = it },
                                        label = { Text("Annual Rate (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = yearsInput,
                                        onValueChange = { yearsInput = it },
                                        label = { Text("Time (Years)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val p = principalInput.toDoubleOrNull() ?: 0.0
                                val rate = interestRateInput.toDoubleOrNull() ?: 0.0
                                val y = yearsInput.toDoubleOrNull() ?: 0.0
                                val comp = engine.calculateCompoundInterest(p, rate, y)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoPinkLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Future Value: \$${"%,.2f".format(comp.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DockObsidian)
                                        Text("Total Interest Earned: \$${"%,.2f".format(comp.totalInterestEarned)}", fontSize = 14.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.BMI -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Body Mass Index (BMI)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Weight (kg)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text("Height (cm)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val w = weightInput.toDoubleOrNull() ?: 0.0
                                val h = heightInput.toDoubleOrNull() ?: 0.0
                                val bmi = engine.calculateBmi(w, h)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoSkyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("BMI: ${"%.1f".format(bmi.bmi)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = DockObsidian)
                                        Text("Category: ${bmi.category}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.FUEL -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Trip Fuel Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                OutlinedTextField(
                                    value = distanceInput,
                                    onValueChange = { distanceInput = it },
                                    label = { Text("Trip Distance (km)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = fuelRateInput,
                                        onValueChange = { fuelRateInput = it },
                                        label = { Text("Liters / 100km") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    OutlinedTextField(
                                        value = pricePerLiterInput,
                                        onValueChange = { pricePerLiterInput = it },
                                        label = { Text("Price per Liter ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                }

                                val d = distanceInput.toDoubleOrNull() ?: 0.0
                                val r = fuelRateInput.toDoubleOrNull() ?: 0.0
                                val p = pricePerLiterInput.toDoubleOrNull() ?: 0.0
                                val fuel = engine.calculateFuelCost(d, r, p)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(BentoHoneyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Estimated Cost: \$${"%.2f".format(fuel.totalCost)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = DockObsidian)
                                        Text("Fuel Required: ${"%.1f".format(fuel.fuelNeeded)} Liters", fontSize = 14.sp, color = TextSecondary)
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
