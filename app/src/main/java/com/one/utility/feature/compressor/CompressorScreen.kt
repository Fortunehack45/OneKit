package com.one.utility.feature.compressor

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
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
import coil.compose.AsyncImage
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.CompressionPreset
import com.one.utility.core.processing.CompressionResult
import com.one.utility.core.processing.ImageCompressorEngine
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompressorScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { ImageCompressorEngine(context) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPreset by remember { mutableStateOf(CompressionPreset.HIGH) }
    var customQuality by remember { mutableFloatStateOf(75f) }
    var useCustomQuality by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var resultData by remember { mutableStateOf<CompressionResult?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            resultData = null
        }
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Image Compressor", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. Image Selector Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = BentoSky,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Select an Image to Compress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("Tap to browse gallery", fontSize = 12.sp, color = TextSecondary)
                        }
                    } else {
                        AsyncImage(
                            model = selectedUri,
                            contentDescription = "Selected",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. Preset & Quality Controls
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Compression Level", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                        CompressionPreset.values().forEach { preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (!useCustomQuality && selectedPreset == preset) BentoSkyLight else Color.Transparent)
                                    .clickable {
                                        useCustomQuality = false
                                        selectedPreset = preset
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(preset.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Text("Quality: ${preset.quality}% • Max: ${preset.maxDimension}px", fontSize = 12.sp, color = TextSecondary)
                                }
                                RadioButton(
                                    selected = !useCustomQuality && selectedPreset == preset,
                                    onClick = {
                                        useCustomQuality = false
                                        selectedPreset = preset
                                    }
                                )
                            }
                        }

                        Divider(color = BorderSubtle)

                        // Custom Quality Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Custom Quality Slider", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPrimary)
                            Switch(checked = useCustomQuality, onCheckedChange = { useCustomQuality = it })
                        }

                        if (useCustomQuality) {
                            Column {
                                Text("Quality: ${customQuality.toInt()}%", fontSize = 13.sp, color = TextSecondary)
                                Slider(
                                    value = customQuality,
                                    onValueChange = { customQuality = it },
                                    valueRange = 10f..100f,
                                    steps = 18
                                )
                            }
                        }
                    }
                }
            }

            // 3. Results Preview Card: Before vs After
            item {
                resultData?.let { res ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = BentoSkyLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = DockObsidian)
                                Text("Compression Complete!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Original", fontSize = 12.sp, color = TextSecondary)
                                    Text("${res.originalSizeBytes / 1024} KB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                }
                                Column {
                                    Text("Compressed", fontSize = 12.sp, color = TextSecondary)
                                    Text("${res.compressedSizeBytes / 1024} KB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                                }
                                Column {
                                    Text("Space Saved", fontSize = 12.sp, color = TextSecondary)
                                    Text(
                                        "${"%.1f".format(res.savedPercentage)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = DockObsidian
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        res.outputFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Compressed Image"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DockObsidian),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Share Compressed Image")
                            }
                        }
                    }
                }
            }

            // 4. Action Button
            item {
                Button(
                    onClick = {
                        val uri = selectedUri ?: return@Button
                        isProcessing = true
                        errorMessage = null
                        coroutineScope.launch {
                            val output = File(context.cacheDir, "ONE_compressed_${System.currentTimeMillis()}.jpg")
                            val result = engine.compressImage(
                                inputUri = uri,
                                outputFile = output,
                                preset = selectedPreset,
                                customQuality = if (useCustomQuality) customQuality.toInt() else null,
                                onProgress = { progress = it }
                            )
                            isProcessing = false
                            result.onSuccess { resultData = it }
                                .onFailure { errorMessage = it.localizedMessage }
                        }
                    },
                    enabled = selectedUri != null && !isProcessing,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                ) {
                    Text(
                        text = if (isProcessing) "Compressing..." else "Compress Image",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
