package com.one.utility.feature.privacy

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.PasswordGeneratorEngine
import com.one.utility.core.processing.PasswordOptions
import java.text.DecimalFormat

data class PasswordSecurityAnalysis(
    val poolSize: Int,
    val combinationsFormatted: String,
    val entropyBits: Double,
    val crackTimeOffline: String,
    val crackTimeOnline: String,
    val strengthTier: String,
    val strengthColor: Color,
    val strengthFraction: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { PasswordGeneratorEngine() }

    var length by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var excludeAmbiguous by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }
    var isCopied by remember { mutableStateOf(false) }

    fun generate() {
        val options = PasswordOptions(
            length = length.toInt(),
            includeUppercase = includeUpper,
            includeLowercase = includeLower,
            includeNumbers = includeNumbers,
            includeSymbols = includeSymbols,
            excludeAmbiguous = excludeAmbiguous
        )
        generatedPassword = engine.generatePassword(options)
        isCopied = false
    }

    LaunchedEffect(Unit) {
        generate()
    }

    // Combinatorial math & entropy calculation
    val analysis = remember(length, includeUpper, includeLower, includeNumbers, includeSymbols, excludeAmbiguous) {
        var pool = 0
        if (includeUpper) pool += if (excludeAmbiguous) 24 else 26
        if (includeLower) pool += if (excludeAmbiguous) 24 else 26
        if (includeNumbers) pool += if (excludeAmbiguous) 8 else 10
        if (includeSymbols) pool += 32
        if (pool <= 0) pool = 1

        val len = length.toInt()
        val entropy = len * (kotlin.math.log2(pool.toDouble()))
        val combinationsDouble = Math.pow(pool.toDouble(), len.toDouble())

        val combinationsStr = when {
            combinationsDouble < 1e6 -> "%,d".format(combinationsDouble.toLong())
            combinationsDouble < 1e9 -> "${"%.2f".format(combinationsDouble / 1e6)} Million"
            combinationsDouble < 1e12 -> "${"%.2f".format(combinationsDouble / 1e9)} Billion"
            combinationsDouble < 1e15 -> "${"%.2f".format(combinationsDouble / 1e12)} Trillion"
            combinationsDouble < 1e18 -> "${"%.2f".format(combinationsDouble / 1e15)} Quadrillion"
            combinationsDouble < 1e21 -> "${"%.2f".format(combinationsDouble / 1e18)} Quintillion"
            else -> {
                val df = DecimalFormat("0.##E0")
                df.format(combinationsDouble)
            }
        }

        val secondsOffline = (combinationsDouble / 2.0) / 10_000_000_000.0
        val crackOffline = when {
            secondsOffline < 0.001 -> "Instant (< 1 ms)"
            secondsOffline < 1.0 -> "< 1 second"
            secondsOffline < 60.0 -> "${"%.1f".format(secondsOffline)} seconds"
            secondsOffline < 3600.0 -> "${"%.1f".format(secondsOffline / 60.0)} minutes"
            secondsOffline < 86400.0 -> "${"%.1f".format(secondsOffline / 3600.0)} hours"
            secondsOffline < 86400.0 * 365.0 -> "${"%.1f".format(secondsOffline / 86400.0)} days"
            secondsOffline < 86400.0 * 365.0 * 100.0 -> "${"%.1f".format(secondsOffline / (86400.0 * 365.0))} years"
            secondsOffline < 86400.0 * 365.0 * 100_000.0 -> "${"%.0f".format(secondsOffline / (86400.0 * 365.0 * 100.0))} centuries"
            else -> "Trillions of years (Unbreakable)"
        }

        val secondsOnline = (combinationsDouble / 2.0) / 1000.0
        val crackOnline = when {
            secondsOnline < 60.0 -> "${"%.0f".format(secondsOnline)} seconds"
            secondsOnline < 3600.0 -> "${"%.1f".format(secondsOnline / 60.0)} minutes"
            secondsOnline < 86400.0 -> "${"%.1f".format(secondsOnline / 3600.0)} hours"
            secondsOnline < 86400.0 * 365.0 -> "${"%.0f".format(secondsOnline / 86400.0)} days"
            else -> "Centuries (Guaranteed Safe)"
        }

        val tier = when {
            entropy < 36 -> "Very Weak"
            entropy < 50 -> "Weak"
            entropy < 65 -> "Moderate"
            entropy < 80 -> "Strong"
            entropy < 100 -> "Very Strong"
            else -> "Military Grade"
        }

        val color = when {
            entropy < 36 -> Color(0xFFE53935)
            entropy < 50 -> Color(0xFFFF9800)
            entropy < 65 -> Color(0xFFFFC107)
            entropy < 80 -> Color(0xFF4CAF50)
            entropy < 100 -> Color(0xFF00BCD4)
            else -> Color(0xFF9C27B0)
        }

        val fraction = (entropy.toFloat() / 128f).coerceIn(0.1f, 1f)

        PasswordSecurityAnalysis(
            poolSize = pool,
            combinationsFormatted = combinationsStr,
            entropyBits = entropy,
            crackTimeOffline = crackOffline,
            crackTimeOnline = crackOnline,
            strengthTier = tier,
            strengthColor = color,
            strengthFraction = fraction
        )
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Password Generator", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppTheme.colors.textPrimary)
                        Text("Cryptographic & Combinatorial Analysis", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                    }
                },
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
            // 1. Password Display Box
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GENERATED PASSWORD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textTertiary,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(analysis.strengthColor))
                                Text(
                                    text = analysis.strengthTier,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = analysis.strengthColor
                                )
                            }
                        }

                        Text(
                            text = generatedPassword,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AppTheme.colors.textPrimary,
                            lineHeight = 28.sp
                        )

                        // Strength Progress Bar
                        LinearProgressIndicator(
                            progress = { analysis.strengthFraction },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = analysis.strengthColor,
                            trackColor = AppTheme.colors.borderSubtle
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { generate() },
                                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.textPrimary)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Regenerate", fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = {
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    cm.setPrimaryClip(ClipData.newPlainText("ONE Password", generatedPassword))
                                    isCopied = true
                                    Toast.makeText(context, "Password copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isCopied) "Copied!" else "Copy Password",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Exact Combinations & Brute-Force Complexity Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Outlined.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Combinatorial & Crack-Time Complexity",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AppTheme.colors.textPrimary
                            )
                        }

                        // Total Combinations Metric
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AppTheme.colors.canvasBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "TOTAL POSSIBLE COMBINATIONS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.textTertiary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = analysis.combinationsFormatted,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Pool of ${analysis.poolSize} characters across ${length.toInt()} positions (${analysis.poolSize}^${length.toInt()})",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textTertiary
                                )
                            }
                        }

                        // Crack Time Breakdown
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Max),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.Speed, contentDescription = null, tint = analysis.strengthColor, modifier = Modifier.size(14.dp))
                                            Text("GPU Cluster Crack", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textTertiary, maxLines = 1, softWrap = false)
                                        }
                                        Text(
                                            text = analysis.crackTimeOffline,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.textPrimary,
                                            maxLines = 2
                                        )
                                    }
                                    Text("At 10B guesses/sec", fontSize = 10.sp, color = AppTheme.colors.textTertiary, maxLines = 1, softWrap = false)
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = AppTheme.colors.canvasBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                            Text("Entropy", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textTertiary, maxLines = 1, softWrap = false)
                                        }
                                        Text(
                                            text = "${"%.1f".format(analysis.entropyBits)} Bits",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                    Text("Information density", fontSize = 10.sp, color = AppTheme.colors.textTertiary, maxLines = 1, softWrap = false)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Customization Controls
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("OPTIONS", style = AppTheme.typography.labelSmall, color = AppTheme.colors.textTertiary, letterSpacing = 1.sp)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Length: ${length.toInt()} characters", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                                Text("${length.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = length,
                                onValueChange = {
                                    length = it
                                    generate()
                                },
                                valueRange = 8f..32f,
                                steps = 23,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        HorizontalDivider(color = AppTheme.colors.borderSubtle)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Uppercase (A-Z)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = includeUpper,
                                onCheckedChange = { includeUpper = it; generate() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Lowercase (a-z)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = includeLower,
                                onCheckedChange = { includeLower = it; generate() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Numbers (0-9)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = includeNumbers,
                                onCheckedChange = { includeNumbers = it; generate() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Symbols (!@#$)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = includeSymbols,
                                onCheckedChange = { includeSymbols = it; generate() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Exclude Ambiguous (0, O, 1, l)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = excludeAmbiguous,
                                onCheckedChange = { excludeAmbiguous = it; generate() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
