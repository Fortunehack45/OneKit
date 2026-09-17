package com.one.utility.feature.calculator

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.TactileCalculatorEngine

enum class CalcNavMode(val title: String) {
    KEYPAD("Keypad"),
    SMART_MATH("Smart Math"),
    PERCENTAGE("Percentage"),
    TIP_BILL("Tip & Bill")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TactileCalculatorScreen(
    initialExpression: String? = null,
    onNavigateBack: () -> Unit
) {
    var navMode by remember { mutableStateOf(CalcNavMode.KEYPAD) }

    if (navMode != CalcNavMode.KEYPAD) {
        CalculatorScreen(
            initialExpression = initialExpression,
            onNavigateBack = { navMode = CalcNavMode.KEYPAD }
        )
        return
    }

    val engine = remember { TactileCalculatorEngine() }
    val view = LocalView.current

    var displayExpression by remember { mutableStateOf(initialExpression ?: "") }
    var currentResult by remember { mutableStateOf("0") }
    var isScientific by remember { mutableStateOf(false) }
    var isDeg by remember { mutableStateOf(true) }

    LaunchedEffect(initialExpression) {
        if (!initialExpression.isNullOrBlank()) {
            displayExpression = initialExpression
            val eval = engine.evaluate(initialExpression)
            if (eval.isSuccess) {
                currentResult = engine.formatResult(eval.getOrThrow())
            }
        }
    }

    fun vibrate() {
        try {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (_: Exception) {}
    }

    fun onKeyPress(key: String) {
        vibrate()
        when (key) {
            "C" -> {
                displayExpression = ""
                currentResult = "0"
            }
            "⌫" -> {
                if (displayExpression.isNotEmpty()) {
                    displayExpression = displayExpression.dropLast(1)
                    if (displayExpression.isEmpty()) {
                        currentResult = "0"
                    } else {
                        val eval = engine.evaluate(displayExpression)
                        if (eval.isSuccess) {
                            currentResult = engine.formatResult(eval.getOrThrow())
                        }
                    }
                }
            }
            "=" -> {
                if (displayExpression.isNotBlank()) {
                    val eval = engine.evaluate(displayExpression)
                    if (eval.isSuccess) {
                        val res = engine.formatResult(eval.getOrThrow())
                        currentResult = res
                        displayExpression = res
                    } else {
                        currentResult = "Error"
                    }
                }
            }
            "±" -> {
                if (displayExpression.startsWith("-")) {
                    displayExpression = displayExpression.removePrefix("-")
                } else if (displayExpression.isNotEmpty()) {
                    displayExpression = "-$displayExpression"
                }
            }
            "sin", "cos", "tan", "log", "ln", "sqrt" -> {
                displayExpression = if (key == "sqrt") "${displayExpression}sqrt(" else "$displayExpression$key("
            }
            "x²" -> displayExpression = "$displayExpression^2"
            "xʸ" -> displayExpression = "$displayExpression^"
            "1/x" -> displayExpression = "$displayExpression^(-1)"
            "n!" -> displayExpression = "$displayExpression!"
            "DEG/RAD" -> {
                isDeg = !isDeg
                engine.isDegreeMode = isDeg
                if (displayExpression.isNotBlank()) {
                    val eval = engine.evaluate(displayExpression)
                    if (eval.isSuccess) {
                        currentResult = engine.formatResult(eval.getOrThrow())
                    }
                }
            }
            else -> {
                displayExpression += key
                val eval = engine.evaluate(displayExpression)
                if (eval.isSuccess) {
                    currentResult = engine.formatResult(eval.getOrThrow())
                }
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Calculator", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.textPrimary)
                    }
                },
                actions = {
                    // 1-Tap Toggle between Standard & Scientific
                    IconButton(onClick = { isScientific = !isScientific }) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = "Toggle Scientific",
                            tint = if (isScientific) BentoHoney else AppTheme.colors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppTheme.colors.canvasBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mode Chips Row (Keypad, Smart Math, Percentage, Tip & Bill)
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CalcNavMode.values()) { mode ->
                    val isSelected = navMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.cardSurface)
                            .border(1.dp, if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                            .clickable { navMode = mode }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.title,
                            color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Numeric Display Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isScientific) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AppTheme.colors.surfaceVariant)
                                    .clickable { onKeyPress("DEG/RAD") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (isDeg) "DEG" else "RAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoHoney)
                            }
                        } else {
                            Spacer(Modifier.width(1.dp))
                        }

                        Text(
                            text = displayExpression.ifEmpty { " " },
                            fontSize = 20.sp,
                            color = AppTheme.colors.textSecondary,
                            maxLines = 2,
                            textAlign = TextAlign.End
                        )
                    }

                    Text(
                        text = currentResult,
                        fontSize = if (currentResult.length > 9) 32.sp else 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Scientific Row (Expandable)
            AnimatedVisibility(
                visible = isScientific,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sciRow1 = listOf("sin", "cos", "tan", "log", "ln")
                    val sciRow2 = listOf("sqrt", "x²", "xʸ", "π", "e")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sciRow1.forEach { key ->
                            KeyButton(
                                label = key,
                                modifier = Modifier.weight(1f).height(42.dp),
                                containerColor = AppTheme.colors.surfaceVariant,
                                contentColor = AppTheme.colors.textPrimary,
                                fontSize = 13.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sciRow2.forEach { key ->
                            KeyButton(
                                label = key,
                                modifier = Modifier.weight(1f).height(42.dp),
                                containerColor = AppTheme.colors.surfaceVariant,
                                contentColor = AppTheme.colors.textPrimary,
                                fontSize = 13.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }
                }
            }

            // Standard Tactile Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val row1 = listOf("C", "(", ")", "÷")
                val row2 = listOf("7", "8", "9", "×")
                val row3 = listOf("4", "5", "6", "−")
                val row4 = listOf("1", "2", "3", "+")
                val row5 = listOf("0", ".", "⌫", "=")

                listOf(row1, row2, row3, row4, row5).forEach { rowKeys ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowKeys.forEach { key ->
                            val isOperator = key in listOf("÷", "×", "−", "+", "=")
                            val isSpecial = key in listOf("C", "(", ")", "⌫")

                            val containerColor = when {
                                key == "=" -> BentoHoney
                                isOperator -> AppTheme.colors.primaryButton
                                isSpecial -> AppTheme.colors.surfaceVariant
                                else -> AppTheme.colors.cardSurface
                            }

                            val contentColor = when {
                                key == "=" -> Color(0xFF241500)
                                isOperator -> AppTheme.colors.onPrimaryButton
                                else -> AppTheme.colors.textPrimary
                            }

                            KeyButton(
                                label = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp),
                                containerColor = containerColor,
                                contentColor = contentColor,
                                fontSize = 20.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 18.sp,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier.border(0.75.dp, AppTheme.colors.borderSubtle.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "⌫") {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = label,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}
