package com.one.utility.feature.pdf

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { PdfToolboxEngine(context) }

    var selectedMode by remember { mutableStateOf(PdfToolMode.MERGE) }
    var selectedPdfUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var singlePdfUri by remember { mutableStateOf<Uri?>(null) }

    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var outputPdf by remember { mutableStateOf<File?>(null) }
    var outputImages by remember { mutableStateOf<List<File>>(emptyList()) }

    // Multi-PDF picker for merging
    val multiPdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) selectedPdfUris = selectedPdfUris + uris
    }

    // Single-PDF picker for split and PDF->Image
    val singlePdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) singlePdfUri = uri
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    outputImages = emptyList()
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

            when (selectedMode) {
                PdfToolMode.MERGE -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Text("Select PDFs to Merge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

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
                                    Text("${selectedPdfUris.size} documents queued:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                                            Text("Document ${i + 1}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                                                val res = engine.mergePdfs(selectedPdfUris, outFile)
                                                isProcessing = false
                                                res.onSuccess { outputPdf = it }
                                            }
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                                    ) {
                                        Text(if (isProcessing) "Merging..." else "Merge ${selectedPdfUris.size} PDFs", fontWeight = FontWeight.Bold)
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
                                Text("Convert PDF Pages into Images", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                                Button(
                                    onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoSky),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TextPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (singlePdfUri == null) "Select PDF" else "Change PDF", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }

                                singlePdfUri?.let { uri ->
                                    Button(
                                        onClick = {
                                            isProcessing = true
                                            coroutineScope.launch {
                                                val outDir = File(context.cacheDir, "pdf_images_${System.currentTimeMillis()}").apply { mkdirs() }
                                                val res = engine.pdfToImages(uri, outDir)
                                                isProcessing = false
                                                res.onSuccess { outputImages = it }
                                            }
                                        },
                                        enabled = !isProcessing,
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                                    ) {
                                        Text(if (isProcessing) "Rendering Pages..." else "Extract Pages to Images", fontWeight = FontWeight.Bold)
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
                                Text("Split PDF Document", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                Text("Extract specific pages into a new standalone PDF.", fontSize = 13.sp, color = TextSecondary)

                                Button(
                                    onClick = { singlePdfPicker.launch(arrayOf("application/pdf")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = BentoPink),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = TextPrimary)
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (singlePdfUri == null) "Choose PDF to Split" else "PDF Selected", fontWeight = FontWeight.Bold, color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // Output Result Card
            outputPdf?.let { pdfFile ->
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoHoneyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Merged PDF Ready!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                Text("${pdfFile.name} • ${pdfFile.length() / 1024} KB", fontSize = 12.sp, color = TextSecondary)
                            }
                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Merged PDF"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            if (outputImages.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(BentoSkyLight)
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Extracted ${outputImages.size} pages successfully!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                            Text("Saved locally to cache folder.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}
