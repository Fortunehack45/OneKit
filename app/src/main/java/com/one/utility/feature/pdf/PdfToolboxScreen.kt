package com.one.utility.feature.pdf

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.PdfToolboxEngine
import kotlinx.coroutines.launch
import java.io.File

enum class PdfToolMode(val label: String) {
    MERGE("Merge PDFs"),
    PDF_TO_IMAGES("PDF → Images"),
    SPLIT("Split PDF")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToolboxScreen(
    initialMode: PdfToolMode = PdfToolMode.MERGE,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }

    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedPdfUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var singlePdfUri by remember { mutableStateOf<Uri?>(null) }
    var singlePdfPageCount by remember { mutableIntStateOf(0) }
    var singlePdfName by remember { mutableStateOf("") }

    // Split mode state
    var selectedPages by remember { mutableStateOf<List<Int>>(emptyList()) } // 0-based ordered list
    var customRangeText by remember { mutableStateOf("") }
    var splitPdfFile by remember { mutableStateOf<File?>(null) }

    // PDF to images state
    var isJpgFormat by remember { mutableStateOf(false) }
    var outputImages by remember { mutableStateOf<List<File>>(emptyList()) }

    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var outputPdf by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun getFileName(uri: Uri): String {
        var name = "document.pdf"
        runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(nameIndex)
                }
            }
        }
        return name
    }

    fun openFileWithExternalApp(file: File, mimeType: String) {
        runCatching {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open file with"))
        }.onFailure {
            errorMessage = "No app found to open this file"
        }
    }

    fun shareFile(file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, title))
    }

    // Multi-PDF picker for merging
    val multiPdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedPdfUris = selectedPdfUris + uris
            errorMessage = null
        }
    }

    // Single-PDF picker for split and PDF->Image
    val singlePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            singlePdfUri = uri
            singlePdfName = getFileName(uri)
            val count = engine.getPdfPageCount(uri)
            singlePdfPageCount = count
            selectedPages = (0 until count).toList() // select all by default
            customRangeText = if (count > 0) "1-$count" else ""
            outputPdf = null
            splitPdfFile = null
            outputImages = emptyList()
            errorMessage = null
        }
    }

    fun updatePagesFromRangeText(text: String, maxPages: Int) {
        customRangeText = text
        val pages = mutableListOf<Int>()
        val parts = text.split(",")
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.contains("-")) {
                val bounds = trimmed.split("-")
                val start = bounds.getOrNull(0)?.trim()?.toIntOrNull()
                val end = bounds.getOrNull(1)?.trim()?.toIntOrNull()
                if (start != null && end != null) {
                    val s = (start - 1).coerceIn(0, maxPages - 1)
                    val e = (end - 1).coerceIn(0, maxPages - 1)
                    val range = if (s <= e) s..e else s downTo e
                    for (p in range) {
                        pages.add(p)
                    }
                }
            } else {
                trimmed.toIntOrNull()?.let { num ->
                    val zeroBased = num - 1
                    if (zeroBased in 0 until maxPages) {
                        pages.add(zeroBased)
                    }
                }
            }
        }
        selectedPages = pages
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("PDF Toolbox", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Mode Selectors
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PdfToolMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) DockObsidian else Color.White)
                                .border(1.dp, if (isSelected) Color.Transparent else BorderSubtle, RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedMode = mode
                                    outputPdf = null
                                    splitPdfFile = null
                                    outputImages = emptyList()
                                    errorMessage = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            errorMessage?.let { msg ->
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoPinkLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(msg, color = Color.Red, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(14.dp))
                    }
                }
            }

            when (selectedMode) {
                PdfToolMode.MERGE -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Merge Multiple PDFs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("Combine multiple PDF documents into a single organized file.", fontSize = 12.sp, color = TextSecondary)

                                Button(
                                    onClick = { multiPdfPicker.launch(arrayOf("application/pdf")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoHoney),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TextPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text("+ Select PDF Documents", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                if (selectedPdfUris.isNotEmpty()) {
                                    Text("${selectedPdfUris.size} documents in merge queue:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    selectedPdfUris.forEachIndexed { i, uri ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BentoHoneyLight)
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                                Text("${i + 1}. ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text(getFileName(uri), fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedPdfUris = selectedPdfUris.toMutableList().also { it.removeAt(i) }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            isProcessing = true
                                            coroutineScope.launch {
                                                val outFile = File(context.cacheDir, "ONE_merged_${System.currentTimeMillis()}.pdf")
                                                val res = engine.mergePdfs(selectedPdfUris, outFile) { current, total ->
                                                    progressStatus = "Merging doc $current of $total..."
                                                }
                                                isProcessing = false
                                                res.onSuccess { outputPdf = it }
                                                    .onFailure { errorMessage = "Failed to merge PDFs: ${it.message}" }
                                            }
                                        },
                                        enabled = !isProcessing && selectedPdfUris.size >= 2,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                                    ) {
                                        Text(
                                            if (isProcessing) progressStatus else if (selectedPdfUris.size < 2) "Add at least 2 PDFs" else "Merge ${selectedPdfUris.size} PDFs",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                PdfToolMode.PDF_TO_IMAGES -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Convert PDF Pages to Images", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("Extract each page of your PDF into crisp, standalone high-res images.", fontSize = 12.sp, color = TextSecondary)

                                Button(
                                    onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoSky),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TextPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (singlePdfUri == null) "Select PDF Document" else "Change PDF", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                singlePdfUri?.let { uri ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = BentoSkyLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(singlePdfName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("$singlePdfPageCount pages detected", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                    }

                                    // Format selector
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Output format:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        FilterChip(
                                            selected = !isJpgFormat,
                                            onClick = { isJpgFormat = false },
                                            label = { Text("PNG (Lossless)") },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        FilterChip(
                                            selected = isJpgFormat,
                                            onClick = { isJpgFormat = true },
                                            label = { Text("JPEG") },
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            isProcessing = true
                                            coroutineScope.launch {
                                                val outDir = File(context.cacheDir, "pdf_images_${System.currentTimeMillis()}").apply { mkdirs() }
                                                val format = if (isJpgFormat) android.graphics.Bitmap.CompressFormat.JPEG else android.graphics.Bitmap.CompressFormat.PNG
                                                val res = engine.pdfToImages(uri, outDir, format = format) { current, total ->
                                                    progressStatus = "Rendering page $current of $total..."
                                                }
                                                isProcessing = false
                                                res.onSuccess { outputImages = it }
                                                    .onFailure { errorMessage = "Failed to extract images: ${it.message}" }
                                            }
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                                    ) {
                                        Text(if (isProcessing) progressStatus else "Extract $singlePdfPageCount Pages to Images", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                PdfToolMode.SPLIT -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Split & Extract PDF Pages", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("Choose specific pages to separate and compile into a brand-new PDF document.", fontSize = 12.sp, color = TextSecondary)

                                Button(
                                    onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPink),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TextPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (singlePdfUri == null) "Select PDF to Split" else "Change PDF", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                singlePdfUri?.let { uri ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = BentoPinkLight,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(singlePdfName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Total $singlePdfPageCount pages • ${selectedPages.size} selected", fontSize = 12.sp, color = TextSecondary)
                                            }
                                        }
                                    }

                                    // Quick Select buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        TextButton(onClick = {
                                            selectedPages = (0 until singlePdfPageCount).toList()
                                            customRangeText = "1-$singlePdfPageCount"
                                        }) {
                                            Text("Select All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        TextButton(onClick = {
                                            selectedPages = emptyList()
                                            customRangeText = ""
                                        }) {
                                            Text("Clear", fontSize = 12.sp, color = TextSecondary)
                                        }
                                    }

                                    // Visual Page Chips
                                    if (singlePdfPageCount > 0) {
                                        Text("Tap pages to include:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(singlePdfPageCount) { idx ->
                                                val isPageSelected = selectedPages.contains(idx)
                                                FilterChip(
                                                    selected = isPageSelected,
                                                    onClick = {
                                                        selectedPages = if (isPageSelected) {
                                                            selectedPages.filter { it != idx }
                                                        } else {
                                                            selectedPages + idx
                                                        }
                                                        customRangeText = selectedPages.map { it + 1 }.joinToString(",")
                                                    },
                                                    label = { Text("P. ${idx + 1}", fontSize = 11.sp) },
                                                    leadingIcon = if (isPageSelected) {
                                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                                    } else null,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = customRangeText,
                                        onValueChange = { updatePagesFromRangeText(it, singlePdfPageCount) },
                                        label = { Text("Or specify range (e.g. 1-3, 5)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            if (selectedPages.isEmpty()) {
                                                errorMessage = "Please select at least 1 page to extract"
                                                return@Button
                                            }
                                            isProcessing = true
                                            errorMessage = null
                                            coroutineScope.launch {
                                                val outFile = File(context.cacheDir, "ONE_split_${System.currentTimeMillis()}.pdf")
                                                val res = engine.splitPdf(uri, selectedPages, outFile)
                                                isProcessing = false
                                                res.onSuccess { splitPdfFile = it }
                                                    .onFailure { errorMessage = "Failed to split PDF: ${it.message}" }
                                            }
                                        },
                                        enabled = !isProcessing && selectedPages.isNotEmpty(),
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                                    ) {
                                        Text(
                                            if (isProcessing) "Splitting..." else "Extract ${selectedPages.size} Pages into New PDF",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Merged Output Result Card
            outputPdf?.let { pdfFile ->
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoHoneyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Merged PDF Ready!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("${pdfFile.name} • ${pdfFile.length() / 1024} KB", fontSize = 12.sp, color = TextSecondary)
                                }
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = DockObsidian)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { openFileWithExternalApp(pdfFile, "application/pdf") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open PDF")
                                }

                                Button(
                                    onClick = { shareFile(pdfFile, "application/pdf", "Share Merged PDF") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
            }

            // Split PDF Output Result Card
            splitPdfFile?.let { pdfFile ->
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoPinkLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Split PDF Created!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("${pdfFile.name} • ${pdfFile.length() / 1024} KB", fontSize = 12.sp, color = TextSecondary)
                                }
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = DockObsidian)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { openFileWithExternalApp(pdfFile, "application/pdf") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Open PDF")
                                }

                                Button(
                                    onClick = { shareFile(pdfFile, "application/pdf", "Share Split PDF") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share")
                                }
                            }
                        }
                    }
                }
            }

            // PDF to Images Gallery
            if (outputImages.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoSkyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Extracted ${outputImages.size} pages successfully!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text("Saved locally to cache", fontSize = 12.sp, color = TextSecondary)
                                }
                                Button(
                                    onClick = {
                                        val uris = ArrayList(outputImages.map {
                                            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", it)
                                        })
                                        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                                            type = if (isJpgFormat) "image/jpeg" else "image/png"
                                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share All Pages"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share All")
                                }
                            }

                            // Preview grid/row of rendered images
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                itemsIndexed(outputImages) { idx, imgFile ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.White,
                                        modifier = Modifier
                                            .width(130.dp)
                                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(CanvasBackground)
                                            ) {
                                                AsyncImage(
                                                    model = imgFile,
                                                    contentDescription = "Page ${idx + 1}",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(4.dp)
                                                        .background(DockObsidian.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("P. ${idx + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Button(
                                                onClick = { shareFile(imgFile, if (isJpgFormat) "image/jpeg" else "image/png", "Share Page ${idx + 1}") },
                                                colors = ButtonDefaults.buttonColors(containerColor = BentoSky),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.fillMaxWidth().height(32.dp)
                                            ) {
                                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp), tint = TextPrimary)
                                                Spacer(Modifier.width(4.dp))
                                                Text("Share", fontSize = 11.sp, color = TextPrimary)
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
    }
}
