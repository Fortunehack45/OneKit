package com.one.utility.feature.cropper

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateRight
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
import com.one.utility.core.processing.CropAspectRatio
import com.one.utility.core.processing.ImageCropperEngine
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { ImageCropperEngine() }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var displayedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var currentRotation by remember { mutableFloatStateOf(0f) }
    var isFlippedH by remember { mutableStateOf(false) }
    var selectedRatio by remember { mutableStateOf(CropAspectRatio.FREE) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            val stream = context.contentResolver.openInputStream(uri)
            val bmp = BitmapFactory.decodeStream(stream)
            originalBitmap = bmp
            displayedBitmap = bmp
            currentRotation = 0f
            isFlippedH = false
            selectedRatio = CropAspectRatio.FREE
        }
    }

    fun applyTransforms() {
        val src = originalBitmap ?: return
        coroutineScope.launch {
            displayedBitmap = engine.transformBitmap(
                source = src,
                rotationDegrees = currentRotation,
                flipHorizontal = isFlippedH,
                aspectRatio = selectedRatio
            )
        }
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Crop & Rotate", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Image Preview Box
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayedBitmap != null) {
                        Image(
                            bitmap = displayedBitmap!!.asImageBitmap(),
                            contentDescription = "Preview",
                            modifier = Modifier.fillMaxSize().padding(12.dp)
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BentoHoney, modifier = Modifier.size(48.dp))
                            Text("Select an Image", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                            Text("Tap to pick from gallery", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            if (originalBitmap != null) {
                // 2. Aspect Ratio Chips
                item {
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Aspect Ratio", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CropAspectRatio.values().size) { idx ->
                                val ratio = CropAspectRatio.values()[idx]
                                val isSelected = selectedRatio == ratio
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        selectedRatio = ratio
                                        applyTransforms()
                                    },
                                    label = { Text(ratio.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DockObsidian,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Transformation Buttons (Rotate, Flip)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                currentRotation = (currentRotation + 90f) % 360f
                                applyTransforms()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Rotate 90°")
                        }

                        Button(
                            onClick = {
                                isFlippedH = !isFlippedH
                                applyTransforms()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoSky)
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Flip Horizontal", color = TextPrimary)
                        }
                    }
                }

                // 4. Save / Share Button
                item {
                    Button(
                        onClick = {
                            val bmp = displayedBitmap ?: return@Button
                            coroutineScope.launch {
                                val outFile = File(context.cacheDir, "ONE_cropped_${System.currentTimeMillis()}.jpg")
                                engine.saveBitmapToFile(bmp, outFile)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Cropped Image"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoHoney)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export & Share Cropped Photo", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}
