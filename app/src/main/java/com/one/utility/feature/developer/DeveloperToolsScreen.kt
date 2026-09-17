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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.one.utility.core.designsystem.pressFeedback
import com.one.utility.core.processing.*
import java.util.Locale
import java.util.UUID

enum class DevToolTab(val title: String) {
    REVERSE("Reverse & Inspect"),
    CODECS("Codecs (Base64/Hex/Bin)"),
    UUID("UUID Inspector"),
    HASH("Hashes & Crack"),
    JWT("JWT Inspector"),
    JSON("JSON & XML"),
    COLOR("Color Tools"),
    DEVICE("Device & Unix")
}

enum class CodecType(val label: String) {
    BASE64("Base64"),
    HEX("Hexadecimal"),
    BINARY("Binary (8-bit)"),
    URL("URL Codec"),
    HTML("HTML Entities")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperToolsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { DeveloperToolsEngine() }

    var selectedTab by remember { mutableStateOf(DevToolTab.REVERSE) }

    // Universal Reverse & Inspect state
    var reverseInput by remember { mutableStateOf("5f4dcc3b5aa765d61d8327deb882cf99") } // Sample MD5 'password'
    val reverseAnalysis = remember(reverseInput) {
        engine.autoDetectAndDecode(reverseInput)
    }

    // Codecs state
    var selectedCodec by remember { mutableStateOf(CodecType.BASE64) }
    var isEncodeMode by remember { mutableStateOf(false) }
    var codecInput by remember { mutableStateOf("T05FIFV0aWxpdHkgLSBPZmZsaW5lIE9T") }
    var codecError by remember { mutableStateOf<String?>(null) }
    val codecOutput = remember(codecInput, selectedCodec, isEncodeMode) {
        codecError = null
        if (codecInput.isBlank()) ""
        else runCatching {
            when (selectedCodec) {
                CodecType.BASE64 -> if (isEncodeMode) engine.base64Encode(codecInput) else engine.base64Decode(codecInput)
                CodecType.HEX -> if (isEncodeMode) engine.textToHex(codecInput, true) else engine.hexToText(codecInput)
                CodecType.BINARY -> if (isEncodeMode) engine.textToBinary(codecInput, true) else engine.binaryToText(codecInput)
                CodecType.URL -> if (isEncodeMode) engine.urlEncode(codecInput) else engine.urlDecode(codecInput)
                CodecType.HTML -> if (isEncodeMode) engine.htmlEscape(codecInput) else engine.htmlUnescape(codecInput)
            }
        }.getOrElse {
            codecError = it.localizedMessage ?: "Decoding failed: Invalid format"
            ""
        }
    }

    // UUID state
    var generatedUuid by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isUuidUppercase by remember { mutableStateOf(false) }
    var uuidInspectInput by remember { mutableStateOf(generatedUuid) }
    val uuidParsed = remember(uuidInspectInput) {
        if (uuidInspectInput.isBlank()) null else engine.parseUuid(uuidInspectInput)
    }

    // Hash state
    var hashInput by remember { mutableStateOf("password123") }
    var hashAlgorithm by remember { mutableStateOf("SHA-256") }
    var crackInput by remember { mutableStateOf("48bb6e862e54f2a795ffc4e541caed4d") } // MD5 for 'hello'
    var crackResult by remember { mutableStateOf<HashReverseResult?>(null) }
    var crackSearched by remember { mutableStateOf(false) }

    // JWT state
    var jwtInput by remember { mutableStateOf("") }
    var jwtOutput by remember { mutableStateOf("") }

    // JSON / XML state
    var jsonInput by remember { mutableStateOf("{\"name\":\"ONE Utility\",\"offline\":true,\"tools\":25}") }
    var jsonOutput by remember { mutableStateOf("") }

    // Color state
    var colorHexInput by remember { mutableStateOf("#4F46E5") }
    var colorDetails by remember(colorHexInput) { mutableStateOf(engine.parseColor(colorHexInput)) }

    // Unix time state
    var timestampInput by remember { mutableStateOf("${System.currentTimeMillis() / 1000}") }
    var dateOutput by remember { mutableStateOf("") }

    fun copy(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ONE Dev Tools", text))
    }

    fun share(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share output"))
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Developer & Tech Suite", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Horizontal scrollable Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(DevToolTab.entries) { tab ->
                        val isSelected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            shape = AppTheme.shapes.Chip,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                            ),
                            modifier = Modifier.pressFeedback()
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.White else AppTheme.colors.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTab) {
                DevToolTab.REVERSE -> {
                    item {
                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Universal Auto-Inverter", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)
                                        Text("Turn hashes, UUIDs, Base64, Hex & Binary into readable text", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    }
                                }

                                // Preset samples
                                Text("Quick Test Samples:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item {
                                        SuggestionChip(
                                            onClick = { reverseInput = "5f4dcc3b5aa765d61d8327deb882cf99" },
                                            label = { Text("MD5 ('password')", fontSize = 11.sp) }
                                        )
                                    }
                                    item {
                                        SuggestionChip(
                                            onClick = { reverseInput = "123e4567-e89b-12d3-a456-426614174000" },
                                            label = { Text("UUID v1", fontSize = 11.sp) }
                                        )
                                    }
                                    item {
                                        SuggestionChip(
                                            onClick = { reverseInput = "48656c6c6f20576f726c6421" },
                                            label = { Text("Hex ('Hello World!')", fontSize = 11.sp) }
                                        )
                                    }
                                    item {
                                        SuggestionChip(
                                            onClick = { reverseInput = "01001111 01001110 01000101" },
                                            label = { Text("Binary ('ONE')", fontSize = 11.sp) }
                                        )
                                    }
                                    item {
                                        SuggestionChip(
                                            onClick = { reverseInput = "T25lS2l0IE9mZmxpbmUgVXRpbGl0eQ==" },
                                            label = { Text("Base64", fontSize = 11.sp) }
                                        )
                                    }
                                }

                                Text("Input Any Encoded / Hashed / UUID String", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textSecondary)
                                OutlinedTextField(
                                    value = reverseInput,
                                    onValueChange = { reverseInput = it },
                                    placeholder = { Text("Paste hash, UUID, Base64, Hex, Binary, or URL...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth().height(110.dp),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                // Analysis Result Display
                                Surface(
                                    shape = AppTheme.shapes.Card,
                                    color = if (reverseAnalysis.isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else AppTheme.colors.canvasBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (reverseAnalysis.isSuccess) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else AppTheme.colors.borderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = AppTheme.shapes.Badge,
                                                color = if (reverseAnalysis.isSuccess) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                                            ) {
                                                Text(
                                                    text = reverseAnalysis.detectedType,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (reverseAnalysis.isSuccess) Color.White else AppTheme.colors.textSecondary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }

                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    onClick = { copy(reverseAnalysis.readableOutput) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                                IconButton(
                                                    onClick = { share(reverseAnalysis.readableOutput) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }

                                        Text(
                                            text = "Readable Decoded Output:",
                                            fontSize = 12.sp,
                                            color = AppTheme.colors.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )

                                        Text(
                                            text = reverseAnalysis.readableOutput,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppTheme.colors.textPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )

                                        if (reverseAnalysis.details.isNotEmpty()) {
                                            HorizontalDivider(color = AppTheme.colors.borderSubtle)
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                reverseAnalysis.details.forEach { (k, v) ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(k, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                        Text(v, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
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

                DevToolTab.CODECS -> {
                    item {
                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Universal Codec Studio", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)

                                // Codec type selection pills
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(CodecType.values()) { codec ->
                                        val isSel = selectedCodec == codec
                                        FilterChip(
                                            selected = isSel,
                                            onClick = { selectedCodec = codec },
                                            label = { Text(codec.label, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                // Encode / Decode switcher
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { isEncodeMode = true },
                                        modifier = Modifier.weight(1f).height(40.dp).pressFeedback(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isEncodeMode) MaterialTheme.colorScheme.primary else AppTheme.colors.canvasBackground,
                                            contentColor = if (isEncodeMode) Color.White else AppTheme.colors.textPrimary
                                        ),
                                        shape = AppTheme.shapes.Chip
                                    ) {
                                        Text("Encode →", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { isEncodeMode = false },
                                        modifier = Modifier.weight(1f).height(40.dp).pressFeedback(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isEncodeMode) MaterialTheme.colorScheme.primary else AppTheme.colors.canvasBackground,
                                            contentColor = if (!isEncodeMode) Color.White else AppTheme.colors.textPrimary
                                        ),
                                        shape = AppTheme.shapes.Chip
                                    ) {
                                        Text("← Decode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }

                                Text(
                                    text = if (isEncodeMode) "Plain Text to Encode" else "${selectedCodec.label} to Decode",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.textSecondary
                                )
                                OutlinedTextField(
                                    value = codecInput,
                                    onValueChange = { codecInput = it },
                                    placeholder = { Text("Enter string...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                codecError?.let { err ->
                                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }

                                if (codecOutput.isNotEmpty()) {
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text("Converted Result:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                IconButton(onClick = { copy(codecOutput) }, modifier = Modifier.size(30.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(
                                                text = codecOutput,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp,
                                                color = AppTheme.colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.UUID -> {
                    item {
                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("UUID Generator & Deep Inspector", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)

                                // Current UUID display
                                val displayUuid = if (isUuidUppercase) generatedUuid.uppercase(Locale.US) else generatedUuid.lowercase(Locale.US)
                                Surface(
                                    shape = AppTheme.shapes.Card,
                                    color = AppTheme.colors.canvasBackground,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Active UUID v4:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                            IconButton(onClick = { copy(displayUuid) }, modifier = Modifier.size(30.dp)) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Text(
                                            text = displayUuid,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            generatedUuid = UUID.randomUUID().toString()
                                            uuidInspectInput = generatedUuid
                                        },
                                        shape = AppTheme.shapes.Chip,
                                        modifier = Modifier.pressFeedback()
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Generate New v4", fontSize = 12.sp)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Uppercase", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                        Spacer(Modifier.width(6.dp))
                                        Switch(checked = isUuidUppercase, onCheckedChange = { isUuidUppercase = it })
                                    }
                                }

                                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                                Text("Inspect Any UUID (Extract Version, Node, Timestamp):", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textSecondary)
                                OutlinedTextField(
                                    value = uuidInspectInput,
                                    onValueChange = { uuidInspectInput = it },
                                    placeholder = { Text("Paste UUID string (e.g. 123e4567-e89b-12d3...)", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                uuidParsed?.let { details ->
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Version", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                Text(details.versionName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Variant", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                Text(details.variant, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                            }
                                            details.formattedTimestamp?.let { ts ->
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Timestamp (UTC)", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                    Text(ts, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("MAC / Node ID", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                Text(details.nodeId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Clock Sequence", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                Text(details.clockSequence, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
                                            }
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Decimal (BigInt)", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                Text(details.decimalValue.take(16) + "...", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
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
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Cryptographic Hash & Reverse Cracker", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)

                                // Generate Section
                                Text("1. Generate Cryptographic Hash", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = hashInput,
                                    onValueChange = { hashInput = it },
                                    placeholder = { Text("Text to hash...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(listOf("SHA-256", "SHA-512", "MD5", "SHA-1")) { algo ->
                                        FilterChip(
                                            selected = hashAlgorithm == algo,
                                            onClick = { hashAlgorithm = algo },
                                            label = { Text(algo, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                val generatedHash = remember(hashInput, hashAlgorithm) {
                                    if (hashInput.isBlank()) "" else runCatching { engine.hashString(hashInput, hashAlgorithm) }.getOrDefault("")
                                }

                                if (generatedHash.isNotEmpty()) {
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                                Text("$hashAlgorithm Result:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                IconButton(onClick = { copy(generatedHash) }, modifier = Modifier.size(30.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(generatedHash, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                                        }
                                    }
                                }

                                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                                // Reverse / Crack Section
                                Text("2. Reverse Hash Lookup / Offline Cracker", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                Text("Search 100k common dictionary words, 4-digit PINs & sequence numbers.", fontSize = 11.sp, color = AppTheme.colors.textSecondary)

                                OutlinedTextField(
                                    value = crackInput,
                                    onValueChange = {
                                        crackInput = it
                                        crackSearched = false
                                    },
                                    placeholder = { Text("Paste MD5, SHA-1 or SHA-256 hash...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                Button(
                                    onClick = {
                                        crackResult = engine.reverseHash(crackInput)
                                        crackSearched = true
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp).pressFeedback(),
                                    shape = AppTheme.shapes.Chip
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Crack / Reverse Lookup", fontWeight = FontWeight.Bold)
                                }

                                if (crackSearched) {
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = if (crackResult != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (crackResult != null) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (crackResult != null) {
                                                Text("MATCH FOUND!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                                Text("Plaintext: ${crackResult!!.plainText}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
                                                Text("Algorithm: ${crackResult!!.algorithm} | Match: ${crackResult!!.matchType}", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                            } else {
                                                Text("No match found in offline dictionary (100k passwords & PINs).", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
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
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("JWT Decoder & Inspector", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)
                                Text("Input JWT Token (Header.Payload.Signature):", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                OutlinedTextField(
                                    value = jwtInput,
                                    onValueChange = { jwtInput = it },
                                    placeholder = { Text("Paste eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                Button(
                                    onClick = {
                                        jwtOutput = runCatching {
                                            val decoded = engine.decodeJwt(jwtInput)
                                            "HEADER:\n${decoded.headerJson}\n\nPAYLOAD:\n${decoded.bodyJson}"
                                        }.getOrElse { "Invalid JWT: ${it.localizedMessage}" }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp).pressFeedback(),
                                    shape = AppTheme.shapes.Chip
                                ) {
                                    Text("Inspect Token Claims", fontWeight = FontWeight.Bold)
                                }

                                if (jwtOutput.isNotEmpty()) {
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Decoded Token Claims:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                IconButton(onClick = { copy(jwtOutput) }, modifier = Modifier.size(30.dp)) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(jwtOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.JSON -> {
                    item {
                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("JSON Formatter & Minifier", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = jsonInput,
                                    onValueChange = { jsonInput = it },
                                    placeholder = { Text("Paste JSON string here...", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth().height(130.dp),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = { jsonOutput = runCatching { engine.formatJson(jsonInput) }.getOrElse { "Invalid JSON: ${it.localizedMessage}" } },
                                        modifier = Modifier.weight(1f).height(44.dp).pressFeedback(),
                                        shape = AppTheme.shapes.Chip
                                    ) { Text("Beautify", fontWeight = FontWeight.Bold) }

                                    Button(
                                        onClick = { jsonOutput = runCatching { engine.minifyJson(jsonInput) }.getOrElse { "Invalid JSON: ${it.localizedMessage}" } },
                                        modifier = Modifier.weight(1f).height(44.dp).pressFeedback(),
                                        shape = AppTheme.shapes.Chip
                                    ) { Text("Minify", fontWeight = FontWeight.Bold) }
                                }

                                if (jsonOutput.isNotEmpty()) {
                                    Surface(
                                        shape = AppTheme.shapes.Card,
                                        color = AppTheme.colors.canvasBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("Formatted JSON:", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                                IconButton(onClick = { copy(jsonOutput) }, modifier = Modifier.size(30.dp)) {
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

                DevToolTab.COLOR -> {
                    item {
                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Color Studio & WCAG Contrast", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)

                                Text("HEX Color Code (e.g. #4F46E5):", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                OutlinedTextField(
                                    value = colorHexInput,
                                    onValueChange = { colorHexInput = it },
                                    placeholder = { Text("#4F46E5", color = AppTheme.colors.textTertiary) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                colorDetails?.let { cd ->
                                    val parsedColor = Color(cd.r, cd.g, cd.b)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .clip(AppTheme.shapes.Card)
                                                .background(parsedColor)
                                                .border(1.dp, AppTheme.colors.borderSubtle, AppTheme.shapes.Card)
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("HEX: ${cd.hex}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary, fontFamily = FontFamily.Monospace)
                                            Text("RGB: rgb(${cd.r}, ${cd.g}, ${cd.b})", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                            Text("HSL: hsl(${cd.h.toInt()}°, ${cd.s.toInt()}%, ${cd.l.toInt()}%)", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                        }
                                    }

                                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                                    Text("WCAG 2.1 Contrast Ratios:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                        Surface(shape = AppTheme.shapes.Card, color = AppTheme.colors.canvasBackground, modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("vs White", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                                Text("${cd.contrastRatioWithWhite}:1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                                Text(if (cd.contrastRatioWithWhite >= 4.5) "PASS AA" else "FAIL AA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (cd.contrastRatioWithWhite >= 4.5) Color(0xFF10B981) else Color(0xFFEF4444))
                                            }
                                        }
                                        Surface(shape = AppTheme.shapes.Card, color = AppTheme.colors.canvasBackground, modifier = Modifier.weight(1f)) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Text("vs Black", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                                                Text("${cd.contrastRatioWithBlack}:1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                                Text(if (cd.contrastRatioWithBlack >= 4.5) "PASS AA" else "FAIL AA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (cd.contrastRatioWithBlack >= 4.5) Color(0xFF10B981) else Color(0xFFEF4444))
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
                        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
                        val memInfo = ActivityManager.MemoryInfo().apply { actManager?.getMemoryInfo(this) }
                        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                        val batteryLevel = batteryIntent?.let {
                            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                            if (level != -1 && scale != -1) (level * 100 / scale) else -1
                        } ?: -1

                        Surface(
                            shape = AppTheme.shapes.Hero,
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Unix Converter & Hardware Telemetry", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = AppTheme.colors.textPrimary)

                                Text("Unix Epoch Timestamp (Seconds):", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                OutlinedTextField(
                                    value = timestampInput,
                                    onValueChange = { timestampInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = AppTheme.shapes.SubCard,
                                    colors = transparentTextFieldColors()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            val ts = timestampInput.toLongOrNull()
                                            dateOutput = if (ts != null) engine.timestampToDate(ts) else "Invalid timestamp"
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp).pressFeedback(),
                                        shape = AppTheme.shapes.Chip
                                    ) { Text("Convert Date", fontWeight = FontWeight.Bold) }

                                    Button(
                                        onClick = {
                                            val now = System.currentTimeMillis() / 1000
                                            timestampInput = now.toString()
                                            dateOutput = engine.timestampToDate(now)
                                        },
                                        modifier = Modifier.weight(1f).height(42.dp).pressFeedback(),
                                        shape = AppTheme.shapes.Chip
                                    ) { Text("Current Time", fontWeight = FontWeight.Bold) }
                                }

                                if (dateOutput.isNotEmpty()) {
                                    Surface(shape = AppTheme.shapes.Card, color = AppTheme.colors.canvasBackground, modifier = Modifier.fillMaxWidth()) {
                                        Text(dateOutput, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace)
                                    }
                                }

                                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                                Text("Hardware Telemetry", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)

                                @Composable
                                fun DeviceRow(label: String, value: String) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(label, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                                    }
                                }

                                DeviceRow("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                                DeviceRow("Android OS", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                                DeviceRow("CPU Architecture", Build.SUPPORTED_ABIS.firstOrNull() ?: "ARM64")
                                DeviceRow("Screen Size", "${configuration.screenWidthDp}dp × ${configuration.screenHeightDp}dp")
                                DeviceRow("Available RAM", "${memInfo.availMem / (1024 * 1024)} MB")
                                DeviceRow("Battery Level", if (batteryLevel >= 0) "$batteryLevel%" else "N/A")
                            }
                        }
                    }
                }
            }
        }
    }
}
