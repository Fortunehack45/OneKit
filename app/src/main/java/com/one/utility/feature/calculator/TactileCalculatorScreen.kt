package com.one.utility.feature.calculator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.TactileCalculatorEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CalculationTapeItem(
    val id: Long,
    val expression: String,
    val result: String,
    val formattedTime: String
)

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

    val context = LocalContext.current
    val engine = remember { TactileCalculatorEngine() }
    val view = LocalView.current

    var displayExpression by remember { mutableStateOf(initialExpression ?: "") }
    var currentResult by remember { mutableStateOf("0") }
    var isScientific by remember { mutableStateOf(false) }
    var isDeg by remember { mutableStateOf(true) }

    // Calculation Tape & Audit History
    val tapeHistory = remember { mutableStateListOf<CalculationTapeItem>() }
    var showTapeSheet by remember { mutableStateOf(false) }

    fun copyToClipboard(text: String, label: String = "Calculation") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(context, "Copied $text", Toast.LENGTH_SHORT).show()
    }

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
                        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                        tapeHistory.add(0, CalculationTapeItem(System.currentTimeMillis(), displayExpression, res, timeStr))
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
                title = { Text("Calculator & Tape", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AppTheme.colors.textPrimary)
                    }
                },
                actions = {
                    // Calculation Tape History Action
                    IconButton(onClick = { showTapeSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (tapeHistory.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ) {
                                        Text(
                                            text = if (tapeHistory.size > 99) "99+" else "${tapeHistory.size}",
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = "Calculation Tape",
                                tint = if (tapeHistory.isNotEmpty()) MaterialTheme.colorScheme.primary else AppTheme.colors.textSecondary
                            )
                        }
                    }

                    // 1-Tap Toggle between Standard & Scientific
                    IconButton(onClick = { isScientific = !isScientific }) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = "Toggle Scientific",
                            tint = if (isScientific) MaterialTheme.colorScheme.primary else AppTheme.colors.textSecondary
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CalcNavMode.entries) { mode ->
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
                    .padding(vertical = 4.dp)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
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
                            fontSize = 18.sp,
                            color = AppTheme.colors.textSecondary,
                            maxLines = 2,
                            textAlign = TextAlign.End
                        )
                    }

                    Text(
                        text = currentResult,
                        fontSize = if (currentResult.length > 9) 30.sp else 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Prominent Standard / Scientific Mode Segmented Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (!isScientific) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isScientific = false }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Standard",
                        fontWeight = if (!isScientific) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 12.5.sp,
                        color = if (!isScientific) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(if (isScientific) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { isScientific = true }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Science,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isScientific) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary
                        )
                        Text(
                            text = "Scientific",
                            fontWeight = if (isScientific) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.5.sp,
                            color = if (isScientific) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary
                        )
                    }
                }
            }

            // Scientific Keypad Rows (Expandable)
            AnimatedVisibility(
                visible = isScientific,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val sciRow1 = listOf("sin", "cos", "tan", "log", "ln")
                    val sciRow2 = listOf("sqrt", "x²", "xʸ", "π", "e")

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sciRow1.forEach { key ->
                            KeyButton(
                                label = key,
                                modifier = Modifier.weight(1f).height(38.dp),
                                containerColor = AppTheme.colors.surfaceVariant,
                                contentColor = AppTheme.colors.textPrimary,
                                fontSize = 12.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        sciRow2.forEach { key ->
                            KeyButton(
                                label = key,
                                modifier = Modifier.weight(1f).height(38.dp),
                                containerColor = AppTheme.colors.surfaceVariant,
                                contentColor = AppTheme.colors.textPrimary,
                                fontSize = 12.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }
                }
            }

            // Standard Tactile Keypad
            val standardKeyHeight = if (isScientific) 46.dp else 54.dp
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                key == "=" -> MaterialTheme.colorScheme.primary
                                isOperator -> AppTheme.colors.primaryButton
                                isSpecial -> AppTheme.colors.surfaceVariant
                                else -> AppTheme.colors.cardSurface
                            }

                            val contentColor = when {
                                key == "=" -> MaterialTheme.colorScheme.onPrimary
                                isOperator -> AppTheme.colors.onPrimaryButton
                                else -> AppTheme.colors.textPrimary
                            }

                            KeyButton(
                                label = key,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(standardKeyHeight),
                                containerColor = containerColor,
                                contentColor = contentColor,
                                fontSize = 19.sp,
                                onClick = { onKeyPress(key) }
                            )
                        }
                    }
                }
            }
        }

        // Calculation Tape & Audit History Modal Sheet
        if (showTapeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTapeSheet = false },
                containerColor = AppTheme.colors.surfaceCard,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Outlined.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Calculation Tape",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                        if (tapeHistory.isNotEmpty()) {
                            TextButton(
                                onClick = { tapeHistory.clear() }
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = "Clear Tape",
                                    tint = AppTheme.colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Clear",
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (tapeHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Outlined.ReceiptLong,
                                    contentDescription = null,
                                    tint = AppTheme.colors.textMuted,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    "Tape is empty",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    "Every calculation with '=' will be recorded here.",
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 360.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tapeHistory, key = { it.id }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = AppTheme.colors.canvasBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .pressFeedback {
                                            displayExpression = item.result
                                            currentResult = item.result
                                            showTapeSheet = false
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.formattedTime,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AppTheme.colors.textTertiary
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                text = item.expression,
                                                fontSize = 14.sp,
                                                color = AppTheme.colors.textSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "= ${item.result}",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = AppTheme.colors.textPrimary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                copyToClipboard("${item.expression} = ${item.result}")
                                            },
                                            modifier = Modifier.size(32.dp).pressFeedback()
                                        ) {
                                            Icon(
                                                Icons.Outlined.ContentCopy,
                                                contentDescription = "Copy calculation",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
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
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AppTheme.colors.borderSubtle.copy(alpha = 0.45f)),
        modifier = modifier.pressFeedback(pressedScale = 0.92f) { onClick() }
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
