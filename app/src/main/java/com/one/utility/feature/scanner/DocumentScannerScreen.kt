package com.one.utility.feature.scanner

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.DocumentFilterMode
import com.one.utility.core.processing.DocumentScannerEngine
import com.one.utility.core.processing.ImageToPdfEngine
import com.one.utility.core.processing.PdfOptions
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScannerScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scannerEngine = remember { DocumentScannerEngine() }
    val pdfEngine = remember { ImageToPdfEngine(context) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var rawBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var filteredBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedFilter by remember { mutableStateOf(DocumentFilterMode.BW_DOCUMENT) }
    var isProcessing by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            val stream = context.contentResolver.openInputStream(uri)
            val bmp = BitmapFactory.decodeStream(stream)
            rawBitmap = bmp
            coroutineScope.launch {
                filteredBitmap = scannerEngine.applyDocumentFilter(bmp, selectedFilter)
            }
        }
    }

    fun applyFilter(filter: DocumentFilterMode) {
        selectedFilter = filter
        val src = rawBitmap ?: return
        coroutineScope.launch {
            filteredBitmap = scannerEngine.applyDocumentFilter(src, filter)
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Document Scanner", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Preview Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppTheme.colors.cardSurface)
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (filteredBitmap != null) {
                        Image(
                            bitmap = filteredBitmap!!.asImageBitmap(),
                            contentDescription = "Document Scan Preview",
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BentoHoney, modifier = Modifier.size(48.dp))
                            Text("Capture or Select Document", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                            Text("Transforms paper photos into clean scans", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }
            }

            if (rawBitmap != null) {
                // 2. Filter Selector Chips
                item {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Document Mode", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DocumentFilterMode.values().forEach { mode ->
                                val isSelected = selectedFilter == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { applyFilter(mode) },
                                    label = { Text(mode.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = AppTheme.colors.cardSurface,
                                        labelColor = AppTheme.colors.textSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Export Buttons (Save as Scanned PDF or Scanned Image)
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val bmp = filteredBitmap ?: return@Button
                                isProcessing = true
                                coroutineScope.launch {
                                    val tempImage = File(context.cacheDir, "scan_temp_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(tempImage).use { out ->
                                        bmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                                    }
                                    val pdfFile = File(context.cacheDir, "ONE_scan_${System.currentTimeMillis()}.pdf")
                                    pdfEngine.convertImagesToPdf(
                                        imageUris = listOf(Uri.fromFile(tempImage)),
                                        outputFile = pdfFile,
                                        options = PdfOptions(fitPage = true)
                                    )
                                    isProcessing = false
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Scanned PDF"))
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isProcessing) "Exporting..." else "Save as PDF", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val bmp = filteredBitmap ?: return@Button
                                val file = File(context.cacheDir, "ONE_scanned_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { out ->
                                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
                                }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Scanned Image"))
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = accentButtonColors(BentoHoney)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Image", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
