package com.one.utility.feature.backgroundremover

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.LocalBackgroundRemovalEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

enum class BgOption(val label: String, val color: Color?) {
    TRANSPARENT("Clear", null),
    WHITE("White", Color.White),
    BLUE("Studio Blue", Color(0xFF2C5EAA)),
    SLATE("Matte Slate", Color(0xFF282A33))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundRemoverScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { LocalBackgroundRemovalEngine() }

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var transparentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var tolerance by remember { mutableFloatStateOf(36f) }
    var selectedBgOption by remember { mutableStateOf(BgOption.TRANSPARENT) }

    // Memory-safe bitmap loading with inSampleSize
    fun loadSafeBitmap(uri: Uri): Bitmap? {
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOptions)
            }

            val maxTarget = 1400
            var sampleSize = 1
            while (boundsOptions.outWidth / sampleSize > maxTarget || boundsOptions.outHeight / sampleSize > maxTarget) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            }
        } catch (e: Exception) {
            null
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedUri = uri
            transparentBitmap = null
            coroutineScope.launch {
                isProcessing = true
                val loaded = withContext(Dispatchers.IO) { loadSafeBitmap(uri) }
                originalBitmap = loaded
                if (loaded != null) {
                    val result = engine.removeBackground(loaded, tolerance = tolerance.toDouble())
                    transparentBitmap = result.getOrNull()
                }
                isProcessing = false
            }
        }
    }

    fun reprocess(newTolerance: Float) {
        val src = originalBitmap ?: return
        tolerance = newTolerance
        coroutineScope.launch {
            isProcessing = true
            val result = engine.removeBackground(src, tolerance = newTolerance.toDouble())
            transparentBitmap = result.getOrNull()
            isProcessing = false
        }
    }

    fun getExportBitmap(): Bitmap? {
        val cut = transparentBitmap ?: return null
        val targetColor = selectedBgOption.color ?: return cut

        val composite = Bitmap.createBitmap(cut.width, cut.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)
        val paint = Paint()
        canvas.drawColor(android.graphics.Color.argb(
            (targetColor.alpha * 255).toInt(),
            (targetColor.red * 255).toInt(),
            (targetColor.green * 255).toInt(),
            (targetColor.blue * 255).toInt()
        ))
        canvas.drawBitmap(cut, 0f, 0f, paint)
        return composite
    }

    fun saveToStorage() {
        val export = getExportBitmap() ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val filename = "ONE_cutout_${System.currentTimeMillis()}.png"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ONE")
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    uri?.let { dest ->
                        context.contentResolver.openOutputStream(dest)?.use { out ->
                            export.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    }
                } else {
                    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ONE")
                    dir.mkdirs()
                    val file = File(dir, filename)
                    FileOutputStream(file).use { out ->
                        export.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Saved cutout to Pictures/ONE!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Save failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun shareImage() {
        val export = getExportBitmap() ?: return
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val cacheFile = File(context.cacheDir, "shared_cutout.png")
                FileOutputStream(cacheFile).use { out ->
                    export.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cacheFile)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Cutout"))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Share failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Remove Background", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Image Preview Canvas
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .clickable {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Checkerboard background to visualize transparency
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val squareSize = 20.dp.toPx()
                        val cols = (size.width / squareSize).toInt() + 1
                        val rows = (size.height / squareSize).toInt() + 1
                        val light = androidx.compose.ui.graphics.Color(0xFFEEEEEE)
                        val dark = androidx.compose.ui.graphics.Color(0xFFDDDDDD)

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val color = if ((r + c) % 2 == 0) light else dark
                                drawRect(
                                    color = color,
                                    topLeft = Offset(c * squareSize, r * squareSize),
                                    size = Size(squareSize, squareSize)
                                )
                            }
                        }
                    }

                    // Active background replacement tint
                    selectedBgOption.color?.let { color ->
                        Box(modifier = Modifier.fillMaxSize().background(color))
                    }

                    if (transparentBitmap != null) {
                        Image(
                            bitmap = transparentBitmap!!.asImageBitmap(),
                            contentDescription = "Cutout Result",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else if (isProcessing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = BentoHoney)
                            Text("Isolating subject on-device...", fontSize = 13.sp, color = Color.Black)
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = HeroLavenderDark,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Select Photo for Background Removal", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                            Text("Tap here to pick image from your gallery", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // 2. Tolerance Slider (Sensitivity)
            if (originalBitmap != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Tune, contentDescription = null, tint = BentoHoney, modifier = Modifier.size(18.dp))
                                    Text("Cutout Sensitivity", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                                }
                                Text("${tolerance.toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textSecondary)
                            }

                            Slider(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                onValueChangeFinished = { reprocess(tolerance) },
                                valueRange = 15f..75f,
                                colors = SliderDefaults.colors(
                                    thumbColor = BentoHoney,
                                    activeTrackColor = BentoHoney
                                )
                            )

                            Text(
                                "Adjust if too much or too little of the background was removed",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textMuted
                            )
                        }
                    }
                }

                // 3. Background Replacement Options
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Background Style",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AppTheme.colors.textPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BgOption.values().forEach { opt ->
                                val isSelected = opt == selectedBgOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (isSelected) AppTheme.colors.primaryButton else AppTheme.colors.cardSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle,
                                            RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedBgOption = opt }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) AppTheme.colors.onPrimaryButton else AppTheme.colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Save & Share Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { saveToStorage() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.primaryButton,
                                contentColor = AppTheme.colors.onPrimaryButton
                            )
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Image", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { shareImage() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BentoHoney,
                                contentColor = TextPrimary
                            )
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
