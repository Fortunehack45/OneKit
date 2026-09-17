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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.ContentCopy
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
import com.one.utility.core.processing.CacheInfo
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

    var storageInfo by remember { mutableStateOf(engine.getStorageBreakdown()) }
    var cacheInfo by remember { mutableStateOf(engine.getCacheInfo()) }
    var largeFiles by remember { mutableStateOf<List<LargeFileInfo>>(emptyList()) }
    var duplicateGroups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }

    var isScanning by remember { mutableStateOf(false) }
    var scanStatus by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var fileToDelete by remember { mutableStateOf<File?>(null) }
    var duplicateGroupToClean by remember { mutableStateOf<DuplicateGroup?>(null) }
    var showCleanCacheDialog by remember { mutableStateOf(false) }

    fun refreshAll() {
        isScanning = true
        coroutineScope.launch {
            storageInfo = engine.getStorageBreakdown()
            cacheInfo = engine.getCacheInfo()

            scanStatus = "Scanning for files > 10MB..."
            largeFiles = engine.findLargeFiles(thresholdBytes = 10 * 1024 * 1024L)

            scanStatus = "Checking file hashes for duplicates..."
            duplicateGroups = engine.findAllDuplicateFiles()

            isScanning = false
            scanStatus = null
        }
    }

    LaunchedEffect(Unit) {
        refreshAll()
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Storage & File Cleaner", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Action Notification Banner
            actionMessage?.let { msg ->
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoHoneyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = DockObsidian, modifier = Modifier.size(18.dp))
                            Text(msg, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                        }
                    }
                }
            }

            // 1. Device Storage Overview
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Outlined.Storage, contentDescription = null, tint = DockObsidian)
                            Text("Device Storage Breakdown", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        }

                        LinearProgressIndicator(
                            progress = { (storageInfo.usedPercentage / 100.0).toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = BentoHoney,
                            trackColor = BorderSubtle
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Used: ${engine.formatBytes(storageInfo.usedBytes)} (${storageInfo.usedPercentage.toInt()}%)",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "Free: ${engine.formatBytes(storageInfo.freeBytes)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            // 2. Cache & Temporary Files Cleaner (100% Offline & Safe)
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.CleaningServices, contentDescription = null, tint = DockObsidian)
                                Text("App Cache & Temporary Files", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }
                        }

                        Text(
                            text = "Temporary cache: ${cacheInfo.formattedSize} (${cacheInfo.fileCount} temporary files). Safe to clean anytime — does not delete your saved documents or presets.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = { showCleanCacheDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                            shape = RoundedCornerShape(14.dp),
                            enabled = cacheInfo.totalSizeBytes > 0 && !isScanning,
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Outlined.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (cacheInfo.totalSizeBytes == 0L) "Cache is Completely Clean" else "Clean Cache (${cacheInfo.formattedSize})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 3. Duplicate Files Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Duplicate Files (${duplicateGroups.size} groups)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    TextButton(onClick = { refreshAll() }, enabled = !isScanning) {
                        Text(if (isScanning) "Scanning..." else "Rescan", color = BentoHoney, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (duplicateGroups.isEmpty() && !isScanning) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No identical duplicate content found.", fontSize = 13.sp, color = TextSecondary)
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        engine.createSampleDuplicates()
                                        refreshAll()
                                        actionMessage = "Created sample duplicate test files in cache!"
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Create Sample Duplicate Files for Testing", fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(duplicateGroups) { group ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoPinkLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "SHA-256 Match (${group.files.size} identical copies)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        "Each copy: ${group.formattedSize} • Total: ${engine.formatBytes(group.sizeBytes * group.files.size)}",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }

                                Button(
                                    onClick = { duplicateGroupToClean = group },
                                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text("Keep 1, Clean", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            group.files.forEachIndexed { i, f ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (i == 0) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(BentoMint, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text("ORIGINAL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = DockObsidian)
                                                }
                                            }
                                            Text(f.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                        }
                                        Text(f.parent ?: "", fontSize = 10.sp, color = TextSecondary, maxLines = 1)
                                    }

                                    if (i > 0) {
                                        IconButton(onClick = { fileToDelete = f }, modifier = Modifier.size(28.dp)) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Copy", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Large Files Section
            item {
                Text("Large Files (${largeFiles.size})", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary)
            }

            if (largeFiles.isEmpty() && !isScanning) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Text("No excessively large files detected (> 10MB).", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(16.dp))
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
                                Text("${item.formattedSize} • ${item.file.parent ?: ""}", fontSize = 11.sp, color = TextSecondary, maxLines = 1)
                            }
                            IconButton(onClick = { fileToDelete = item.file }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Explicit Confirmation Dialog for Cache Cleaning
    if (showCleanCacheDialog) {
        AlertDialog(
            onDismissRequest = { showCleanCacheDialog = false },
            title = { Text("Clean App Cache?", fontWeight = FontWeight.Bold) },
            text = {
                Text("This will delete ${cacheInfo.formattedSize} of temporary files and image rendering caches. Saved exports in your Downloads folder will NOT be touched.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCleanCacheDialog = false
                        coroutineScope.launch {
                            val freed = engine.clearCache()
                            actionMessage = "Cache cleared! Freed ${engine.formatBytes(freed)}."
                            refreshAll()
                        }
                    }
                ) {
                    Text("Clean Cache", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCleanCacheDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Explicit Confirmation Dialog for Batch Duplicate Deletion
    duplicateGroupToClean?.let { group ->
        AlertDialog(
            onDismissRequest = { duplicateGroupToClean = null },
            title = { Text("Delete Duplicate Copies?", fontWeight = FontWeight.Bold) },
            text = {
                Text("The first original copy will be preserved. ${group.files.size - 1} redundant duplicate copies will be permanently deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val deleted = engine.deleteDuplicateCopies(group)
                            actionMessage = "Deleted $deleted duplicate copies!"
                            duplicateGroupToClean = null
                            refreshAll()
                        }
                    }
                ) {
                    Text("Delete Duplicates", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { duplicateGroupToClean = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Explicit Confirmation Dialog for Individual File Deletion
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to permanently delete \"${file.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        file.delete()
                        actionMessage = "Deleted \"${file.name}\""
                        fileToDelete = null
                        refreshAll()
                    }
                ) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
