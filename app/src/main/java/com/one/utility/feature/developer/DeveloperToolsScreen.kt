package com.one.utility.feature.developer

import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ColorDetails
import com.one.utility.core.processing.DeveloperToolsEngine

enum class DevToolTab(val title: String) {
    JSON("JSON"),
    BASE64("Base64"),
    URL("URL Codec"),
    HTML("HTML"),
    JWT("JWT"),
    HASH("Hashes"),
    COLOR("Color Tools"),
    DEVICE("Device Info"),
    UNIX("Unix Time")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperToolsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { DeveloperToolsEngine() }

    var selectedTab by remember { mutableStateOf(DevToolTab.JSON) }

    // JSON state
    var jsonInput by remember { mutableStateOf("{\"name\":\"ONE\",\"offline\":true,\"version\":1}") }
    var jsonOutput by remember { mutableStateOf("") }

    // Base64 state
    var base64Input by remember { mutableStateOf("Hello ONE Utility!") }
    var base64Output by remember { mutableStateOf("") }

    // URL state
    var urlInput by remember { mutableStateOf("https://one.utility/search?query=hello world&category=images") }
    var urlOutput by remember { mutableStateOf("") }

    // HTML state
    var htmlInput by remember { mutableStateOf("<h1>Hello & Welcome to \"ONE\"</h1>") }
    var htmlOutput by remember { mutableStateOf("") }

    // JWT state
    var jwtInput by remember { mutableStateOf("") }
    var jwtOutput by remember { mutableStateOf("") }

    // Hash state
    var hashInput by remember { mutableStateOf("") }
    var hashOutput by remember { mutableStateOf("") }

    // Color state
    var colorHexInput by remember { mutableStateOf("#3B82F6") }
    var colorDetails by remember(colorHexInput) { mutableStateOf(engine.parseColor(colorHexInput)) }

    // Unix time state
    var timestampInput by remember { mutableStateOf("${System.currentTimeMillis() / 1000}") }
    var dateOutput by remember { mutableStateOf("") }

    fun copy(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ONE Dev", text))
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Developer & Tech Tools", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal scrollable Tab row (single-line guaranteed)
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DevToolTab.values()) { tab ->
                        val isSelected = tab == selectedTab
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.cardSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle),
                            modifier = Modifier.clickable { selectedTab = tab }
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                DevToolTab.JSON -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("JSON Formatter & Minifier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = jsonInput,
                                    onValueChange = { jsonInput = it },
                                    label = { Text("Raw JSON") },
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = AppTheme.colors.borderSubtle
                                    )
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { jsonOutput = runCatching { engine.formatJson(jsonInput) }.getOrElse { "Invalid JSON: ${it.localizedMessage}" } },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Pretty (2 spaces)", maxLines = 1, softWrap = false) }
                                    Button(
                                        onClick = { jsonOutput = runCatching { engine.minifyJson(jsonInput) }.getOrElse { "Invalid JSON: ${it.localizedMessage}" } },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Minify", maxLines = 1, softWrap = false) }
                                }
                                if (jsonOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("Output:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                                                IconButton(onClick = { copy(jsonOutput) }) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(jsonOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.BASE64 -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Base64 Encoder / Decoder", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = base64Input,
                                    onValueChange = { base64Input = it },
                                    label = { Text("Input Text / Base64") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { base64Output = engine.base64Encode(base64Input) },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Encode", maxLines = 1, softWrap = false) }
                                    Button(
                                        onClick = { base64Output = runCatching { engine.base64Decode(base64Input) }.getOrElse { "Decode Error: ${it.localizedMessage}" } },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Decode", maxLines = 1, softWrap = false) }
                                }
                                if (base64Output.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(base64Output, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(base64Output) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.URL -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("URL Encoder / Decoder & Parser", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = urlInput,
                                    onValueChange = { urlInput = it },
                                    label = { Text("URL / Parameters") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { urlOutput = engine.urlEncode(urlInput) },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Encode URL", maxLines = 1, softWrap = false) }
                                    Button(
                                        onClick = { urlOutput = runCatching { engine.urlDecode(urlInput) }.getOrElse { "Decode Error" } },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Decode URL", maxLines = 1, softWrap = false) }
                                }
                                if (urlOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(urlOutput, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(urlOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.HTML -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("HTML Entities Escape / Unescape", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = htmlInput,
                                    onValueChange = { htmlInput = it },
                                    label = { Text("HTML String") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { htmlOutput = engine.htmlEscape(htmlInput) },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Escape", maxLines = 1, softWrap = false) }
                                    Button(
                                        onClick = { htmlOutput = engine.htmlUnescape(htmlInput) },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Unescape", maxLines = 1, softWrap = false) }
                                }
                                if (htmlOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(htmlOutput, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(htmlOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.JWT -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("JWT Decoder (Header & Payload)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = jwtInput,
                                    onValueChange = { jwtInput = it },
                                    label = { Text("Paste JWT Token (eyJ...)") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Button(
                                    onClick = {
                                        jwtOutput = runCatching {
                                            val decoded = engine.decodeJwt(jwtInput)
                                            "HEADER:\n${decoded.headerJson}\n\nPAYLOAD:\n${decoded.bodyJson}"
                                        }.getOrElse { "Invalid JWT: ${it.localizedMessage}" }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = obsidianButtonColors(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Decode Token", maxLines = 1, softWrap = false) }
                                if (jwtOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Row {
                                            Text(jwtOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(jwtOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.HASH -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Cryptographic Hash Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = hashInput,
                                    onValueChange = { hashInput = it },
                                    label = { Text("String to Hash") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    listOf("SHA-256", "SHA-512", "MD5").forEach { algo ->
                                        Button(
                                            onClick = { hashOutput = "$algo:\n${engine.hashString(hashInput, algo)}" },
                                            modifier = Modifier.weight(1f),
                                            colors = obsidianButtonColors(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) { Text(algo, maxLines = 1, softWrap = false, fontSize = 11.sp) }
                                    }
                                }
                                if (hashOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(hashOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(hashOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.COLOR -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Color Tools & WCAG Contrast", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = colorHexInput,
                                    onValueChange = { colorHexInput = it },
                                    label = { Text("HEX Color Code (e.g. #3B82F6)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                colorDetails?.let { cd ->
                                    val parsedColor = Color(cd.r, cd.g, cd.b)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(parsedColor)
                                                .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("HEX: ${cd.hex}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                            Text("RGB: rgb(${cd.r}, ${cd.g}, ${cd.b})", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                            Text("HSL: hsl(${cd.h.toInt()}°, ${cd.s.toInt()}%, ${cd.l.toInt()}%)", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        }
                                    }

                                    Divider(color = AppTheme.colors.borderSubtle)

                                    Text("Complementary Color: ${cd.complementaryHex}", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = AppTheme.colors.canvasBackground,
                                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("vs White", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                                Text("${cd.contrastRatioWithWhite}:1", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                                Text(if (cd.contrastRatioWithWhite >= 4.5) "PASS AA" else "FAIL AA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (cd.contrastRatioWithWhite >= 4.5) Color(0xFF10B981) else Color(0xFFEF4444))
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = AppTheme.colors.canvasBackground,
                                            modifier = Modifier.weight(1f).padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("vs Black", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                                Text("${cd.contrastRatioWithBlack}:1", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                                Text(if (cd.contrastRatioWithBlack >= 4.5) "PASS AA" else "FAIL AA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (cd.contrastRatioWithBlack >= 4.5) Color(0xFF10B981) else Color(0xFFEF4444))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.DEVICE -> {
                    item {
                        val configuration = LocalConfiguration.current
                        val density = LocalDensity.current
                        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        val memInfo = ActivityManager.MemoryInfo().apply { actManager?.getMemoryInfo(this) }
                        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                        val batteryLevel = batteryIntent?.let {
                            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                            if (level != -1 && scale != -1) (level * 100 / scale) else -1
                        } ?: -1

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Device & Hardware Telemetry", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                                @Composable
                                fun DeviceRow(label: String, value: String) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                    }
                                }

                                DeviceRow("Device Model", "${Build.MANUFACTURER} ${Build.MODEL}")
                                DeviceRow("Hardware Board", Build.BOARD)
                                DeviceRow("Android OS", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                                DeviceRow("CPU Architecture", Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown")
                                DeviceRow("CPU Cores", "${Runtime.getRuntime().availableProcessors()} Cores")
                                DeviceRow("Screen Width & Height", "${configuration.screenWidthDp}dp × ${configuration.screenHeightDp}dp")
                                DeviceRow("Screen Density Bucket", "${configuration.densityDpi} DPI")
                                DeviceRow("RAM Available / Total", "${memInfo.availMem / (1024 * 1024)} MB / ${memInfo.totalMem / (1024 * 1024)} MB")
                                DeviceRow("Battery Level", if (batteryLevel >= 0) "$batteryLevel%" else "Unknown")
                                DeviceRow("ONE App Version", "v1.0.8 (Offline Build)")
                            }
                        }
                    }
                }

                DevToolTab.UNIX -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Unix Timestamp Converter", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = timestampInput,
                                    onValueChange = { timestampInput = it },
                                    label = { Text("Unix Timestamp (seconds)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            val ts = timestampInput.toLongOrNull()
                                            dateOutput = if (ts != null) engine.timestampToDate(ts) else "Invalid timestamp"
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Convert Date", maxLines = 1, softWrap = false) }
                                    Button(
                                        onClick = {
                                            val now = System.currentTimeMillis() / 1000
                                            timestampInput = now.toString()
                                            dateOutput = engine.timestampToDate(now)
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Now", maxLines = 1, softWrap = false) }
                                }
                                if (dateOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(AppTheme.colors.canvasBackground).padding(12.dp)
                                    ) {
                                        Text(dateOutput, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
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
