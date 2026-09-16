package com.one.utility.feature.files

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
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
import com.one.utility.core.processing.FileRenamerEngine
import com.one.utility.core.processing.RenameRule
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchRenameScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val engine = remember { FileRenamerEngine() }

    // Mock/Sample files for demonstration or live cache files
    val sampleFiles = remember {
        val cacheFiles = context.cacheDir.listFiles()?.filter { it.isFile } ?: emptyList()
        if (cacheFiles.isNotEmpty()) cacheFiles.take(8)
        else listOf(
            File("IMG_001.jpg"),
            File("IMG_002.jpg"),
            File("IMG_003.jpg"),
            File("IMG_004.jpg")
        )
    }

    var findText by remember { mutableStateOf("IMG") }
    var replaceWith by remember { mutableStateOf("Vacation") }
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var appendNumbering by remember { mutableStateOf(false) }

    val rule = RenameRule(
        findText = findText,
        replaceWith = replaceWith,
        prefix = prefix,
        suffix = suffix,
        appendIndex = appendNumbering
    )

    val previews = remember(findText, replaceWith, prefix, suffix, appendNumbering) {
        engine.previewBatchRename(sampleFiles, rule)
    }

    var renameStatus by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Batch File Renamer", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. Rename Configuration Box
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Pattern Replacement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = findText,
                                onValueChange = { findText = it },
                                label = { Text("Find text") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = replaceWith,
                                onValueChange = { replaceWith = it },
                                label = { Text("Replace with") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = prefix,
                                onValueChange = { prefix = it },
                                label = { Text("Add Prefix") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = suffix,
                                onValueChange = { suffix = it },
                                label = { Text("Add Suffix") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sequential Numbering (_001, _002)", fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = appendNumbering, onCheckedChange = { appendNumbering = it })
                        }
                    }
                }
            }

            // 2. Live Preview Section
            item {
                Text("Live Rename Preview (${previews.size} files)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            }

            items(previews) { p ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(p.originalName, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = TextSecondary)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = BentoHoney, modifier = Modifier.size(16.dp))
                        Text(p.newName, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DockObsidian)
                    }
                }
            }

            // 3. Apply Action Button
            item {
                Button(
                    onClick = {
                        val result = engine.applyRename(previews)
                        result.onSuccess { count ->
                            renameStatus = "Successfully renamed $count files on device."
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                ) {
                    Text("Apply Batch Rename", fontWeight = FontWeight.Bold)
                }

                renameStatus?.let { status ->
                    Text(status, fontSize = 13.sp, color = HeroLavenderDark, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
