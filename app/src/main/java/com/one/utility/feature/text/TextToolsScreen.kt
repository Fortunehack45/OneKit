package com.one.utility.feature.text

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.TextToolsEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToolsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { TextToolsEngine() }

    var inputText by remember { mutableStateOf("") }
    val stats = remember(inputText) { engine.analyze(inputText) }
    var copiedMessage by remember { mutableStateOf<String?>(null) }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ONE Text", text))
        copiedMessage = "Copied to clipboard!"
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Text Tools", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            // 1. Live Stats Banner
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.words}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                            Text("Words", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.charactersWithSpaces}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                            Text("Characters", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.lines}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                            Text("Lines", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.sentences}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                            Text("Sentences", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }
            }

            // 2. Input Text Area
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                copiedMessage = null
                            },
                            placeholder = { Text("Paste or type your text here...", color = AppTheme.colors.textMuted) },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary,
                                focusedContainerColor = AppTheme.colors.canvasBackground,
                                unfocusedContainerColor = AppTheme.colors.canvasBackground,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = AppTheme.colors.borderSubtle
                            )
                        )

                        copiedMessage?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = BentoHoney, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Transformation Actions
            item {
                Text("Quick Text Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.toUpperCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) { Text("UPPERCASE", fontSize = 12.sp, color = Color.White) }

                        Button(
                            onClick = { inputText = engine.toLowerCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) { Text("lowercase", fontSize = 12.sp, color = Color.White) }

                        Button(
                            onClick = { inputText = engine.toTitleCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Title Case", fontSize = 12.sp, color = Color.White) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.removeExtraSpaces(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (AppTheme.colors.isDark) Color(0xFF2B3A4A) else BentoSky,
                                contentColor = if (AppTheme.colors.isDark) Color.White else Color(0xFF14151B)
                            )
                        ) { Text("Trim Spaces", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                        Button(
                            onClick = { inputText = engine.removeDuplicateLines(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (AppTheme.colors.isDark) Color(0xFF3D253A) else BentoPink,
                                contentColor = if (AppTheme.colors.isDark) Color.White else Color(0xFF14151B)
                            )
                        ) { Text("Deduplicate", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                        Button(
                            onClick = { inputText = engine.sortLinesAlphabetically(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (AppTheme.colors.isDark) Color(0xFF3D3520) else BentoHoney,
                                contentColor = if (AppTheme.colors.isDark) Color.White else Color(0xFF14151B)
                            )
                        ) { Text("Sort Lines", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    }

                    Button(
                        onClick = { copyToClipboard(inputText) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = obsidianButtonColors()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Transformed Text", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
