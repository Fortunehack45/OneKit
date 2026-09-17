package com.one.utility.feature.converter

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ConversionResult
import com.one.utility.core.processing.ImageConverterEngine
import com.one.utility.core.processing.ImageFormat
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageConverterScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { ImageConverterEngine(context) }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var targetFormat by remember { mutableStateOf(ImageFormat.WEBP) }
    var quality by remember { mutableFloatStateOf(90f) }

    var isProcessing by remember { mutableStateOf(false) }
    var resultData by remember { mutableStateOf<ConversionResult?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            resultData = null
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Image Format Converter", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // 1. Image Preview Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppTheme.colors.surfaceCard)
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedUri != null) {
                        AsyncImage(model = selectedUri, contentDescription = "Selected", modifier = Modifier.fillMaxSize())
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                            Text("Select an Image to Convert", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                            Text("Convert between JPG, PNG, and modern WEBP", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }
            }

            // 2. Target Format Selector
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.surfaceCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Target Format", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ImageFormat.values().forEach { format ->
                                val isSelected = targetFormat == format
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { targetFormat = format },
                                    label = { Text(format.extension.uppercase()) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (AppTheme.colors.isDark) Color.White else DockObsidian,
                                        selectedLabelColor = if (AppTheme.colors.isDark) Color(0xFF14151B) else Color.White,
                                        containerColor = AppTheme.colors.surfaceVariant,
                                        labelColor = AppTheme.colors.textPrimary
                                    )
                                )
                            }
                        }

                        if (targetFormat == ImageFormat.JPEG) {
                            Text("Note: Transparent areas will be filled with a solid white background.", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        } else if (targetFormat == ImageFormat.WEBP) {
                            Text("WEBP offers up to 30% smaller file sizes with transparency support.", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }

                        // Quality Slider
                        Column {
                            Text("Output Quality: ${quality.toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                            Slider(
                                value = quality,
                                onValueChange = { quality = it },
                                valueRange = 10f..100f,
                                steps = 18
                            )
                        }
                    }
                }
            }

            // 3. Result Preview & Share
            resultData?.let { res ->
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (AppTheme.colors.isDark) Color(0xFF1E293B) else BentoSkyLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Converted to ${res.format.extension.uppercase()}!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                Text("${res.outputFile.name} • ${res.sizeBytes / 1024} KB", fontSize = 12.sp, color = AppTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }

                            Spacer(Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", res.outputFile)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = res.format.mimeType
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Converted Image"))
                                },
                                colors = obsidianButtonColors(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold)
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
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_converted_${System.currentTimeMillis()}.${targetFormat.extension}")
                            val res = engine.convertImage(uri, targetFormat, quality.toInt(), outFile)
                            isProcessing = false
                            res.onSuccess { resultData = it }
                        }
                    },
                    enabled = selectedUri != null && !isProcessing,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = obsidianButtonColors()
                ) {
                    Text(
                        text = if (isProcessing) "Converting..." else "Convert to ${targetFormat.extension.uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
