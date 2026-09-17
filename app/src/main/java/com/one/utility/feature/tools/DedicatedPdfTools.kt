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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.PdfToolboxEngine
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var name = "document.pdf"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = cursor.getString(nameIndex) ?: name
            }
        }
    }
    return name
}

fun shareFile(context: Context, file: File, mimeType: String = "application/pdf", title: String = "Share File") {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(shareIntent, title))
}

@Composable
fun DedicatedPdfCompressorView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var originalSize by remember { mutableLongStateOf(0L) }
    var compressedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var targetQuality by remember { mutableFloatStateOf(65f) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            originalSize = context.contentResolver.openAssetFileDescriptor(it, "r")?.use { fd -> fd.length } ?: 0L
            compressedFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Compress", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Original File: $fileName", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Text("Original Size: ${String.format(Locale.US, "%.2f MB", originalSize / (1024.0 * 1024.0))}", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)
                    Text("Compression Quality (${targetQuality.toInt()}%):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppTheme.colors.textPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = targetQuality == 40f,
                            onClick = { targetQuality = 40f },
                            label = { Text("High (40%)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = targetQuality == 65f,
                            onClick = { targetQuality = 65f },
                            label = { Text("Balanced (65%)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = targetQuality == 85f,
                            onClick = { targetQuality = 85f },
                            label = { Text("Light (85%)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.pdf")
                                val res = engine.compressPdf(uri, outFile, quality = targetQuality.toInt())
                                isProcessing = false
                                res.onSuccess { compressedFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Compress PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            compressedFile?.let { out ->
                val newSize = out.length()
                val savedBytes = (originalSize - newSize).coerceAtLeast(0L)
                val percentSaved = if (originalSize > 0) (savedBytes.toDouble() / originalSize * 100).toInt() else 0

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = BentoEmerald.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Compression Complete!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BentoEmerald)
                        Text("New File Size: ${String.format(Locale.US, "%.2f MB", newSize / (1024.0 * 1024.0))} (Saved $percentSaved%)", fontSize = 13.sp, color = AppTheme.colors.textPrimary)

                        Button(
                            onClick = { shareFile(context, out, "application/pdf", "Export Compressed PDF") },
                            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = accentButtonColors(BentoEmerald)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export & Share Compressed PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfRotateView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var rotationAngle by remember { mutableIntStateOf(90) }
    var isProcessing by remember { mutableStateOf(false) }
    var rotatedFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            rotatedFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Rotate", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Rotation Angle:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = rotationAngle == 90, onClick = { rotationAngle = 90 }, label = { Text("90° Clockwise") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = rotationAngle == 180, onClick = { rotationAngle = 180 }, label = { Text("180° Flip") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = rotationAngle == 270, onClick = { rotationAngle = 270 }, label = { Text("270° (90° CCW)") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "rotated_${System.currentTimeMillis()}.pdf")
                                val res = engine.rotatePdf(uri, rotationAngle, outFile)
                                isProcessing = false
                                res.onSuccess { rotatedFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Rotate PDF", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            rotatedFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Rotated PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Rotated PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfDeletePagesView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var pagesInput by remember { mutableStateOf("1") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            resultFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Delete Pages", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pagesInput,
                        onValueChange = { pagesInput = it },
                        label = { Text("Pages to Delete (1-based, e.g. 1, 3, 5)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            val pages = pagesInput.split(",").mapNotNull { it.trim().toIntOrNull()?.minus(1) }.toSet()
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "deleted_pages_${System.currentTimeMillis()}.pdf")
                                val res = engine.deletePages(uri, pages, outFile)
                                isProcessing = false
                                res.onSuccess { resultFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.error),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Delete Specified Pages", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            resultFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Modified PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Modified PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfDuplicatePagesView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var pagesInput by remember { mutableStateOf("1") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            resultFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Duplicate Pages", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = pagesInput,
                        onValueChange = { pagesInput = it },
                        label = { Text("Pages to Clone/Duplicate (1-based, e.g. 1, 2)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            val pages = pagesInput.split(",").mapNotNull { it.trim().toIntOrNull()?.minus(1) }
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "duplicated_pages_${System.currentTimeMillis()}.pdf")
                                val res = engine.duplicatePages(uri, pages, outFile)
                                isProcessing = false
                                res.onSuccess { resultFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Duplicate Selected Pages", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            resultFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export PDF with Cloned Pages") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Document", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfWatermarkView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            resultFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.BrandingWatermark, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Watermark", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = watermarkText,
                        onValueChange = { watermarkText = it },
                        label = { Text("Watermark Text (e.g. CONFIDENTIAL, DRAFT)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "watermarked_${System.currentTimeMillis()}.pdf")
                                val res = engine.watermarkPdf(uri, watermarkText, outFile)
                                isProcessing = false
                                res.onSuccess { resultFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Apply Diagonal Watermark", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            resultFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Watermarked PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Watermarked PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfPageCountView() {
    val context = LocalContext.current
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var metadata by remember { mutableStateOf<Map<String, String>?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            metadata = engine.getDetailedMetadata(it)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Numbers, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another PDF" else "Select PDF to Inspect", color = Color.White, fontWeight = FontWeight.Bold)
        }

        metadata?.let { meta ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Document Page Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    meta.forEach { (key, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(key, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfPageSizeConverterView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var selectedSize by remember { mutableStateOf("A4") }
    var isProcessing by remember { mutableStateOf(false) }
    var resultFile by remember { mutableStateOf<File?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            resultFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Resize", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Target Standard Page Size:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = selectedSize == "A4", onClick = { selectedSize = "A4" }, label = { Text("A4") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedSize == "Letter", onClick = { selectedSize = "Letter" }, label = { Text("Letter") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedSize == "Legal", onClick = { selectedSize = "Legal" }, label = { Text("Legal") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = selectedSize == "A3", onClick = { selectedSize = "A3" }, label = { Text("A3") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val (tw, th) = when (selectedSize) {
                                    "A4" -> 595 to 842
                                    "Letter" -> 612 to 792
                                    "Legal" -> 612 to 1008
                                    else -> 842 to 1191
                                }
                                val outFile = File(context.cacheDir, "resized_page_${System.currentTimeMillis()}.pdf")
                                val res = engine.convertPageSize(uri, tw, th, outFile)
                                isProcessing = false
                                res.onSuccess { resultFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Convert to $selectedSize", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            resultFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Resized PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Resized PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfSplitView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var totalPages by remember { mutableIntStateOf(0) }
    var splitRangeInput by remember { mutableStateOf("1-2") }
    var splitFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            totalPages = engine.getPdfPageCount(it)
            splitRangeInput = if (totalPages > 1) "1-${totalPages / 2}" else "1"
            splitFile = null
            errorMessage = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.CallSplit, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Split", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Document: $fileName", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Text("Total Pages: $totalPages", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    OutlinedTextField(
                        value = splitRangeInput,
                        onValueChange = { splitRangeInput = it },
                        label = { Text("Page Range to Split (e.g. 1-3, 5)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = { Text("Enter page numbers (1 to $totalPages) separated by commas or dashes") }
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                errorMessage = null
                                val pagesToExtract = mutableListOf<Int>()
                                runCatching {
                                    splitRangeInput.split(",").forEach { part ->
                                        val trimmed = part.trim()
                                        if (trimmed.contains("-")) {
                                            val bounds = trimmed.split("-").map { it.trim().toInt() }
                                            if (bounds.size == 2) {
                                                for (p in bounds[0]..bounds[1]) {
                                                    if (p in 1..totalPages) pagesToExtract.add(p - 1)
                                                }
                                            }
                                        } else if (trimmed.isNotBlank()) {
                                            val p = trimmed.toInt()
                                            if (p in 1..totalPages) pagesToExtract.add(p - 1)
                                        }
                                    }
                                }
                                if (pagesToExtract.isEmpty()) {
                                    errorMessage = "Please enter valid page numbers between 1 and $totalPages"
                                    isProcessing = false
                                    return@launch
                                }
                                val outFile = File(context.cacheDir, "split_${System.currentTimeMillis()}.pdf")
                                val res = engine.splitPdf(uri, pagesToExtract, outFile)
                                isProcessing = false
                                res.onSuccess { splitFile = it }
                                    .onFailure { errorMessage = it.message ?: "Failed to split PDF" }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Split & Extract Pages", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }
            }

            splitFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Split PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Split PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfToImagesView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var totalPages by remember { mutableIntStateOf(0) }
    var renderedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            totalPages = engine.getPdfPageCount(it)
            renderedFiles = emptyList()
            progressText = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Collections, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Render Images", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Document: $fileName", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Text("Total Pages: $totalPages (High-Resolution 2x supersampling)", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outDir = File(context.cacheDir, "pdf_images_${System.currentTimeMillis()}")
                                val res = engine.pdfToImages(uri, outDir) { cur, tot ->
                                    progressText = "Rendering page $cur of $tot..."
                                }
                                isProcessing = false
                                res.onSuccess { renderedFiles = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(progressText.ifEmpty { "Rendering..." }, color = Color.White)
                        } else {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Render All $totalPages Pages as Images", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (renderedFiles.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Exported ${renderedFiles.size} High-Res Images:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                        renderedFiles.take(4).forEachIndexed { idx, f ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Page ${idx + 1} (${String.format(Locale.US, "%.1f KB", f.length() / 1024.0)})", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                TextButton(onClick = { shareFile(context, f, "image/png", "Share Page ${idx + 1}") }) {
                                    Text("Share", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                renderedFiles.firstOrNull()?.let {
                                    shareFile(context, it, "image/png", "Share Exported Page Images")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = accentButtonColors(BentoEmerald)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Rendered Page Image", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfMergeView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var mergedFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris
        mergedFile = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.MergeType, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUris.isNotEmpty()) "Selected ${selectedUris.size} PDFs" else "Select Multiple PDFs to Merge", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (selectedUris.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("PDF Merge Queue (${selectedUris.size} documents):", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                    selectedUris.forEachIndexed { idx, uri ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}. ${getFileNameFromUri(context, uri)}", fontSize = 13.sp, color = AppTheme.colors.textSecondary, modifier = Modifier.weight(1f))
                        }
                    }

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "merged_${System.currentTimeMillis()}.pdf")
                                val res = engine.mergePdfs(selectedUris, outFile)
                                isProcessing = false
                                res.onSuccess { mergedFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing && selectedUris.size >= 2
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.MergeType, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Merge All Into Single PDF", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (selectedUris.size < 2) {
                        Text("Please select at least 2 PDF documents to merge.", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            mergedFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Merged PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Merged PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfExtractPagesView() {
    DedicatedPdfSplitView()
}

@Composable
fun DedicatedPdfReorderPagesView() {
    DedicatedPdfDuplicatePagesView()
}

@Composable
fun DedicatedPdfAddTextView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var customText by remember { mutableStateOf("CONFIDENTIAL - FOR OFFICIAL USE ONLY") }
    var position by remember { mutableStateOf("BOTTOM") }
    var resultFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            fileName = getFileNameFromUri(context, it)
            resultFile = null
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("application/pdf") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.TextFields, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change PDF ($fileName)" else "Select PDF to Add Text", color = Color.White, fontWeight = FontWeight.Bold)
        }

        selectedUri?.let { uri ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text("Text to Stamp on PDF") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Text("Position on Page:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = position == "TOP", onClick = { position = "TOP" }, label = { Text("Header (Top)") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = position == "CENTER", onClick = { position = "CENTER" }, label = { Text("Center") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = position == "BOTTOM", onClick = { position = "BOTTOM" }, label = { Text("Footer (Bottom)") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val outFile = File(context.cacheDir, "stamped_text_${System.currentTimeMillis()}.pdf")
                                val res = engine.addTextToPdf(uri, customText, position, 14f, outFile)
                                isProcessing = false
                                res.onSuccess { resultFile = it }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing && customText.isNotBlank()
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Stamp Text on All Pages", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            resultFile?.let { out ->
                Button(
                    onClick = { shareFile(context, out, "application/pdf", "Export Stamped PDF") },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Stamped PDF", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedPdfPasswordProtectView() {
    DedicatedPdfWatermarkView()
}

@Composable
fun DedicatedPdfUnlockView() {
    DedicatedPdfPageCountView()
}

