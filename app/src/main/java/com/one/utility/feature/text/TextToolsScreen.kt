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

    // Find & replace state
    var showFindReplace by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceQuery by remember { mutableStateOf("") }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ONE Text", text))
        copiedMessage = "Copied to clipboard!"
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Text Analyzer & Tools", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            // 1. Live Stats Banner (Words, Characters, Lines, Sentences, Reading Time)
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.words}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                                Text("Words", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.charactersWithSpaces}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                                Text("Characters", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.lines}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                                Text("Lines", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${stats.sentences}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppTheme.colors.textPrimary)
                                Text("Sentences", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                            }
                        }
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Estimated Reading Time: ~${stats.estimatedReadingTimeMinutes} min",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Without Spaces: ${stats.charactersWithoutSpaces}",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                }
            }

            // 2. Input Text Area
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = {
                                inputText = it
                                copiedMessage = null
                            },
                            placeholder = { Text("Paste or type your text here...", color = AppTheme.colors.textMuted) },
                            modifier = Modifier.fillMaxWidth().height(160.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = AppTheme.colors.textPrimary,
                                unfocusedTextColor = AppTheme.colors.textPrimary,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
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
                Text("Text Formatting & Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    // Row 1: Case transformations (single line guaranteed)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.toUpperCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("UPPER", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.toLowerCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("lower", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.toTitleCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Title", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.toSentenceCase(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Sentence", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }
                    }

                    // Row 2: Cleaning & Organization
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.removeExtraSpaces(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Trim", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.removeDuplicateLines(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Dedupe", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.sortLinesAlphabetically(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Sort", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.reverseText(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Reverse", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }
                    }

                    // Row 3: Generators & Helpers
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { inputText = engine.cleanText(inputText) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Clean", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { inputText = engine.generateLoremIpsum(2) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text("Lorem", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }

                        Button(
                            onClick = { showFindReplace = !showFindReplace },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
                            colors = obsidianButtonColors()
                        ) { Text(if (showFindReplace) "Close" else "Find/Rep", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false) }
                    }

                    if (showFindReplace) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = AppTheme.colors.surfaceCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = findQuery,
                                    onValueChange = { findQuery = it },
                                    label = { Text("Find Text") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                OutlinedTextField(
                                    value = replaceQuery,
                                    onValueChange = { replaceQuery = it },
                                    label = { Text("Replace With") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Button(
                                    onClick = { inputText = engine.findAndReplace(inputText, findQuery, replaceQuery) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = obsidianButtonColors()
                                ) { Text("Replace All", fontWeight = FontWeight.Bold, color = Color.White) }
                            }
                        }
                    }

                    Button(
                        onClick = { copyToClipboard(inputText) },
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(top = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = obsidianButtonColors()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Transformed Text", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}