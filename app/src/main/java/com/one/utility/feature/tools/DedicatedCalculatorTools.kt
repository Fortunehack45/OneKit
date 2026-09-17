package com.one.utility.feature.tools

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.AppTheme
import com.one.utility.core.designsystem.BentoEmerald
import com.one.utility.core.processing.*
import java.util.Locale

@Composable
fun DedicatedLoanCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var principalInput by remember { mutableStateOf("250000") }
    var rateInput by remember { mutableStateOf("6.5") }
    var termYearsInput by remember { mutableStateOf("5") }

    val principal = principalInput.parseFlexibleDouble() ?: 0.0
    val rate = rateInput.parseFlexibleDouble() ?: 0.0
    val termMonths = (termYearsInput.parseFlexibleInt() ?: 1) * 12
    val result = engine.calculateLoan(principal, rate, termMonths)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = principalInput,
            onValueChange = { principalInput = it },
            label = { Text("Loan Principal ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rateInput,
                onValueChange = { rateInput = it },
                label = { Text("Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = termYearsInput,
                onValueChange = { termYearsInput = it },
                label = { Text("Term (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Monthly Payment:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val fmt = String.format(Locale.US, "$%,.2f", result.monthlyPayment)
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Repayment:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalPayment), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Interest Paid:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalInterest), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedMortgageCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var homeValueInput by remember { mutableStateOf("450000") }
    var downPaymentInput by remember { mutableStateOf("90000") }
    var rateInput by remember { mutableStateOf("5.8") }
    var termYearsInput by remember { mutableStateOf("30") }

    val homeVal = homeValueInput.parseFlexibleDouble() ?: 0.0
    val downPay = downPaymentInput.parseFlexibleDouble() ?: 0.0
    val rate = rateInput.parseFlexibleDouble() ?: 0.0
    val termYears = termYearsInput.parseFlexibleInt() ?: 30
    val result = engine.calculateMortgage(homeVal, downPay, rate, termYears)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = homeValueInput,
                onValueChange = { homeValueInput = it },
                label = { Text("Home Value ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = downPaymentInput,
                onValueChange = { downPaymentInput = it },
                label = { Text("Down Payment ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rateInput,
                onValueChange = { rateInput = it },
                label = { Text("Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = termYearsInput,
                onValueChange = { termYearsInput = it },
                label = { Text("Loan Term (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Monthly Principal & Interest:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val fmt = String.format(Locale.US, "$%,.2f", result.monthlyPayment)
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Loan Amount:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.loanAmount), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Interest over $termYears Years:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalInterest), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedSavingsGoalCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var targetInput by remember { mutableStateOf("50000") }
    var monthsInput by remember { mutableStateOf("24") }
    var annualRateInput by remember { mutableStateOf("4.0") }

    val target = targetInput.parseFlexibleDouble() ?: 0.0
    val months = monthsInput.parseFlexibleDouble() ?: 24.0
    val rate = annualRateInput.parseFlexibleDouble() ?: 0.0
    val years = (months / 12.0).coerceAtLeast(0.01)
    val result = engine.calculateSavingsGoal(target, rate, years)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Savings Target Goal ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = monthsInput,
                onValueChange = { monthsInput = it },
                label = { Text("Timeline (Months)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = annualRateInput,
                onValueChange = { annualRateInput = it },
                label = { Text("Interest Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Required Monthly Deposit:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val fmt = String.format(Locale.US, "$%,.2f", result.monthlyDepositNeeded)
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Out-of-Pocket Deposits:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalDeposited), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Interest Earned on Growth:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalInterestEarned), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedCompoundInterestCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var principalInput by remember { mutableStateOf("10000") }
    var annualReturnInput by remember { mutableStateOf("7.5") }
    var yearsInput by remember { mutableStateOf("10") }

    val principal = principalInput.parseFlexibleDouble() ?: 0.0
    val rate = annualReturnInput.parseFlexibleDouble() ?: 0.0
    val years = yearsInput.parseFlexibleDouble() ?: 5.0
    val result = engine.calculateCompoundInterest(principal, rate, years)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = principalInput,
            onValueChange = { principalInput = it },
            label = { Text("Initial Investment ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = annualReturnInput,
                onValueChange = { annualReturnInput = it },
                label = { Text("Annual Return (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = yearsInput,
                onValueChange = { yearsInput = it },
                label = { Text("Years") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Future Investment Balance:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val fmt = String.format(Locale.US, "$%,.2f", result.totalAmount)
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Compound Profit:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalInterestEarned), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Return Percentage (ROI):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    val roi = if (principal > 0.0) (result.totalInterestEarned / principal) * 100.0 else 0.0
                    Text(String.format(Locale.US, "%.1f%%", roi), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
fun DedicatedRatioCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var aInput by remember { mutableStateOf("16") }
    var bInput by remember { mutableStateOf("9") }
    var cInput by remember { mutableStateOf("1920") }

    val a = aInput.parseFlexibleDouble() ?: 1.0
    val b = bInput.parseFlexibleDouble() ?: 1.0
    val c = cInput.parseFlexibleDouble() ?: 1.0
    val xVal = engine.calculateRatio(a, b, c)

    fun gcd(n1: Long, n2: Long): Long {
        var x = kotlin.math.abs(n1)
        var y = kotlin.math.abs(n2)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return if (x == 0L) 1L else x
    }
    val g = gcd(a.toLong().coerceAtLeast(1L), b.toLong().coerceAtLeast(1L))
    val simplifiedRatio = "${a.toLong() / g} : ${b.toLong() / g}"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Solve Proportion (A : B = C : X):", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = aInput, onValueChange = { aInput = it }, label = { Text("A") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = bInput, onValueChange = { bInput = it }, label = { Text("B") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = cInput, onValueChange = { cInput = it }, label = { Text("C") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Resulting Value X:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "%.2f", xVal).trimEnd('0').trimEnd('.')
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Simplified Ratio:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(simplifiedRatio, fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedAverageCalcView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var numbersInput by remember { mutableStateOf("12, 18, 25, 30, 42, 18, 55") }

    val numbers = numbersInput.split(",", " ", "\n").mapNotNull { it.trim().parseFlexibleDouble() }
    val result = engine.calculateAverage(numbers)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = numbersInput,
            onValueChange = { numbersInput = it },
            label = { Text("Numbers (comma or space separated)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Arithmetic Mean (Average):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "%.2f", result.mean).trimEnd('0').trimEnd('.')
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Median:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.2f", result.median).trimEnd('0').trimEnd('.'), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mode:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(result.mode?.let { String.format(Locale.US, "%.2f", it).trimEnd('0').trimEnd('.') } ?: "None", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Count / Range:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("${result.count} items (${result.min} .. ${result.max})", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedProfitMarginView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var costInput by remember { mutableStateOf("120") }
    var sellInput by remember { mutableStateOf("180") }

    val cost = costInput.parseFlexibleDouble() ?: 0.0
    val sell = sellInput.parseFlexibleDouble() ?: 0.0
    val result = engine.calculateProfitAndMargin(cost, sell)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = costInput,
                onValueChange = { costInput = it },
                label = { Text("Cost Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = sellInput,
                onValueChange = { sellInput = it },
                label = { Text("Selling Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Net Profit:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "$%,.2f", result.profit)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = if (result.profit >= 0) BentoEmerald else MaterialTheme.colorScheme.error)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gross Profit Margin:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.1f%%", result.profitMarginPercent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Markup on Cost:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.1f%%", result.markupPercent), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
fun DedicatedSimpleInterestView(copyAction: (String) -> Unit) {
    var principalInput by remember { mutableStateOf("10000") }
    var rateInput by remember { mutableStateOf("7.5") }
    var yearsInput by remember { mutableStateOf("3") }

    val p = principalInput.parseFlexibleDouble() ?: 0.0
    val r = rateInput.parseFlexibleDouble() ?: 0.0
    val t = yearsInput.parseFlexibleDouble() ?: 0.0
    val interest = p * (r / 100.0) * t
    val total = p + interest

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = principalInput,
            onValueChange = { principalInput = it },
            label = { Text("Principal Amount ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rateInput,
                onValueChange = { rateInput = it },
                label = { Text("Annual Rate (%)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = yearsInput,
                onValueChange = { yearsInput = it },
                label = { Text("Term (Years)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total Interest Earned:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "$%,.2f", interest)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = BentoEmerald)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Final Balance:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", total), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DedicatedInvestmentReturnView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var initialInput by remember { mutableStateOf("50000") }
    var finalInput by remember { mutableStateOf("95000") }
    var yearsInput by remember { mutableStateOf("5") }

    val initial = initialInput.parseFlexibleDouble() ?: 0.0
    val finalVal = finalInput.parseFlexibleDouble() ?: 0.0
    val years = yearsInput.parseFlexibleDouble() ?: 1.0
    val result = engine.calculateInvestmentReturn(initial, finalVal, years)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = initialInput,
            onValueChange = { initialInput = it },
            label = { Text("Initial Investment ($)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = finalInput,
                onValueChange = { finalInput = it },
                label = { Text("Final Value ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = yearsInput,
                onValueChange = { yearsInput = it },
                label = { Text("Years") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total ROI (Return on Investment):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "+%.1f%%", result.totalRoiPercent)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = BentoEmerald)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Net Gain:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "$%,.2f", result.totalGain), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Annualized Compound Return (CAGR):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.2f%% / yr", result.annualizedRoiPercent), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DedicatedFuelCostView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var distanceInput by remember { mutableStateOf("450") }
    var consumptionInput by remember { mutableStateOf("7.8") }
    var priceInput by remember { mutableStateOf("1.65") }

    val dist = distanceInput.parseFlexibleDouble() ?: 0.0
    val cons = consumptionInput.parseFlexibleDouble() ?: 0.0
    val price = priceInput.parseFlexibleDouble() ?: 0.0
    val result = engine.calculateFuelCost(dist, cons, price)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = distanceInput,
            onValueChange = { distanceInput = it },
            label = { Text("Trip Distance (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = consumptionInput,
                onValueChange = { consumptionInput = it },
                label = { Text("Liters / 100km") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = priceInput,
                onValueChange = { priceInput = it },
                label = { Text("Price per Liter ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Total Fuel Cost:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "$%,.2f", result.totalCost)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Fuel Required:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.1f Liters", result.fuelNeeded), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedSpeedDistanceTimeView(copyAction: (String) -> Unit) {
    var distanceKmInput by remember { mutableStateOf("120") }
    var timeHoursInput by remember { mutableStateOf("1.5") }

    val dist = distanceKmInput.parseFlexibleDouble() ?: 0.0
    val time = timeHoursInput.parseFlexibleDouble() ?: 1.0
    val speedKmh = if (time > 0) dist / time else 0.0
    val speedMph = speedKmh * 0.621371

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = distanceKmInput,
                onValueChange = { distanceKmInput = it },
                label = { Text("Distance (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = timeHoursInput,
                onValueChange = { timeHoursInput = it },
                label = { Text("Time (hours)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Calculated Average Speed:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = String.format(Locale.US, "%.1f km/h", speedKmh)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Miles Per Hour:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(Locale.US, "%.1f mph", speedMph), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedFractionView(copyAction: (String) -> Unit) {
    val engine = remember { EverydayCalculatorsEngine() }
    var n1 by remember { mutableStateOf("3") }
    var d1 by remember { mutableStateOf("4") }
    var op by remember { mutableStateOf("+") }
    var n2 by remember { mutableStateOf("2") }
    var d2 by remember { mutableStateOf("5") }

    val num1 = n1.parseFlexibleInt()?.toLong() ?: 0L
    val den1 = (d1.parseFlexibleInt()?.toLong() ?: 1L).coerceAtLeast(1L)
    val num2 = n2.parseFlexibleInt()?.toLong() ?: 0L
    val den2 = (d2.parseFlexibleInt()?.toLong() ?: 1L).coerceAtLeast(1L)
    val result: FractionCalculationResult? = runCatching { engine.calculateFraction(num1, den1, num2, den2, op) }.getOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = n1, onValueChange = { n1 = it }, label = { Text("Num 1") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = d1, onValueChange = { d1 = it }, label = { Text("Den 1") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("+", "-", "×", "÷").forEach { o ->
                    FilterChip(selected = op == o, onClick = { op = o }, label = { Text(o) })
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = n2, onValueChange = { n2 = it }, label = { Text("Num 2") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(value = d2, onValueChange = { d2 = it }, label = { Text("Den 2") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
            }
        }

        result?.let { res ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Fraction Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    val fmt = "${res.numerator}/${res.denominator}"
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(text = fmt, fontSize = 32.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { copyAction(fmt) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Decimal Equivalent:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(String.format(Locale.US, "%.4f", res.decimalValue).trimEnd('0').trimEnd('.'), fontWeight = FontWeight.Bold, color = BentoEmerald)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mixed Representation:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(res.mixedRepresentation, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                }
            }
        }
    }
}

