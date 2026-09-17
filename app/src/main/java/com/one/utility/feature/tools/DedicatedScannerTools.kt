package com.one.utility.feature.tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.ImageEffectsEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun DedicatedIdScannerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var combinedFile by remember { mutableStateOf<File?>(null) }
    var isFrontActive by remember { mutableStateOf(true) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (isFrontActive) frontUri = uri else backUri = uri
        combinedFile = null
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, if (frontUri != null) BentoEmerald else AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    .pressFeedback {
                        isFrontActive = true
                        picker.launch("image/*")
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = if (frontUri != null) BentoEmerald else AppTheme.colors.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(if (frontUri != null) "Front Added ✓" else "Tap for Front Side", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier
                    .weight(1f)
                    .height(140.dp)
                    .border(1.dp, if (backUri != null) BentoEmerald else AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    .pressFeedback {
                        isFrontActive = false
                        picker.launch("image/*")
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = if (backUri != null) BentoEmerald else AppTheme.colors.textSecondary)
                    Spacer(Modifier.height(6.dp))
                    Text(if (backUri != null) "Back Added ✓" else "Tap for Back Side", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                }
            }
        }

        if (frontUri != null || backUri != null) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val frontBmp = frontUri?.let { loadBitmapFromUri(context, it) }
                        val backBmp = backUri?.let { loadBitmapFromUri(context, it) }
                        val outFile = File(context.cacheDir, "ONE_id_combined_${System.currentTimeMillis()}.jpg")

                        withContext(Dispatchers.Default) {
                            val w = 1200
                            val h = 1800
                            val combined = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(combined)
                            canvas.drawColor(android.graphics.Color.WHITE)

                            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                            frontBmp?.let {
                                val scaled = Bitmap.createScaledBitmap(it, 1000, 650, true)
                                canvas.drawBitmap(scaled, 100f, 150f, paint)
                            }
                            backBmp?.let {
                                val scaled = Bitmap.createScaledBitmap(it, 1000, 650, true)
                                canvas.drawBitmap(scaled, 100f, 900f, paint)
                            }

                            FileOutputStream(outFile).use { out -> combined.compress(Bitmap.CompressFormat.JPEG, 92, out) }
                            combined.recycle()
                        }
                        combinedFile = outFile
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Combine Front & Back onto Single Sheet", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        combinedFile?.let { out ->
            Button(
                onClick = { shareFile(context, out, "image/jpeg", "Share Combined ID Card") },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Combined ID Card", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedReceiptScannerView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val effectsEngine = remember { ImageEffectsEngine(context) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var enhancedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
        uri?.let {
            val bmp = loadBitmapFromUri(context, it)
            originalBitmap = bmp
            bmp?.let { b ->
                coroutineScope.launch {
                    val gray = effectsEngine.applyGrayscale(b)
                    enhancedBitmap = effectsEngine.adjustContrast(gray, 1.7f)
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
            Icon(Icons.Default.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (selectedUri != null) "Select Another Receipt" else "Select Receipt to Enhance", color = Color.White, fontWeight = FontWeight.Bold)
        }

        enhancedBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppTheme.colors.cardSurface)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Enhanced Receipt", modifier = Modifier.fillMaxSize().padding(10.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_receipt_${System.currentTimeMillis()}.jpg")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.JPEG, 92, out) }
                        }
                        shareFile(context, outFile, "image/jpeg", "Share High-Contrast Receipt")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Clean Receipt", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedSignatureCaptureView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lines = remember { mutableStateListOf<List<Offset>>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var inkColor by remember { mutableStateOf(Color.Black) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .border(2.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> currentLine = listOf(offset) },
                        onDrag = { change, _ ->
                            currentLine = currentLine + change.position
                        },
                        onDragEnd = {
                            if (currentLine.isNotEmpty()) {
                                lines.add(currentLine)
                                currentLine = emptyList()
                            }
                        }
                    )
                }
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val drawPaint = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                lines.forEach { line ->
                    if (line.size > 1) {
                        for (i in 0 until line.size - 1) {
                            drawLine(color = inkColor, start = line[i], end = line[i + 1], strokeWidth = 6f)
                        }
                    }
                }
                if (currentLine.size > 1) {
                    for (i in 0 until currentLine.size - 1) {
                        drawLine(color = inkColor, start = currentLine[i], end = currentLine[i + 1], strokeWidth = 6f)
                    }
                }
            }

            Text(
                text = "Sign with finger or stylus",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { lines.clear(); currentLine = emptyList() },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_signature_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.Default) {
                            val bmp = Bitmap.createBitmap(800, 400, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bmp)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = if (inkColor == Color.Black) android.graphics.Color.BLACK else android.graphics.Color.BLUE
                                strokeWidth = 8f
                                style = Paint.Style.STROKE
                                strokeCap = Paint.Cap.ROUND
                                strokeJoin = Paint.Join.ROUND
                            }

                            lines.forEach { line ->
                                if (line.size > 1) {
                                    val path = Path()
                                    path.moveTo(line[0].x, line[0].y)
                                    for (i in 1 until line.size) {
                                        path.lineTo(line[i].x, line[i].y)
                                    }
                                    canvas.drawPath(path, paint)
                                }
                            }

                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                            bmp.recycle()
                        }
                        shareFile(context, outFile, "image/png", "Share Transparent Signature")
                    }
                },
                modifier = Modifier.weight(1.5f).height(46.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(MaterialTheme.colorScheme.primary),
                enabled = lines.isNotEmpty()
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export Transparent PNG", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
