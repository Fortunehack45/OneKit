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
import androidx.compose.material.icons.filled.CameraAlt
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
import com.one.utility.core.designsystem.components.MediaPickerModalSheet
import com.one.utility.core.designsystem.components.createTempCameraUri
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

    var showSourceSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            selectedUri = tempCameraUri
            resultData = null
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            resultData = null
        }
    }

    if (showSourceSheet) {
        MediaPickerModalSheet(
            onDismissRequest = { showSourceSheet = false },
            onTakePhoto = {
                val uri = createTempCameraUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onChooseGallery = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        )
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Image Compressor", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Image Selector Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppTheme.colors.surfaceCard)
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .clickable { showSourceSheet = true },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Text("Take Photo or Choose Image", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                            Text("Camera capture & Gallery supported", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
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
                    color = AppTheme.colors.surfaceCard,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Compression Level", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                        CompressionPreset.values().forEach { preset ->
                            val isSelected = !useCustomQuality && selectedPreset == preset
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                                    .clickable {
                                        useCustomQuality = false
                                        selectedPreset = preset
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(preset.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                    Text("Quality: ${preset.quality}% • Max: ${preset.maxDimension}px", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        useCustomQuality = false
                                        selectedPreset = preset
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        HorizontalDivider(color = AppTheme.colors.borderSubtle)

                        // Custom Quality Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Custom Quality Slider", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(
                                checked = useCustomQuality,
                                onCheckedChange = { useCustomQuality = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        if (useCustomQuality) {
                            Column {
                                Text("Quality: ${customQuality.toInt()}%", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                                Slider(
                                    value = customQuality,
                                    onValueChange = { customQuality = it },
                                    valueRange = 10f..100f,
                                    steps = 18,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
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
                        color = AppTheme.colors.surfaceCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(
                                    Icons.Outlined.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "Compression Complete!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = AppTheme.colors.textPrimary
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Original", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    Text("${res.originalSizeBytes / 1024} KB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                }
                                Column {
                                    Text("Compressed", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    Text("${res.compressedSizeBytes / 1024} KB", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                                }
                                Column {
                                    Text("Space Saved", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    Text(
                                        "${"%.1f".format(res.savedPercentage)}%",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Share Compressed Image",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = AppTheme.colors.surfaceVariant,
                        disabledContentColor = AppTheme.colors.textTertiary
                    )
                ) {
                    Text(
                        text = if (isProcessing) "Compressing..." else "Compress Image",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedUri != null && !isProcessing) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textTertiary
                    )
                }
            }
        }
    }
}
