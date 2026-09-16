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
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Text Tools", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. Live Stats Banner
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.words}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DockObsidian)
                            Text("Words", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.charactersWithSpaces}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DockObsidian)
                            Text("Characters", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.lines}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DockObsidian)
                            Text("Lines", fontSize = 12.sp, color = TextSecondary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.sentences}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = DockObsidian)
                            Text("Sentences", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            // 2. Input Text Area
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                copiedMessage = null
                            },
                            placeholder = { Text("Paste or type your text here...") },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(16.dp)
                        )

                        copiedMessage?.let { msg ->
                            Text(msg, fontSize = 12.sp, color = BentoHoney, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Transformation Actions
            item {
                Text("Quick Text Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.toUpperCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                        ) { Text("UPPERCASE", fontSize = 12.sp) }

                        Button(
                            onClick = { inputText = engine.toLowerCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                        ) { Text("lowercase", fontSize = 12.sp) }

                        Button(
                            onClick = { inputText = engine.toTitleCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                        ) { Text("Title Case", fontSize = 12.sp) }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.removeExtraSpaces(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoSky)
                        ) { Text("Trim Spaces", color = TextPrimary, fontSize = 12.sp) }

                        Button(
                            onClick = { inputText = engine.removeDuplicateLines(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPink)
                        ) { Text("Deduplicate", color = TextPrimary, fontSize = 12.sp) }

                        Button(
                            onClick = { inputText = engine.sortLinesAlphabetically(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoHoney)
                        ) { Text("Sort Lines", color = TextPrimary, fontSize = 12.sp) }
                    }

                    Button(
                        onClick = { copyToClipboard(inputText) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Transformed Text", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
