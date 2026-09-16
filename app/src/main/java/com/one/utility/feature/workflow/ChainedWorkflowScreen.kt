package com.one.utility.feature.workflow

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowForward
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
import com.one.utility.core.processing.CompressionPreset
import com.one.utility.core.processing.ImageCompressorEngine
import com.one.utility.core.processing.ImageToPdfEngine
import com.one.utility.core.processing.PdfOptions
import com.one.utility.core.workflow.WorkflowEngine
import com.one.utility.core.workflow.WorkflowStep
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChainedWorkflowScreen(
    stepNames: List<String>,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val compressor = remember { ImageCompressorEngine(context) }
    val pdfEngine = remember { ImageToPdfEngine(context) }

    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressStatus by remember { mutableStateOf("") }
    var finalPdfFile by remember { mutableStateOf<File?>(null) }

    val steps = remember {
        WorkflowEngine().createChainedWorkflow(stepNames)
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = uris
            currentStepIndex = 1
        }
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Smart Pipeline: Compress → PDF", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // Workflow Step Indicators
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    steps.forEachIndexed { index, step ->
                        val isActive = index == currentStepIndex
                        val isDone = index < currentStepIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isDone -> DockObsidian
                                        isActive -> BentoHoney
                                        else -> BorderSubtle
                                    }
                                )
                        )
                    }
                }
            }

            // Step Content
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = steps.getOrNull(currentStepIndex)?.name ?: "Workflow",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = steps.getOrNull(currentStepIndex)?.description ?: "",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )

                        if (currentStepIndex == 0) {
                            Button(
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Choose Photos", fontWeight = FontWeight.Bold)
                            }
                        } else if (currentStepIndex == 1) {
                            Text("${selectedUris.size} photos ready for auto-compression and PDF assembly.", fontSize = 13.sp, color = TextPrimary)

                            Button(
                                onClick = {
                                    isProcessing = true
                                    coroutineScope.launch {
                                        // 1. Compress
                                        progressStatus = "Compressing photos..."
                                        val compressedFiles = mutableListOf<File>()
                                        selectedUris.forEachIndexed { i, uri ->
                                            val outFile = File(context.cacheDir, "temp_comp_$i.jpg")
                                            compressor.compressImage(uri, outFile, CompressionPreset.HIGH)
                                            compressedFiles.add(outFile)
                                        }

                                        // 2. Generate PDF
                                        progressStatus = "Creating optimized PDF..."
                                        val pdfFile = File(context.cacheDir, "ONE_chained_${System.currentTimeMillis()}.pdf")
                                        val result = pdfEngine.convertImagesToPdf(
                                            imageUris = compressedFiles.map { Uri.fromFile(it) },
                                            outputFile = pdfFile,
                                            options = PdfOptions()
                                        )

                                        isProcessing = false
                                        result.onSuccess {
                                            finalPdfFile = it
                                            currentStepIndex = 2
                                        }
                                    }
                                },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoHoney)
                            ) {
                                Text(
                                    text = if (isProcessing) progressStatus else "Run Compress & PDF Pipeline",
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        } else if (currentStepIndex >= 2 && finalPdfFile != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(BentoHoneyLight)
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = DockObsidian)
                                        Text("Pipeline Complete!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                    }
                                    Text("Photos were compressed and compiled into ${finalPdfFile!!.name}", fontSize = 13.sp, color = TextSecondary)
                                }
                            }

                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        finalPdfFile!!
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Share Result PDF", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
