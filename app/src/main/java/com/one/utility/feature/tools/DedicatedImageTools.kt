package com.one.utility.feature.tools

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ImageCropperEngine
import com.one.utility.core.processing.ImageEffectsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                decoder.isMutableRequired = true
            }
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }.getOrNull()
}

@Composable
fun DedicatedImageResizerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resizedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var targetWidthInput by remember { mutableStateOf("1080") }
    var targetHeightInput by remember { mutableStateOf("1920") }
    var keepAspectRatio by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            resizedBitmap = bmp
            bmp?.let { b ->
                targetWidthInput = b.width.toString()
                targetHeightInput = b.height.toString()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.PhotoSizeSelectLarge, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Photo" else "Select Photo to Resize", color = Color.White, fontWeight = FontWeight.Bold)
        }

        originalBitmap?.let { orig ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                (resizedBitmap ?: orig).let { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Current Dimensions: ${orig.width} x ${orig.height} px", fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = targetWidthInput,
                            onValueChange = {
                                targetWidthInput = it
                                if (keepAspectRatio && orig.width > 0) {
                                    val w = it.toIntOrNull() ?: 0
                                    val h = (w.toDouble() * orig.height / orig.width).toInt()
                                    targetHeightInput = h.toString()
                                }
                            },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = targetHeightInput,
                            onValueChange = {
                                targetHeightInput = it
                                if (keepAspectRatio && orig.height > 0) {
                                    val h = it.toIntOrNull() ?: 0
                                    val w = (h.toDouble() * orig.width / orig.height).toInt()
                                    targetWidthInput = w.toString()
                                }
                            },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Checkbox(checked = keepAspectRatio, onCheckedChange = { keepAspectRatio = it })
                        Text("Lock Aspect Ratio", fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                targetWidthInput = (orig.width / 2).toString()
                                targetHeightInput = (orig.height / 2).toString()
                            },
                            label = { Text("50%") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                targetWidthInput = "1080"
                                targetHeightInput = if (orig.width > 0) (1080.0 * orig.height / orig.width).toInt().toString() else "1080"
                            },
                            label = { Text("1080p") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                targetWidthInput = "720"
                                targetHeightInput = if (orig.width > 0) (720.0 * orig.height / orig.width).toInt().toString() else "720"
                            },
                            label = { Text("720p") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            val w = targetWidthInput.toIntOrNull() ?: orig.width
                            val h = targetHeightInput.toIntOrNull() ?: orig.height
                            coroutineScope.launch {
                                isProcessing = true
                                val scaled = withContext(Dispatchers.Default) {
                                    Bitmap.createScaledBitmap(orig, w.coerceAtLeast(1), h.coerceAtLeast(1), true)
                                }
                                resizedBitmap = scaled
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Apply Resize", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            resizedBitmap?.let { bmp ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_resized_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                            }
                            shareFile(context, outFile, "image/jpeg", "Share Resized Image")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Resized Image", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedImageSharpenView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectsEngine = remember { ImageEffectsEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sharpenedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var intensity by remember { mutableFloatStateOf(1.2f) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            sharpenedBitmap = bmp
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Photo" else "Select Photo to Sharpen", color = Color.White, fontWeight = FontWeight.Bold)
        }

        originalBitmap?.let { orig ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                (sharpenedBitmap ?: orig).let { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Sharpened", modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sharpen Intensity", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        Text(String.format(Locale.US, "%.1fx", intensity), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        onValueChangeFinished = {
                            coroutineScope.launch {
                                isProcessing = true
                                sharpenedBitmap = effectsEngine.applySharpen(orig, intensity)
                                isProcessing = false
                            }
                        },
                        valueRange = 0.5f..3.0f
                    )
                }
            }

            sharpenedBitmap?.let { bmp ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_sharpened_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                            }
                            shareFile(context, outFile, "image/jpeg", "Share Sharpened Photo")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Sharpened Photo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedImageGrayscaleView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectsEngine = remember { ImageEffectsEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var grayscaleBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            bmp?.let { b ->
                coroutineScope.launch {
                    grayscaleBitmap = effectsEngine.applyGrayscale(b)
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.FilterBAndW, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Photo" else "Select Photo for Monochrome", color = Color.White, fontWeight = FontWeight.Bold)
        }

        grayscaleBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Grayscale", modifier = Modifier.fillMaxSize().padding(10.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_bw_${System.currentTimeMillis()}.jpg")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                        }
                        shareFile(context, outFile, "image/jpeg", "Share Black & White Photo")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Black & White Photo", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedImageBrightnessContrastView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectsEngine = remember { ImageEffectsEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1.0f) }

    fun updateImage() {
        val orig = originalBitmap ?: return
        coroutineScope.launch {
            val bAdjusted = effectsEngine.adjustBrightness(orig, brightness)
            previewBitmap = effectsEngine.adjustContrast(bAdjusted, contrast)
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            previewBitmap = bmp
            brightness = 0f
            contrast = 1.0f
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Photo" else "Select Photo to Adjust", color = Color.White, fontWeight = FontWeight.Bold)
        }

        originalBitmap?.let { orig ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                (previewBitmap ?: orig).let { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Adjusted", modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Brightness / Exposure", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        Text("${brightness.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = brightness,
                        onValueChange = { brightness = it },
                        onValueChangeFinished = { updateImage() },
                        valueRange = -100f..100f
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Contrast Dynamic Range", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        Text(String.format(Locale.US, "%.2fx", contrast), fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = contrast,
                        onValueChange = { contrast = it },
                        onValueChangeFinished = { updateImage() },
                        valueRange = 0.5f..2.0f
                    )
                }
            }

            previewBitmap?.let { bmp ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_adjusted_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                            }
                            shareFile(context, outFile, "image/jpeg", "Share Adjusted Photo")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Adjusted Photo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedBackgroundChangerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectsEngine = remember { ImageEffectsEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedColor by remember { mutableStateOf(Color.White) }

    val presetColors = listOf(
        Color.White,
        Color(0xFF1E293B), // Slate Dark
        Color(0xFF2563EB), // Studio Blue
        Color(0xFFDC2626), // Passport Red
        Color(0xFFF3F4F6), // Studio Off-White
        Color(0xFF10B981)  // Green
    )

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            bmp?.let { b ->
                coroutineScope.launch {
                    resultBitmap = effectsEngine.replaceBackgroundWithColor(b, selectedColor.toArgb())
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Change Cutout Photo" else "Select Cutout / Transparent Photo", color = Color.White, fontWeight = FontWeight.Bold)
        }

        originalBitmap?.let { orig ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                (resultBitmap ?: orig).let { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Preview", modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select Solid Studio Background:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        presetColors.forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (selectedColor == c) 3.dp else 1.dp,
                                        color = if (selectedColor == c) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColor = c
                                        coroutineScope.launch {
                                            resultBitmap = effectsEngine.replaceBackgroundWithColor(orig, c.toArgb())
                                        }
                                    }
                            )
                        }
                    }
                }
            }

            resultBitmap?.let { bmp ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_bg_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                            }
                            shareFile(context, outFile, "image/jpeg", "Share Photo with Background")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Final Photo", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedScreenshotCropperView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            croppedBitmap = bmp
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Screenshot" else "Select Screenshot to Crop", color = Color.White, fontWeight = FontWeight.Bold)
        }

        originalBitmap?.let { orig ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                (croppedBitmap ?: orig).let { bmp ->
                    Image(bitmap = bmp.asImageBitmap(), contentDescription = "Cropped Screenshot", modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1-Tap Rapid Screenshot Trimmers:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                val topOffset = (orig.height * 0.05f).toInt()
                                val newH = orig.height - topOffset
                                if (newH > 10) {
                                    croppedBitmap = Bitmap.createBitmap(orig, 0, topOffset, orig.width, newH)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Text("Trim Top Bar", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val btmOffset = (orig.height * 0.06f).toInt()
                                val newH = orig.height - btmOffset
                                if (newH > 10) {
                                    croppedBitmap = Bitmap.createBitmap(orig, 0, 0, orig.width, newH)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Text("Trim Nav Bar", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val topOffset = (orig.height * 0.05f).toInt()
                                val btmOffset = (orig.height * 0.06f).toInt()
                                val newH = orig.height - topOffset - btmOffset
                                if (newH > 10) {
                                    croppedBitmap = Bitmap.createBitmap(orig, 0, topOffset, orig.width, newH)
                                }
                            },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Trim Both", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            croppedBitmap?.let { bmp ->
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val outFile = File(context.cacheDir, "ONE_clean_screenshot_${System.currentTimeMillis()}.jpg")
                            withContext(Dispatchers.IO) {
                                FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 95, out) }
                            }
                            shareFile(context, outFile, "image/jpeg", "Share Trimmed Screenshot")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Trimmed Screenshot", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedBatchImageCompressorView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var quality by remember { mutableFloatStateOf(65f) }
    var compressedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris
        compressedFiles = emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Compress, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUris.isNotEmpty()) "Selected ${selectedUris.size} Images" else "Select Batch Images to Compress", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (selectedUris.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Batch Compression: ${selectedUris.size} Photos", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                    Text("Target Quality: ${quality.toInt()}%", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = quality,
                        onValueChange = { quality = it },
                        valueRange = 20f..95f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val files = mutableListOf<File>()
                                withContext(Dispatchers.IO) {
                                    selectedUris.forEachIndexed { idx, uri ->
                                        loadBitmapFromUri(context, uri)?.let { bmp ->
                                            val outFile = File(context.cacheDir, "batch_comp_${idx}_${System.currentTimeMillis()}.jpg")
                                            FileOutputStream(outFile).use { out ->
                                                bmp.compress(Bitmap.CompressFormat.JPEG, quality.toInt(), out)
                                            }
                                            files.add(outFile)
                                        }
                                    }
                                }
                                compressedFiles = files
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.Compress, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compress All ${selectedUris.size} Images", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (compressedFiles.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Compressed ${compressedFiles.size} Photos Successfully!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = BentoEmerald)
                        val totalKb = compressedFiles.sumOf { it.length() } / 1024.0
                        Text(String.format(Locale.US, "Total Compressed Size: %.2f MB", totalKb / 1024.0), fontSize = 13.sp, color = AppTheme.colors.textSecondary)

                        Button(
                            onClick = {
                                compressedFiles.firstOrNull()?.let {
                                    shareFile(context, it, "image/jpeg", "Share Batch Compressed Photo")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = accentButtonColors(BentoEmerald)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share Compressed Photo", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedBatchImageResizerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var scalePercent by remember { mutableFloatStateOf(50f) }
    var resizedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris
        resizedFiles = emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUris.isNotEmpty()) "Selected ${selectedUris.size} Images" else "Select Batch Images to Resize", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (selectedUris.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Scale Factor: ${scalePercent.toInt()}%", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                    Slider(
                        value = scalePercent,
                        onValueChange = { scalePercent = it },
                        valueRange = 25f..100f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary)
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val files = mutableListOf<File>()
                                val factor = scalePercent / 100f
                                withContext(Dispatchers.IO) {
                                    selectedUris.forEachIndexed { idx, uri ->
                                        loadBitmapFromUri(context, uri)?.let { orig ->
                                            val w = (orig.width * factor).toInt().coerceAtLeast(1)
                                            val h = (orig.height * factor).toInt().coerceAtLeast(1)
                                            val scaled = Bitmap.createScaledBitmap(orig, w, h, true)
                                            val outFile = File(context.cacheDir, "batch_resized_${idx}_${System.currentTimeMillis()}.jpg")
                                            FileOutputStream(outFile).use { out -> scaled.compress(Bitmap.CompressFormat.JPEG, 90, out) }
                                            scaled.recycle()
                                            files.add(outFile)
                                        }
                                    }
                                }
                                resizedFiles = files
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else {
                            Icon(Icons.Default.AspectRatio, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Resize All ${selectedUris.size} Images", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (resizedFiles.isNotEmpty()) {
                Button(
                    onClick = {
                        resizedFiles.firstOrNull()?.let { shareFile(context, it, "image/jpeg", "Share Resized Photo") }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Resized Photos (${resizedFiles.size})", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedBatchImageConverterView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var targetFormat by remember { mutableStateOf("PNG") }
    var convertedFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        selectedUris = uris
        convertedFiles = emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(
            onClick = { picker.launch("image/*") },
            modifier = Modifier.fillMaxWidth().height(50.dp).pressFeedback(),
            shape = RoundedCornerShape(14.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Transform, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUris.isNotEmpty()) "Selected ${selectedUris.size} Images" else "Select Batch Images to Convert", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (selectedUris.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Target Format:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        FilterChip(selected = targetFormat == "PNG", onClick = { targetFormat = "PNG" }, label = { Text("PNG (Lossless)") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = targetFormat == "JPEG", onClick = { targetFormat = "JPEG" }, label = { Text("JPEG (Compact)") }, modifier = Modifier.weight(1f))
                        FilterChip(selected = targetFormat == "WEBP", onClick = { targetFormat = "WEBP" }, label = { Text("WEBP (Modern)") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isProcessing = true
                                val files = mutableListOf<File>()
                                val (fmt, ext) = when (targetFormat) {
                                    "PNG" -> Bitmap.CompressFormat.PNG to "png"
                                    "WEBP" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY to "webp" else @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP to "webp"
                                    else -> Bitmap.CompressFormat.JPEG to "jpg"
                                }
                                withContext(Dispatchers.IO) {
                                    selectedUris.forEachIndexed { idx, uri ->
                                        loadBitmapFromUri(context, uri)?.let { bmp ->
                                            val outFile = File(context.cacheDir, "batch_conv_${idx}_${System.currentTimeMillis()}.$ext")
                                            FileOutputStream(outFile).use { out -> bmp.compress(fmt, 92, out) }
                                            files.add(outFile)
                                        }
                                    }
                                }
                                convertedFiles = files
                                isProcessing = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Convert All to $targetFormat", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (convertedFiles.isNotEmpty()) {
                Button(
                    onClick = {
                        convertedFiles.firstOrNull()?.let { shareFile(context, it, "image/*", "Share Converted Image") }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                    shape = RoundedCornerShape(12.dp),
                    colors = accentButtonColors(BentoEmerald)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Export & Share Converted Images (${convertedFiles.size})", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DedicatedBatchImageRenamerView() {
    DedicatedFileInfoView()
}

