package com.one.utility.feature.developer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.DeveloperToolsEngine

enum class DevToolTab(val title: String) {
    JSON("JSON"),
    BASE64("Base64"),
    JWT("JWT"),
    HASH("Hashes"),
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

    // JWT state
    var jwtInput by remember { mutableStateOf("") }
    var jwtOutput by remember { mutableStateOf("") }

    // Hash state
    var hashInput by remember { mutableStateOf("") }
    var hashOutput by remember { mutableStateOf("") }

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
                title = { Text("Developer Tools", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            // Tab row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DevToolTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.cardSurface)
                                .border(1.dp, if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.title,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            when (selectedTab) {
                DevToolTab.JSON -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("JSON Formatter & Minifier", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = jsonInput,
                                    onValueChange = { jsonInput = it },
                                    label = { Text("Input JSON") },
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            jsonOutput = try {
                                                engine.formatJson(jsonInput)
                                            } catch (e: Exception) {
                                                "Invalid JSON: ${e.message}"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Format", fontWeight = FontWeight.Bold) }

                                    Button(
                                        onClick = {
                                            jsonOutput = try {
                                                engine.minifyJson(jsonInput)
                                            } catch (e: Exception) {
                                                "Invalid JSON: ${e.message}"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = accentButtonColors(BentoSky),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Minify", color = TextPrimary, fontWeight = FontWeight.Bold) }
                                }

                                if (jsonOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AppTheme.colors.bentoSkySubtle)
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(jsonOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(jsonOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                                            }
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
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Base64 Encoder & Decoder", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = base64Input,
                                    onValueChange = { base64Input = it },
                                    label = { Text("Text or Base64 String") },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { base64Output = engine.base64Encode(base64Input) },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Encode", fontWeight = FontWeight.Bold) }

                                    Button(
                                        onClick = {
                                            base64Output = try {
                                                engine.base64Decode(base64Input)
                                            } catch (e: Exception) {
                                                "Error decoding Base64"
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = accentButtonColors(BentoHoney),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("Decode", color = TextPrimary, fontWeight = FontWeight.Bold) }
                                }

                                if (base64Output.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AppTheme.colors.bentoHoneySubtle)
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(base64Output, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(base64Output) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
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
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("JWT Inspector", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = jwtInput,
                                    onValueChange = { jwtInput = it },
                                    label = { Text("Paste JWT Token (header.payload.signature)") },
                                    modifier = Modifier.fillMaxWidth().height(110.dp),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Button(
                                    onClick = {
                                        jwtOutput = try {
                                            val payload = engine.decodeJwt(jwtInput)
                                            "HEADER:\n${payload.headerJson}\n\nPAYLOAD:\n${payload.bodyJson}"
                                        } catch (e: Exception) {
                                            "Invalid JWT: ${e.message}"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = obsidianButtonColors(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Decode JWT", fontWeight = FontWeight.Bold) }

                                if (jwtOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AppTheme.colors.bentoPinkSubtle)
                                            .padding(14.dp)
                                    ) {
                                        Text(jwtOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.HASH -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Cryptographic Hash Generator", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                OutlinedTextField(
                                    value = hashInput,
                                    onValueChange = { hashInput = it },
                                    label = { Text("Text to Hash") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { hashOutput = "SHA-256:\n" + engine.hashString(hashInput, "SHA-256") },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("SHA-256", fontWeight = FontWeight.Bold) }

                                    Button(
                                        onClick = { hashOutput = "SHA-512:\n" + engine.hashString(hashInput, "SHA-512") },
                                        modifier = Modifier.weight(1f),
                                        colors = obsidianButtonColors(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) { Text("SHA-512", fontWeight = FontWeight.Bold) }
                                }

                                if (hashOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AppTheme.colors.bentoSkySubtle)
                                            .padding(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(hashOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary, modifier = Modifier.weight(1f))
                                            IconButton(onClick = { copy(hashOutput) }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DevToolTab.UNIX -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = AppTheme.colors.cardSurface,
                            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
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

                                Button(
                                    onClick = {
                                        val ts = timestampInput.toLongOrNull()
                                        dateOutput = if (ts != null) {
                                            engine.timestampToDate(ts)
                                        } else {
                                            "Invalid timestamp"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = obsidianButtonColors(),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("Convert to Human Date", fontWeight = FontWeight.Bold) }

                                if (dateOutput.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(AppTheme.colors.bentoHoneySubtle)
                                            .padding(14.dp)
                                    ) {
                                        Text(dateOutput, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
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
