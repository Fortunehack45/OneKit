package com.one.utility.feature.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.FolderZip
import androidx.compose.material.icons.outlined.Storage
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
import com.one.utility.core.processing.DuplicateGroup
import com.one.utility.core.processing.LargeFileInfo
import com.one.utility.core.processing.StorageAnalyzerEngine
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageCleanerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { StorageAnalyzerEngine(context) }

    val storageInfo = remember { engine.getStorageBreakdown() }
    var largeFiles by remember { mutableStateOf<List<LargeFileInfo>>(emptyList()) }
    var duplicateGroups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<File?>(null) }

    fun runScan() {
        isScanning = true
        coroutineScope.launch {
            scanStatus = "Scanning for files > 10MB..."
            largeFiles = engine.findLargeFiles(thresholdBytes = 10 * 1024 * 1024L)

            scanStatus = "Checking file hashes for duplicate content..."
            context.externalCacheDir?.let {
                duplicateGroups = engine.findDuplicateFiles(it)
            }
            isScanning = false
            scanStatus = null
        }
    }

    LaunchedEffect(Unit) {
        runScan()
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Storage Analyzer", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. Storage Bar Overview
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Storage, contentDescription = null, tint = DockObsidian)
                            Text("Device Storage", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }

                        LinearProgressIndicator(
                            progress = { (storageInfo.usedPercentage / 100.0).toFloat() },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = BentoHoney,
                            trackColor = BorderSubtle
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Used: ${"%.1f".format(storageInfo.usedBytes.toDouble() / (1024 * 1024 * 1024))} GB (${storageInfo.usedPercentage.toInt()}%)",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Free: ${"%.1f".format(storageInfo.freeBytes.toDouble() / (1024 * 1024 * 1024))} GB",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 2. Large Files Section
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Large Files (${largeFiles.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    TextButton(onClick = { runScan() }, enabled = !isScanning) {
                        Text(if (isScanning) "Scanning..." else "Rescan", color = BentoHoney, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (largeFiles.isEmpty() && !isScanning) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Text("No excessively large files detected in cache.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(largeFiles) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.file.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text(item.formattedSize, fontSize = 12.sp, color = TextSecondary)
                            }
                            IconButton(onClick = { showDeleteConfirmDialog = item.file }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            // 3. Duplicate Files Section
            item {
                Text("Duplicate Files (${duplicateGroups.size} groups)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            }

            if (duplicateGroups.isEmpty() && !isScanning) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Text("No identical duplicate content found.", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(16.dp))
                    }
                }
            } else {
                items(duplicateGroups) { group ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BentoPinkLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Identical SHA-256 Hash (${group.files.size} copies)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            group.files.forEach { f ->
                                Text("• ${f.name} (${f.length() / 1024} KB)", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Explicit Confirmation Dialog for File Deletion
    showDeleteConfirmDialog?.let { fileToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete File?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${fileToDelete.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        fileToDelete.delete()
                        showDeleteConfirmDialog = null
                        runScan()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
