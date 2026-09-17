package com.one.utility.feature.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.EverydayCalculatorsEngine
import com.one.utility.core.processing.parseFlexibleDouble
import com.one.utility.core.processing.parseFlexibleInt
import java.time.LocalDate

enum class EverydayCalcTab(val label: String) {
    MARGIN("Profit & Margin"),
    DISCOUNT("Discount"),
    TIP_SPLIT("Tip & Split"),
    TAX("Sales Tax & VAT"),
    INTEREST("Interest"),
    LOAN("Loan & EMI"),
    SAVINGS("Savings Goal"),
    BMI("BMI Health"),
    FUEL("Fuel Trip"),
    DATE_DIFF("Date & Age")
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

    // State for Discount
    var discountPriceInput by remember { mutableStateOf("100") }
    var discountPercentInput by remember { mutableStateOf("20") }

    // State for Tip & Split
    var billAmountInput by remember { mutableStateOf("85") }
    var tipPercentInput by remember { mutableStateOf("15") }
    var numberOfPeopleInput by remember { mutableStateOf("2") }

    // State for Sales Tax
    var taxAmountInput by remember { mutableStateOf("250") }
    var taxRateInput by remember { mutableStateOf("8.5") }

    // State for Interest
    var principalInput by remember { mutableStateOf("10000") }
    var interestRateInput by remember { mutableStateOf("5.5") }
    var yearsInput by remember { mutableStateOf("3") }

    // State for Loan & EMI
    var loanPrincipalInput by remember { mutableStateOf("250000") }
    var loanRateInput by remember { mutableStateOf("6.5") }
    var loanTermYearsInput by remember { mutableStateOf("15") }

    // State for Savings Goal
    var savingsGoalTargetInput by remember { mutableStateOf("50000") }
    var savingsRateTargetInput by remember { mutableStateOf("7.0") }
    var savingsYearsTargetInput by remember { mutableStateOf("5") }

    // State for BMI
    var weightInput by remember { mutableStateOf("70") }
    var heightInput by remember { mutableStateOf("175") }

    // State for Fuel
    var distanceInput by remember { mutableStateOf("450") }
    var fuelRateInput by remember { mutableStateOf("7.2") }
    var pricePerLiterInput by remember { mutableStateOf("1.65") }

    // State for Date & Age
    var startYearInput by remember { mutableStateOf("2000") }
    var startMonthInput by remember { mutableStateOf("1") }
    var startDayInput by remember { mutableStateOf("1") }
    var endYearInput by remember { mutableStateOf(LocalDate.now().year.toString()) }
    var endMonthInput by remember { mutableStateOf(LocalDate.now().monthValue.toString()) }
    var endDayInput by remember { mutableStateOf(LocalDate.now().dayOfMonth.toString()) }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Everyday Calculators", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // Tab Selector Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(EverydayCalcTab.entries) { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            label = {
                                Text(
                                    tab.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = AppTheme.shapes.Chip,
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

            when (selectedTab) {
                EverydayCalcTab.MARGIN -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Profit, Margin & Markup", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = costPriceInput,
                                        onValueChange = { costPriceInput = it },
                                        label = { Text("Cost Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = sellingPriceInput,
                                        onValueChange = { sellingPriceInput = it },
                                        label = { Text("Selling Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val c = costPriceInput.parseFlexibleDouble() ?: 0.0
                                val s = sellingPriceInput.parseFlexibleDouble() ?: 0.0
                                val res = engine.calculateProfitAndMargin(c, s)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF332B1A) else BentoHoneyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Net Profit: \$${"%.2f".format(res.profit)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Profit Margin: ${"%.1f".format(res.profitMarginPercent)}%", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                        Text("Markup: ${"%.1f".format(res.markupPercent)}%", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.DISCOUNT -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Discount Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = discountPriceInput,
                                        onValueChange = { discountPriceInput = it },
                                        label = { Text("Original Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = discountPercentInput,
                                        onValueChange = { discountPercentInput = it },
                                        label = { Text("Discount (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("10", "15", "20", "25", "50").forEach { pct ->
                                        SuggestionChip(
                                            onClick = { discountPercentInput = pct },
                                            label = { Text("$pct%") }
                                        )
                                    }
                                }

                                val orig = discountPriceInput.parseFlexibleDouble() ?: 0.0
                                val disc = discountPercentInput.parseFlexibleDouble() ?: 0.0
                                val res = engine.calculateDiscount(orig, disc)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (AppTheme.colors.isDark) Color(0xFF332B1A) else BentoHoneyLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Final Price: $${"%.2f".format(res.finalPrice)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("You Save: $${"%.2f".format(res.savedAmount)} ($disc% off)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.TIP_SPLIT -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Tip & Split Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = billAmountInput,
                                    onValueChange = { billAmountInput = it },
                                    label = { Text("Total Bill ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = tipPercentInput,
                                        onValueChange = { tipPercentInput = it },
                                        label = { Text("Tip (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = numberOfPeopleInput,
                                        onValueChange = { numberOfPeopleInput = it },
                                        label = { Text("People (Count)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("10", "15", "18", "20", "25").forEach { tip ->
                                        SuggestionChip(
                                            onClick = { tipPercentInput = tip },
                                            label = { Text("$tip% tip") }
                                        )
                                    }
                                }

                                val bill = billAmountInput.parseFlexibleDouble() ?: 0.0
                                val tipPct = tipPercentInput.parseFlexibleDouble() ?: 0.0
                                val people = (numberOfPeopleInput.parseFlexibleInt() ?: 1).coerceAtLeast(1)
                                val splitRes = engine.calculateTipAndSplit(bill, tipPct, people)

                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (AppTheme.colors.isDark) Color(0xFF1B332A) else BentoMintLight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Each Person Pays: $${"%.2f".format(splitRes.perPersonAmount)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Total Tip: $${"%.2f".format(splitRes.tipAmount)} • Grand Total: $${"%.2f".format(splitRes.grandTotal)}", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
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
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Sales Tax / VAT", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = taxAmountInput,
                                        onValueChange = { taxAmountInput = it },
                                        label = { Text("Base Price ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = taxRateInput,
                                        onValueChange = { taxRateInput = it },
                                        label = { Text("Tax Rate (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val a = taxAmountInput.parseFlexibleDouble() ?: 0.0
                                val r = taxRateInput.parseFlexibleDouble() ?: 0.0
                                val (tax, total) = engine.calculateTax(a, r)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF1E293B) else BentoSkyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Total with Tax: \$${"%.2f".format(total)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Tax Amount: \$${"%.2f".format(tax)}", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
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
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Compound Interest", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = principalInput,
                                    onValueChange = { principalInput = it },
                                    label = { Text("Principal Investment ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = interestRateInput,
                                        onValueChange = { interestRateInput = it },
                                        label = { Text("Annual Rate (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = yearsInput,
                                        onValueChange = { yearsInput = it },
                                        label = { Text("Time (Years)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val p = principalInput.parseFlexibleDouble() ?: 0.0
                                val rate = interestRateInput.parseFlexibleDouble() ?: 0.0
                                val y = yearsInput.parseFlexibleDouble() ?: 0.0
                                val comp = engine.calculateCompoundInterest(p, rate, y)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Future Value: \$${"%,.2f".format(comp.totalAmount)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Total Interest Earned: \$${"%,.2f".format(comp.totalInterestEarned)}", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.LOAN -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Loan & EMI Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = loanPrincipalInput,
                                    onValueChange = { loanPrincipalInput = it },
                                    label = { Text("Loan Principal ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = loanRateInput,
                                        onValueChange = { loanRateInput = it },
                                        label = { Text("Annual Rate (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = loanTermYearsInput,
                                        onValueChange = { loanTermYearsInput = it },
                                        label = { Text("Term (Years)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                val p = loanPrincipalInput.parseFlexibleDouble() ?: 0.0
                                val r = loanRateInput.parseFlexibleDouble() ?: 0.0
                                val y = loanTermYearsInput.parseFlexibleInt() ?: 1
                                val loan = engine.calculateLoan(p, r, y)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Monthly EMI: \$${"%,.2f".format(loan.monthlyPayment)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Total Payment: \$${"%,.2f".format(loan.totalPayment)}", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
                                        Text("Total Interest: \$${"%,.2f".format(loan.totalInterest)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.SAVINGS -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Savings Goal Target", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = savingsGoalTargetInput,
                                    onValueChange = { savingsGoalTargetInput = it },
                                    label = { Text("Savings Target Goal ($)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = savingsRateTargetInput,
                                        onValueChange = { savingsRateTargetInput = it },
                                        label = { Text("Annual Return (%)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = savingsYearsTargetInput,
                                        onValueChange = { savingsYearsTargetInput = it },
                                        label = { Text("Years to Goal") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val target = savingsGoalTargetInput.parseFlexibleDouble() ?: 0.0
                                val rate = savingsRateTargetInput.parseFlexibleDouble() ?: 0.0
                                val yrs = savingsYearsTargetInput.parseFlexibleDouble() ?: 1.0
                                val savings = engine.calculateSavingsGoal(target, rate, yrs)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("Monthly Deposit: \$${"%,.2f".format(savings.monthlyDepositNeeded)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Total Deposited: \$${"%,.2f".format(savings.totalDeposited)}", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
                                        Text("Interest Earned: \$${"%,.2f".format(savings.totalInterestEarned)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
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
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Body Mass Index (BMI)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { weightInput = it },
                                        label = { Text("Weight (kg)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = heightInput,
                                        onValueChange = { heightInput = it },
                                        label = { Text("Height (cm)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val w = weightInput.parseFlexibleDouble() ?: 0.0
                                val h = heightInput.parseFlexibleDouble() ?: 0.0
                                val bmi = engine.calculateBmi(w, h)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF1E293B) else BentoSkyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("BMI: ${"%.1f".format(bmi.bmi)}", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = AppTheme.colors.textPrimary)
                                        Text("Category: ${bmi.category}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
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
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Trip Fuel Cost", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                OutlinedTextField(
                                    value = distanceInput,
                                    onValueChange = { distanceInput = it },
                                    label = { Text("Trip Distance (km)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = fuelRateInput,
                                        onValueChange = { fuelRateInput = it },
                                        label = { Text("Liters / 100km") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                    OutlinedTextField(
                                        value = pricePerLiterInput,
                                        onValueChange = { pricePerLiterInput = it },
                                        label = { Text("Price per Liter ($)") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                                    )
                                }

                                val d = distanceInput.parseFlexibleDouble() ?: 0.0
                                val r = fuelRateInput.parseFlexibleDouble() ?: 0.0
                                val p = pricePerLiterInput.parseFlexibleDouble() ?: 0.0
                                val fuel = engine.calculateFuelCost(d, r, p)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF332B1A) else BentoHoneyLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("Estimated Cost: \$${"%.2f".format(fuel.totalCost)}", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = AppTheme.colors.textPrimary)
                                        Text("Fuel Required: ${"%.1f".format(fuel.fuelNeeded)} Liters", fontSize = 14.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                EverydayCalcTab.DATE_DIFF -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Date Difference & Age Calculator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                Text("Start Date / Birth Date (YYYY - MM - DD)", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = startYearInput,
                                        onValueChange = { startYearInput = it },
                                        label = { Text("Year") },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = startMonthInput,
                                        onValueChange = { startMonthInput = it },
                                        label = { Text("Month") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = startDayInput,
                                        onValueChange = { startDayInput = it },
                                        label = { Text("Day") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                Text("End Date / Today (YYYY - MM - DD)", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = endYearInput,
                                        onValueChange = { endYearInput = it },
                                        label = { Text("Year") },
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = endMonthInput,
                                        onValueChange = { endMonthInput = it },
                                        label = { Text("Month") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    OutlinedTextField(
                                        value = endDayInput,
                                        onValueChange = { endDayInput = it },
                                        label = { Text("Day") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                }

                                val startDate = runCatching {
                                    LocalDate.of(
                                        startYearInput.parseFlexibleInt() ?: 2000,
                                        (startMonthInput.parseFlexibleInt() ?: 1).coerceIn(1, 12),
                                        (startDayInput.parseFlexibleInt() ?: 1).coerceIn(1, 28)
                                    )
                                }.getOrDefault(LocalDate.of(2000, 1, 1))

                                val endDate = runCatching {
                                    LocalDate.of(
                                        endYearInput.parseFlexibleInt() ?: LocalDate.now().year,
                                        (endMonthInput.parseFlexibleInt() ?: 1).coerceIn(1, 12),
                                        (endDayInput.parseFlexibleInt() ?: 1).coerceIn(1, 28)
                                    )
                                }.getOrDefault(LocalDate.now())

                                val diff = engine.calculateDateDifference(startDate, endDate)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (AppTheme.colors.isDark) Color(0xFF1B332A) else BentoMintLight)
                                        .padding(16.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("${diff.years} Years, ${diff.months} Months, ${diff.days} Days", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                                        Text("Total Days: ${diff.totalDays} days", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
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
