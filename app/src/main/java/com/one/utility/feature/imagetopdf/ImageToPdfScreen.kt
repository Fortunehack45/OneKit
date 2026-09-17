package com.one.utility.feature.imagetopdf

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
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
import com.one.utility.core.processing.ImageToPdfEngine
import com.one.utility.core.processing.PageSize
import com.one.utility.core.processing.PdfOptions
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { ImageToPdfEngine(context) }

    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var pageSize by remember { mutableStateOf(PageSize.A4) }
    var isLandscape by remember { mutableStateOf(false) }
    var marginPoints by remember { mutableFloatStateOf(24f) }
    var fitPage by remember { mutableStateOf(true) }
    var addPageNumbers by remember { mutableStateOf(true) }

    var isProcessing by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Photo picker for selecting multiple images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedUris = selectedUris + uris
        }
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("Image → PDF", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            // 1. Image Selection Box / Carousel
            item {
                if (selectedUris.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(AppTheme.colors.cardSurface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = BentoHoney,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("Select Photos", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                            Text("Supports JPG, PNG, WEBP", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedUris.size} Images Selected",
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            TextButton(onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }) {
                                Text("+ Add More", color = BentoHoney, fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(selectedUris) { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(AppTheme.colors.cardSurface)
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    IconButton(
                                        onClick = {
                                            selectedUris = selectedUris.toMutableList().also { it.removeAt(index) }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(24.dp)
                                            .padding(2.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Page & Layout Settings Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("PDF Page Options", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)

                        // Page Size Selection Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PageSize.values().forEach { size ->
                                FilterChip(
                                    selected = pageSize == size,
                                    onClick = { pageSize = size },
                                    label = { Text(if (size == PageSize.ORIGINAL) "Original" else size.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = AppTheme.colors.cardSurface,
                                        labelColor = AppTheme.colors.textSecondary
                                    )
                                )
                            }
                        }

                        // Orientation Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Landscape Orientation", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(checked = isLandscape, onCheckedChange = { isLandscape = it })
                        }

                        // Fit vs Fill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fit Image to Page (Preserve Aspect)", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(checked = fitPage, onCheckedChange = { fitPage = it })
                        }

                        // Page Numbers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Add Page Numbers", fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            Switch(checked = addPageNumbers, onCheckedChange = { addPageNumbers = it })
                        }
                    }
                }
            }

            // 3. Progress / Result Area
            item {
                if (isProcessing) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.bentoHoneySubtle,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { if (totalPages > 0) currentProgress.toFloat() / totalPages else 0f },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = BentoHoney
                            )
                            Text(
                                "Generating Page $currentProgress of $totalPages...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                        }
                    }
                }

                generatedPdfFile?.let { pdfFile ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier.fillMaxWidth().border(1.dp, BentoHoney, RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BentoHoney)
                                Column {
                                    Text("PDF Created Successfully", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                                    Text("${pdfFile.name} • ${(pdfFile.length() / 1024)} KB", fontSize = 12.sp, color = AppTheme.colors.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        pdfFile
                                    )
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share PDF"))
                                },
                                colors = obsidianButtonColors(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 4. Action Button
            item {
                Button(
                    onClick = {
                        if (selectedUris.isEmpty()) return@Button
                        isProcessing = true
                        errorMessage = null
                        coroutineScope.launch {
                            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                            val output = File(context.cacheDir, "ONE_$timestamp.pdf")
                            val options = PdfOptions(
                                pageSize = pageSize,
                                marginPoints = marginPoints.toInt(),
                                isLandscape = isLandscape,
                                fitPage = fitPage,
                                addPageNumbers = addPageNumbers
                            )
                            val result = engine.convertImagesToPdf(
                                imageUris = selectedUris,
                                outputFile = output,
                                options = options,
                                onProgress = { cur, tot ->
                                    currentProgress = cur
                                    totalPages = tot
                                }
                            )
                            isProcessing = false
                            result.onSuccess { generatedPdfFile = it }
                                .onFailure { errorMessage = it.localizedMessage }
                        }
                    },
                    enabled = selectedUris.isNotEmpty() && !isProcessing,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = obsidianButtonColors()
                ) {
                    Text(
                        text = if (isProcessing) "Converting..." else "Create PDF",
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
