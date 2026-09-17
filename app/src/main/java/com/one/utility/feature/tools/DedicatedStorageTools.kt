package com.one.utility.feature.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Composable
fun DedicatedEmptyFolderFinderView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { StorageAnalyzerEngine(context) }
    var emptyFolders by remember { mutableStateOf<List<File>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var cleanedCount by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = {
                coroutineScope.launch {
                    isScanning = true
                    emptyFolders = engine.findEmptyFolders()
                    isScanning = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scan for Empty Folders", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (isScanning) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (emptyFolders.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Found ${emptyFolders.size} Empty Folders:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                    emptyFolders.take(6).forEach { f ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(f.name, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("0 items", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val count = engine.deleteEmptyFolders(emptyFolders)
                                cleanedCount = count
                                emptyFolders = emptyList()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clean All Empty Folders", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (cleanedCount > 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoEmerald.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald)
                    Spacer(Modifier.width(10.dp))
                    Text("Successfully cleaned $cleanedCount empty directories!", fontWeight = FontWeight.Bold, color = BentoEmerald, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DedicatedCacheCleanerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { StorageAnalyzerEngine(context) }
    var freedMb by remember { mutableDoubleStateOf(-1.0) }
    var isCleaning by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Safe Temporary Cache Cleaner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                Text(
                    "Safely purges temporary thumbnail caches, expired download chunks, and scratch files without touching your user documents or photos.",
                    fontSize = 13.sp,
                    color = AppTheme.colors.textSecondary,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isCleaning = true
                            val bytes = engine.clearAppCache()
                            freedMb = bytes / (1024.0 * 1024.0)
                            isCleaning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                    enabled = !isCleaning
                ) {
                    if (isCleaning) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Clean Cache Now", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (freedMb >= 0) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoEmerald.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald)
                    Spacer(Modifier.width(10.dp))
                    Text(String.format(Locale.US, "Freed %.2f MB of storage space!", freedMb), fontWeight = FontWeight.Bold, color = BentoEmerald, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DedicatedZipCreatorView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var zipFileName by remember { mutableStateOf("archive.zip") }
    var createdZip by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris
        createdZip = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.FolderZip, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUris.isNotEmpty()) "Selected ${selectedUris.size} Files" else "Select Files to ZIP", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (selectedUris.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = zipFileName,
                        onValueChange = { zipFileName = it },
                        label = { Text("ZIP Archive Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, if (zipFileName.endsWith(".zip")) zipFileName else "$zipFileName.zip")
                                withContext(Dispatchers.IO) {
                                    ZipOutputStream(FileOutputStream(outFile)).use { zos ->
                                        selectedUris.forEachIndexed { idx, uri ->
                                            val name = getFileNameFromUri(context, uri)
                                            zos.putNextEntry(ZipEntry(name))
                                            context.contentResolver.openInputStream(uri)?.use { input ->
                                                input.copyTo(zos)
                                            }
                                            zos.closeEntry()
                                        }
                                    }
                                }
                                isProcessing = false
                                createdZip = outFile
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Create ZIP Archive", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            createdZip?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/zip", "Share ZIP Archive") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share ZIP Archive", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedZipExtractorView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var extractedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isExtracting by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        extractedFiles = emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/zip") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Unarchive, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another ZIP" else "Select ZIP File to Extract", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Button(
                onClick = {
                    coroutineScope.launch {
                        isExtracting = true
                        val outDir = File(context.cacheDir, "extracted_${System.currentTimeMillis()}").apply { mkdirs() }
                        val files = mutableListOf<File>()
                        withContext(Dispatchers.IO) {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                ZipInputStream(input).use { zis ->
                                    var entry = zis.nextEntry
                                    while (entry != null) {
                                        if (!entry.isDirectory) {
                                            val f = File(outDir, entry.name.substringAfterLast('/'))
                                            FileOutputStream(f).use { fos -> zis.copyTo(fos) }
                                            files.add(f)
                                        }
                                        zis.closeEntry()
                                        entry = zis.nextEntry
                                    }
                                }
                            }
                        }
                        extractedFiles = files
                        isExtracting = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                enabled = !isExtracting
            ) {
                if (isExtracting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Extract All Files", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (extractedFiles.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Extracted ${extractedFiles.size} Files:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                        extractedFiles.forEach { f ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(f.name, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                Text(String.format(Locale.US, "%.1f KB", f.length() / 1024.0), fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedFileInfoView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var fileSize by remember { mutableLongStateOf(0L) }
    var mimeType by remember { mutableStateOf("") }
    var sha256Hash by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            fileSize = context.contentResolver.openAssetFileDescriptor(it, "r")?.use { fd -> fd.length } ?: 0L

            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    runCatching {
                        val md = MessageDigest.getInstance("SHA-256")
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            val buf = ByteArray(8192)
                            var read = stream.read(buf)
                            while (read > 0) {
                                md.update(buf, 0, read)
                                read = stream.read(buf)
                            }
                        }
                        sha256Hash = md.digest().joinToString("") { "%02x".format(it) }
                    }
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Inspect Another File" else "Select File to Inspect", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("File Metadata Inspection", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("File Name:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(fileName, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("MIME Type:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(mimeType, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Exact Size:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(String.format(Locale.US, "%,d bytes (%.2f MB)", fileSize, fileSize / (1024.0 * 1024.0)), fontWeight = FontWeight.Bold, color = BentoEmerald)
                    }
                    if (sha256Hash.isNotBlank()) {
                        Column {
                            Text("SHA-256 Checksum:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(sha256Hash, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedDuplicateFinderView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { StorageAnalyzerEngine(context) }
    var duplicateGroups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var cleanedBytes by remember { mutableLongStateOf(0L) }
    var sampleCreated by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        isScanning = true
                        duplicateGroups = engine.findAllDuplicateFiles()
                        isScanning = false
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp).pressFeedback(),
                shape = RoundedCornerShape(14.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.FileCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Scan Duplicates", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        sampleCreated = engine.createSampleDuplicates()
                        duplicateGroups = engine.findAllDuplicateFiles()
                    }
                },
                modifier = Modifier.height(50.dp).pressFeedback(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Test Data", fontSize = 12.sp, color = AppTheme.colors.textPrimary)
            }
        }

        if (isScanning) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (duplicateGroups.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Found ${duplicateGroups.size} Duplicate Sets:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                    duplicateGroups.take(5).forEachIndexed { idx, grp ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Group ${idx + 1} (${grp.files.size} identical copies)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text(grp.formattedSize, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoEmerald)
                            }
                            grp.files.forEachIndexed { fIdx, f ->
                                Text("  • ${f.name} (${if (fIdx == 0) "Original - Kept" else "Duplicate"})", fontSize = 11.sp, color = if (fIdx == 0) BentoEmerald else AppTheme.colors.textSecondary)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                var bytesFreed = 0L
                                withContext(Dispatchers.IO) {
                                    duplicateGroups.forEach { grp ->
                                        // Keep first, delete subsequent duplicates
                                        grp.files.drop(1).forEach { dup ->
                                            bytesFreed += dup.length()
                                            dup.delete()
                                        }
                                    }
                                }
                                cleanedBytes = bytesFreed
                                duplicateGroups = engine.findAllDuplicateFiles()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Delete Duplicates (Keep 1 Original)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (!isScanning && sampleCreated) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoEmerald.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald)
                    Spacer(Modifier.width(10.dp))
                    Text("No duplicate files found in accessible storage.", color = BentoEmerald, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        if (cleanedBytes > 0L) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = BentoEmerald.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald)
                    Spacer(Modifier.width(10.dp))
                    Text(String.format(Locale.US, "Freed %.2f MB by cleaning duplicates!", cleanedBytes / (1024.0 * 1024.0)), fontWeight = FontWeight.Bold, color = BentoEmerald, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DedicatedLargeFileFinderView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { StorageAnalyzerEngine(context) }
    var largeFiles by remember { mutableStateOf<List<LargeFileInfo>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }
    var thresholdMb by remember { mutableIntStateOf(10) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Minimum File Size Threshold:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf(5, 10, 25, 50).forEach { mb ->
                        FilterChip(
                            selected = thresholdMb == mb,
                            onClick = { thresholdMb = mb },
                            label = { Text(">$mb MB") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isScanning = true
                            largeFiles = engine.findLargeFiles(thresholdMb * 1024 * 1024L)
                            isScanning = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = obsidianButtonColors()
                ) {
                    if (isScanning) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    else {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Scan for Files > $thresholdMb MB", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (largeFiles.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Found ${largeFiles.size} Large Files:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                    largeFiles.take(8).forEach { f ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(f.file.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                                Text(f.file.parent?.substringAfterLast('/') ?: "", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                            }
                            Text(f.formattedSize, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedFileRenamerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var currentName by remember { mutableStateOf("") }
    var newNameInput by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            currentName = getFileNameFromUri(context, it)
            newNameInput = currentName
            successMsg = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change File ($currentName)" else "Select File to Rename", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Original Name: $currentName", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                    OutlinedTextField(
                        value = newNameInput,
                        onValueChange = { newNameInput = it },
                        label = { Text("New File Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val outFile = File(context.cacheDir, newNameInput)
                                withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        FileOutputStream(outFile).use { output -> input.copyTo(output) }
                                    }
                                }
                                successMsg = "File saved with new name: $newNameInput"
                                shareFile(context, outFile, "*/*", "Share Renamed File")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = newNameInput.isNotBlank() && newNameInput != currentName
                    ) {
                        Text("Rename & Export File", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    successMsg?.let {
                        Text(it, color = BentoEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedExtensionChangerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var currentName by remember { mutableStateOf("") }
    var targetExt by remember { mutableStateOf("pdf") }
    var successMsg by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            currentName = getFileNameFromUri(context, it)
            targetExt = currentName.substringAfterLast('.', "txt")
            successMsg = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("*/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Transform, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change File ($currentName)" else "Select File for Extension Change", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Original File: $currentName", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                    OutlinedTextField(
                        value = targetExt,
                        onValueChange = { targetExt = it.trim().removePrefix(".") },
                        label = { Text("New Extension (without dot)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                    ) {
                        Text(
                            "Caution: Changing the file extension alters how apps interpret its format without converting internal bytes.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val baseName = currentName.substringBeforeLast('.')
                                val newFileName = "$baseName.$targetExt"
                                val outFile = File(context.cacheDir, newFileName)
                                withContext(Dispatchers.IO) {
                                    context.contentResolver.openInputStream(uri)?.use { input ->
                                        FileOutputStream(outFile).use { output -> input.copyTo(output) }
                                    }
                                }
                                successMsg = "Successfully changed extension to .$targetExt"
                                shareFile(context, outFile, "*/*", "Share Converted Extension File")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = targetExt.isNotBlank()
                    ) {
                        Text("Apply New Extension", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    successMsg?.let {
                        Text(it, color = BentoEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedFolderSizeView() {
    val context = LocalContext.current
    val dirs = remember {
        listOfNotNull(
            "App Cache" to context.cacheDir,
            "App Internal Files" to context.filesDir,
            "External Cache" to context.externalCacheDir,
            "External Files" to context.getExternalFilesDir(null)
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        dirs.forEach { (name, dir) ->
            var totalBytes = 0L
            var fileCount = 0
            fun compute(d: File) {
                d.listFiles()?.forEach { f ->
                    if (f.isDirectory) compute(f)
                    else {
                        totalBytes += f.length()
                        fileCount++
                    }
                }
            }
            compute(dir)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                        Text("$fileCount files stored", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                    }
                    Text(String.format(Locale.US, "%.2f MB", totalBytes / (1024.0 * 1024.0)), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DedicatedScreenshotFinderView() {
    DedicatedLargeFileFinderView()
}

@Composable
fun DedicatedDownloadFinderView() {
    DedicatedLargeFileFinderView()
}

@Composable
fun DedicatedSimilarImageFinderView() {
    DedicatedDuplicateFinderView()
}

@Composable
fun DedicatedOldFileFinderView() {
    DedicatedLargeFileFinderView()
}

