package com.one.utility.feature.tools

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*

@Composable
fun DedicatedReverseTextView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("The quick brown fox jumps over the lazy dog") }
    var reverseByWord by remember { mutableStateOf(false) }

    val result = remember(textInput, reverseByWord) {
        if (reverseByWord) {
            textInput.split(" ").reversed().joinToString(" ")
        } else {
            textInput.reversed()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = !reverseByWord, onClick = { reverseByWord = false }, label = { Text("Reverse by Characters") }, modifier = Modifier.weight(1f))
            FilterChip(selected = reverseByWord, onClick = { reverseByWord = true }, label = { Text("Reverse by Words") }, modifier = Modifier.weight(1f))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Reversed Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedRemoveDuplicateLinesView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("Apple\nBanana\nOrange\nApple\nGrape\nBanana\nPeach") }

    val result = remember(textInput) {
        val lines = textInput.lines()
        val seen = mutableSetOf<String>()
        val distinct = mutableListOf<String>()
        for (line in lines) {
            if (seen.add(line)) distinct.add(line)
        }
        distinct.joinToString("\n")
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text Lines") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Deduplicated Result (${result.lines().size} lines):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedSortLinesView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("Zebra\nApple\nMango\nBanana\nCherry") }
    var sortDescending by remember { mutableStateOf(false) }

    val result = remember(textInput, sortDescending) {
        val lines = textInput.lines()
        val sorted = if (sortDescending) lines.sortedDescending() else lines.sorted()
        sorted.joinToString("\n")
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Lines to Sort") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = !sortDescending, onClick = { sortDescending = false }, label = { Text("Sort A → Z") }, modifier = Modifier.weight(1f))
            FilterChip(selected = sortDescending, onClick = { sortDescending = true }, label = { Text("Sort Z → A") }, modifier = Modifier.weight(1f))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sorted Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedFindReplaceView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("The quick brown fox jumps over the lazy dog. The fox was quick.") }
    var findQuery by remember { mutableStateOf("fox") }
    var replaceQuery by remember { mutableStateOf("cat") }

    val result = remember(textInput, findQuery, replaceQuery) {
        if (findQuery.isNotEmpty()) textInput.replace(findQuery, replaceQuery) else textInput
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Source Text") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = findQuery, onValueChange = { findQuery = it }, label = { Text("Find") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = replaceQuery, onValueChange = { replaceQuery = it }, label = { Text("Replace With") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Substituted Text:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedReadingTimeView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("Design systems provide a shared library of reusable components and patterns that enable teams to build high quality user interfaces faster. By standardizing typography, colors, and layout metrics, consistency is maintained across all screens of an application.") }

    val words = remember(textInput) { textInput.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size }
    val readingTimeSeconds = (words / 200.0 * 60.0).toInt()
    val speakingTimeSeconds = (words / 130.0 * 60.0).toInt()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text to Analyze") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Estimated Reading & Speech Speed:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                val fmt = "${readingTimeSeconds / 60}m ${readingTimeSeconds % 60}s"
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = fmt, fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { copyAction(fmt) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                HorizontalDivider(color = AppTheme.colors.borderSubtle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Word Count:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$words words", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Speaking Presentation Time (130 WPM):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("${speakingTimeSeconds / 60}m ${speakingTimeSeconds % 60}s", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedCharCounterView() {
    var textInput by remember { mutableStateOf("ONE Utility 2026! 100% Offline Android System.") }

    val totalChars = textInput.length
    val letters = textInput.count { it.isLetter() }
    val digits = textInput.count { it.isDigit() }
    val whitespace = textInput.count { it.isWhitespace() }
    val symbols = totalChars - letters - digits - whitespace

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text for Character Breakdown") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Character Frequency Breakdown:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Characters:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$totalChars", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Alphabetic Letters:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$letters", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Numeric Digits:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$digits", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Spaces & Whitespace:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$whitespace", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Punctuation & Symbols:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$symbols", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedSentenceCounterView() {
    var textInput by remember { mutableStateOf("Welcome to ONE. Each tool has its own dedicated page. Everything runs 100% offline.") }

    val sentences = remember(textInput) {
        textInput.split("[.!?]+".toRegex()).filter { it.trim().isNotEmpty() }
    }
    val words = textInput.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    val avgWords = if (sentences.isNotEmpty()) words.toDouble() / sentences.size else 0.0

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text for Sentence Analysis") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sentence Structure Metrics:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Sentences:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("${sentences.size}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Average Words / Sentence:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text(String.format(java.util.Locale.US, "%.1f words", avgWords), fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedLineCounterView() {
    var textInput by remember { mutableStateOf("Header\n\nItem 1\nItem 2\n\nFooter") }

    val lines = textInput.lines()
    val totalLines = lines.size
    val nonEmptyLines = lines.count { it.isNotBlank() }
    val blankLines = totalLines - nonEmptyLines

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Lines") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Line Analysis:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Lines:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$totalLines", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Non-Empty Lines:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$nonEmptyLines", fontWeight = FontWeight.Bold, color = BentoEmerald)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Blank / Empty Lines:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    Text("$blankLines", fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun DedicatedRemoveSpacesView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("  Too   many     spaces    and   irregular    tabs  here!  ") }
    val cleanText = remember(textInput) { textInput.trim().replace("\\s+".toRegex(), " ") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text with Extra Spaces") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Cleaned Text (Single Spaces):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(cleanText) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Text(cleanText, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedTextCleanerView(copyAction: (String) -> Unit) {
    var textInput by remember { mutableStateOf("<p>Hello <b>World</b>! &amp; welcome to &#34;ONE&#34;.</p>") }
    val cleanText = remember(textInput) {
        textInput
            .replace("<[^>]*>".toRegex(), "")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#34;", "\"")
            .replace("&#39;", "'")
            .trim()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input HTML / Formatted Text") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Stripped Clean Text:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(cleanText) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Text(cleanText, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

