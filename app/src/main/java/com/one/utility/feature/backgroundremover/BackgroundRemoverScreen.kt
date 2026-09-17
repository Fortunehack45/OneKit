package com.one.utility.feature.backgroundremover

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import java.util.ArrayDeque
import kotlin.math.max

enum class BgOption(val label: String, val color: Color?) {
    TRANSPARENT("Clear", null),
    WHITE("White", Color.White),
    BLUE("Studio Blue", Color(0xFF2C5EAA)),
    SLATE("Matte Slate", Color(0xFF282A33)),
    BLACK("Obsidian", Color(0xFF121212)),
    CREAM("Warm Cream", Color(0xFFF7F3E9))
}

enum class EditToolMode {
    VIEW,
    ERASE_BRUSH,
    RESTORE_BRUSH
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

    // Manual Touch-Up Brush State
    var activeTool by remember { mutableStateOf(EditToolMode.VIEW) }
    var brushRadius by remember { mutableFloatStateOf(28f) }
    val historyStack = remember { ArrayDeque<Bitmap>() }

    fun pushHistory() {
        transparentBitmap?.let { current ->
            if (historyStack.size >= 5) historyStack.removeFirst()
            historyStack.addLast(current.copy(current.config ?: Bitmap.Config.ARGB_8888, true))
        }
    }

    fun undo() {
        if (!historyStack.isEmpty()) {
            transparentBitmap = historyStack.removeLast()
        }
    }

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
            historyStack.clear()
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
            result.getOrNull()?.let {
                pushHistory()
                transparentBitmap = it
            }
            isProcessing = false
        }
    }

    // Interactive finger brush application on bitmap coordinate space
    fun applyBrushStroke(screenX: Float, screenY: Float, viewWidth: Float, viewHeight: Float) {
        val currentCut = transparentBitmap ?: return
        val orig = originalBitmap ?: return

        val scaleX = currentCut.width.toFloat() / viewWidth
        val scaleY = currentCut.height.toFloat() / viewHeight
        val bmpX = screenX * scaleX
        val bmpY = screenY * scaleY
        val bmpRadius = brushRadius * ((scaleX + scaleY) / 2f)

        val working = currentCut.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(working)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        if (activeTool == EditToolMode.ERASE_BRUSH) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawCircle(bmpX, bmpY, bmpRadius, paint)
        } else if (activeTool == EditToolMode.RESTORE_BRUSH) {
            // Restore from original bitmap using a circular clip
            val path = android.graphics.Path().apply {
                addCircle(bmpX, bmpY, bmpRadius, android.graphics.Path.Direction.CW)
            }
            canvas.save()
            canvas.clipPath(path)
            canvas.drawBitmap(orig, 0f, 0f, paint)
            canvas.restore()
        }

        transparentBitmap = working
    }

    fun getExportBitmap(): Bitmap? {
        val cut = transparentBitmap ?: return null
        val targetColor = selectedBgOption.color ?: return cut

        val composite = Bitmap.createBitmap(cut.width, cut.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(composite)
        val paint = Paint()
        canvas.drawColor(
            android.graphics.Color.argb(
                (targetColor.alpha * 255).toInt(),
                (targetColor.red * 255).toInt(),
                (targetColor.green * 255).toInt(),
                (targetColor.blue * 255).toInt()
            )
        )
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
                title = {
                    Column {
                        Text(
                            text = "AI Background Remover",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = "Studio AI Segmentation + Precision Brush",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppTheme.colors.textPrimary
                        )
                    }
                },
                actions = {
                    if (historyStack.isNotEmpty()) {
                        IconButton(onClick = { undo() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo Brush",
                                tint = AppTheme.colors.textPrimary
                            )
                        }
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
            // 1. Image Preview & Interactive Touch-Up Canvas
            item {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .background(AppTheme.colors.surfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    val boxWidthPx = constraints.maxWidth.toFloat()
                    val boxHeightPx = constraints.maxHeight.toFloat()

                    // Checkerboard background to visualize transparency
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val squareSize = 18.dp.toPx()
                        val cols = (size.width / squareSize).toInt() + 1
                        val rows = (size.height / squareSize).toInt() + 1
                        val light = Color(0xFFE8E8E8)
                        val dark = Color(0xFFD4D4D4)

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
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(activeTool) {
                                    if (activeTool != EditToolMode.VIEW) {
                                        detectDragGestures(
                                            onDragStart = { pushHistory() },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                applyBrushStroke(
                                                    change.position.x,
                                                    change.position.y,
                                                    boxWidthPx,
                                                    boxHeightPx
                                                )
                                            }
                                        )
                                    }
                                },
                            contentScale = ContentScale.Fit
                        )
                    } else if (isProcessing) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                "AI Segmenting subject...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Select Image to Cut Out",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AppTheme.colors.textPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "AI isolates people, clothes, and subjects instantly",
                                fontSize = 12.sp,
                                color = AppTheme.colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (originalBitmap != null) {
                // 2. Editing Tool Mode Selector (Auto AI vs Erase Brush vs Restore Brush)
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "TOUCH-UP BRUSH & TOOLS",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.textTertiary,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // View / Normal Mode
                                FilterChip(
                                    selected = activeTool == EditToolMode.VIEW,
                                    onClick = { activeTool = EditToolMode.VIEW },
                                    label = { Text("Auto AI", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.AutoFixHigh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = AppTheme.colors.canvasBackground,
                                        labelColor = AppTheme.colors.textSecondary
                                    )
                                )

                                // Erase Brush
                                FilterChip(
                                    selected = activeTool == EditToolMode.ERASE_BRUSH,
                                    onClick = { activeTool = EditToolMode.ERASE_BRUSH },
                                    label = { Text("Erase", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.CleaningServices,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = AppTheme.colors.canvasBackground,
                                        labelColor = AppTheme.colors.textSecondary
                                    )
                                )

                                // Restore Brush
                                FilterChip(
                                    selected = activeTool == EditToolMode.RESTORE_BRUSH,
                                    onClick = { activeTool = EditToolMode.RESTORE_BRUSH },
                                    label = { Text("Restore", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Brush,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = AppTheme.colors.canvasBackground,
                                        labelColor = AppTheme.colors.textSecondary
                                    )
                                )
                            }

                            if (activeTool != EditToolMode.VIEW) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Brush Size: ${brushRadius.toInt()}px",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = "Drag finger on photo to ${if (activeTool == EditToolMode.ERASE_BRUSH) "erase" else "restore"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Slider(
                                    value = brushRadius,
                                    onValueChange = { brushRadius = it },
                                    valueRange = 10f..60f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = MaterialTheme.colorScheme.primary,
                                        activeTrackColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }
                }

                // 3. Sensitivity Slider
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.surfaceCard,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Tune,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "AI Sensitivity / Feathering",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AppTheme.colors.textPrimary
                                    )
                                }
                                Text(
                                    "${tolerance.toInt()}%",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textSecondary
                                )
                            }

                            Slider(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                onValueChangeFinished = { reprocess(tolerance) },
                                valueRange = 15f..80f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            Text(
                                "Fine-tunes the border threshold between foreground and background",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textTertiary
                            )
                        }
                    }
                }

                // 4. Background Replacement Palette
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "BACKGROUND STYLE",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.textTertiary,
                            letterSpacing = 1.sp
                        )

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(BgOption.values()) { opt ->
                                val isSelected = opt == selectedBgOption
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedBgOption = opt },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.surfaceCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color.Transparent else AppTheme.colors.borderSubtle
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (opt.color != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(opt.color)
                                                    .border(
                                                        1.dp,
                                                        if (isSelected) Color.White.copy(alpha = 0.7f) else AppTheme.colors.borderSubtle,
                                                        CircleShape
                                                    )
                                            )
                                        } else {
                                            Icon(
                                                Icons.Outlined.LayersClear,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary
                                            )
                                        }
                                        Text(
                                            text = opt.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Save & Share Actions
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
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Outlined.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save Image", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { shareImage() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = AppTheme.colors.textPrimary
                            )
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Share", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Pick another photo button
                item {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.borderSubtle),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppTheme.colors.textPrimary
                        )
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Choose Different Image", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
