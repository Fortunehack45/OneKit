package com.one.utility.feature.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.*
import com.one.utility.core.router.ToolDefinition
import com.one.utility.core.router.ToolRegistry
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DedicatedToolScreen(
    toolId: String,
    onNavigateBack: () -> Unit,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tool: ToolDefinition = remember(toolId) {
        ToolRegistry.findTool(toolId) ?: ToolDefinition(
            id = toolId,
            title = toolId.replace('_', ' ').replaceFirstChar { it.uppercase() },
            description = "Standalone Offline Utility",
            category = com.one.utility.core.router.ToolCategory.TECH,
            route = "tool/$toolId",
            keywords = emptyList()
        )
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = tool.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AppTheme.colors.textPrimary
                        )
                        tool.badge?.let { badge ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
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
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tool.category.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.textSecondary
                        )
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
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Header Description Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = tool.description,
                            fontSize = 13.sp,
                            color = AppTheme.colors.textSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Dedicated Interactive Tool Content
            item {
                DedicatedToolBody(
                    toolId = tool.id,
                    tool = tool,
                    onNavigateToRoute = onNavigateToRoute
                )
            }
        }
    }
}

@Composable
fun DedicatedToolBody(
    toolId: String,
    tool: ToolDefinition,
    onNavigateToRoute: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val calcEngine = remember { EverydayCalculatorsEngine() }
    val unitsEngine = remember { ComprehensiveUnitsEngine() }
    val textEngine = remember { TextToolsEngine() }
    val devEngine = remember { DeveloperToolsEngine() }
    val effectsEngine = remember { ImageEffectsEngine(context) }
    val cropperEngine = remember { ImageCropperEngine() }
    val pwdEngine = remember { PasswordGeneratorEngine() }

    fun copy(text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("ONE Utility", text))
    }

    when (toolId) {
        // =========================================================================
        // 📄 DEDICATED PDF TOOLS
        // =========================================================================
        "pdf_compressor" -> DedicatedPdfCompressorView()
        "pdf_rotate" -> DedicatedPdfRotateView()
        "pdf_split" -> DedicatedPdfSplitView()
        "pdf_to_images", "pdf_to_images_page" -> DedicatedPdfToImagesView()
        "pdf_merge", "batch_pdf_processing", "pdf_image_to_pdf" -> DedicatedPdfMergeView()
        "pdf_delete_pages" -> DedicatedPdfDeletePagesView()
        "pdf_extract_pages" -> DedicatedPdfExtractPagesView()
        "pdf_duplicate_pages" -> DedicatedPdfDuplicatePagesView()
        "pdf_reorder" -> DedicatedPdfReorderPagesView()
        "pdf_page_counter" -> DedicatedPdfPageCountView()
        "pdf_add_text" -> DedicatedPdfAddTextView()
        "pdf_add_signature" -> DedicatedSignatureCaptureView()
        "pdf_watermark", "pdf_metadata_editor" -> DedicatedPdfWatermarkView()
        "pdf_metadata_viewer" -> DedicatedPdfPageCountView()
        "pdf_password_protect" -> DedicatedPdfPasswordProtectView()
        "pdf_unlock" -> DedicatedPdfUnlockView()
        "pdf_page_size_converter" -> DedicatedPdfPageSizeConverterView()
        "pdf_scanner" -> DedicatedReceiptScannerView()

        // =========================================================================
        // 📸 DEDICATED IMAGE TOOLS
        // =========================================================================
        "image_resizer" -> DedicatedImageResizerView()
        "image_sharpen" -> DedicatedImageSharpenView()
        "image_grayscale" -> DedicatedImageGrayscaleView()
        "image_brightness", "image_contrast", "image_saturation" -> DedicatedImageBrightnessContrastView()
        "background_changer" -> DedicatedBackgroundChangerView()
        "screenshot_cropper" -> DedicatedScreenshotCropperView()
        "batch_image_compressor" -> DedicatedBatchImageCompressorView()
        "batch_image_resizer" -> DedicatedBatchImageResizerView()
        "batch_image_converter" -> DedicatedBatchImageConverterView()
        "batch_image_renamer" -> DedicatedBatchImageRenamerView()

        // =========================================================================
        // 📷 DEDICATED SCANNER TOOLS
        // =========================================================================
        "scanner_id", "scanner_passport" -> DedicatedIdScannerView()
        "scanner_receipt", "scanner_receipt_card", "scanner_contrast", "scanner_bw", "scan_to_image", "scan_to_pdf", "scanner_ocr", "ocr_to_text" -> DedicatedReceiptScannerView()
        "signature_capture" -> DedicatedSignatureCaptureView()
        "scanner_perspective", "scanner_auto_edge", "scanner_multi_page", "scanner_book", "scanner_whiteboard" -> DedicatedScreenshotCropperView()

        // =========================================================================
        // 🧹 DEDICATED STORAGE & FILE TOOLS
        // =========================================================================
        "empty_folder_finder" -> DedicatedEmptyFolderFinderView()
        "temporary_file_cleaner", "cache_cleaner" -> DedicatedCacheCleanerView()
        "zip_creator" -> DedicatedZipCreatorView()
        "zip_extractor" -> DedicatedZipExtractorView()
        "file_information", "file_size_analyzer" -> DedicatedFileInfoView()
        "folder_size_analyzer" -> DedicatedFolderSizeView()
        "duplicate_file_finder" -> DedicatedDuplicateFinderView()
        "large_file_finder" -> DedicatedLargeFileFinderView()
        "file_renamer" -> DedicatedFileRenamerView()
        "file_extension_changer" -> DedicatedExtensionChangerView()
        "screenshot_finder" -> DedicatedScreenshotFinderView()
        "download_finder" -> DedicatedDownloadFinderView()
        "similar_image_finder" -> DedicatedSimilarImageFinderView()
        "old_file_finder" -> DedicatedOldFileFinderView()

        // =========================================================================
        // 🧮 DEDICATED CALCULATOR TOOLS
        // =========================================================================
        "calc_loan", "loan_calculator", "money_loan_payment", "calc_emi", "emi_calculator" -> DedicatedLoanCalcView(copyAction = ::copy)
        "calc_mortgage", "mortgage_calculator" -> DedicatedMortgageCalcView(copyAction = ::copy)
        "calc_savings", "savings_goal", "money_savings_goal", "calc_savings_goal" -> DedicatedSavingsGoalCalcView(copyAction = ::copy)
        "calc_compound_interest", "compound_interest_calculator", "money_compound_growth" -> DedicatedCompoundInterestCalcView(copyAction = ::copy)
        "calc_simple_interest", "money_interest" -> DedicatedSimpleInterestView(copyAction = ::copy)
        "calc_investment_return" -> DedicatedInvestmentReturnView(copyAction = ::copy)
        "calc_profit", "calc_profit_margin", "calc_markup", "money_profit_loss", "money_markup", "money_margin" -> DedicatedProfitMarginView(copyAction = ::copy)
        "calc_ratio", "ratio_calculator" -> DedicatedRatioCalcView(copyAction = ::copy)
        "calc_fraction" -> DedicatedFractionView(copyAction = ::copy)
        "calc_average", "average_calculator" -> DedicatedAverageCalcView(copyAction = ::copy)
        "calc_speed", "calc_distance", "calc_time_diff" -> DedicatedSpeedDistanceTimeView(copyAction = ::copy)
        "calc_fuel_cost" -> DedicatedFuelCostView(copyAction = ::copy)

        // =========================================================================
        // 📅 DEDICATED DATE & TIME TOOLS
        // =========================================================================
        "time_world_clock", "world_clock" -> DedicatedWorldClockView()
        "time_zone_converter" -> DedicatedTimeZoneConverterView()
        "time_stopwatch", "stopwatch" -> DedicatedStopwatchView()
        "time_timer", "timer" -> DedicatedTimerView()
        "time_countdown", "countdown" -> DedicatedTimeCountdownView()
        "time_unix_timestamp", "tech_unix_timestamp", "unix_timestamp" -> DedicatedUnixTimestampView(copyAction = ::copy)
        "time_business_days", "business_days" -> DedicatedBusinessDaysView(copyAction = ::copy)
        "time_date_calculator", "time_date_difference", "calc_date_diff", "date_difference_calculator", "date_difference" -> DedicatedDateCalculatorView(copyAction = ::copy)
        "time_week_number" -> DedicatedWeekNumberView(copyAction = ::copy)
        "time_alarm_shortcuts" -> DedicatedAlarmShortcutsView()

        // =========================================================================
        // 🔐 DEDICATED PRIVACY & TECH TOOLS
        // =========================================================================
        "qr_wifi" -> DedicatedWifiQrView()
        "qr_contact", "qr_email", "qr_phone", "qr_url", "qr_text_tool" -> DedicatedContactQrView()
        "barcode_generator_tool" -> DedicatedBarcodeGeneratorView()
        "barcode_scanner_tool", "qr_scanner_tool" -> DedicatedReceiptScannerView()
        "hash_sha256_tool", "hash_sha512_tool", "hash_md5_tool", "file_hash_checker" -> DedicatedHashGeneratorView(copyAction = ::copy)
        "base64_enc", "base64_dec" -> DedicatedBase64CodecView(copyAction = ::copy)
        "url_enc", "url_dec", "url_codec_tool" -> DedicatedUrlCodecView(copyAction = ::copy)
        "local_text_enc" -> DedicatedAesEncryptionView(copyAction = ::copy)
        "local_text_dec" -> DedicatedAesDecryptionView(copyAction = ::copy)
        "json_formatter_tool", "json_minifier_tool", "xml_formatter_tool", "html_formatter_tool", "css_formatter_tool", "html_escape_tool" -> DedicatedJsonFormatterView(copyAction = ::copy)
        "passphrase_gen", "pass_generator_tool" -> DedicatedPassphraseGenView(copyAction = ::copy)
        "pass_strength_checker" -> DedicatedPasswordStrengthView()
        "random_num_gen" -> DedicatedRandomNumberView(copyAction = ::copy)
        "random_str_gen" -> DedicatedRandomStringView(copyAction = ::copy)
        "url_parser_tool" -> DedicatedUrlParserView()
        "jwt_decoder_tool" -> DedicatedJwtDecoderView(copyAction = ::copy)
        "regex_tester_tool" -> DedicatedRegexTesterView()
        "color_picker", "screen_color_picker" -> DedicatedColorPickerView(copyAction = ::copy)
        "color_converter_tool", "hex_to_rgb_tool", "rgb_to_hex_tool", "hsl_converter_tool", "color_hex_to_rgb", "color_rgb_to_hex", "color_rgb_to_hsl", "color_hsl_to_rgb", "color_hex_to_hsl" -> DedicatedColorConverterView(copyAction = ::copy)
        "color_palette_gen", "complementary_color" -> DedicatedColorPaletteGeneratorView(copyAction = ::copy)
        "contrast_checker" -> DedicatedContrastCheckerView()
        "user_agent_viewer" -> DedicatedUserAgentView(copyAction = ::copy)
        "ip_information" -> DedicatedIpInfoView(copyAction = ::copy)
        "dns_lookup_tool" -> DedicatedDnsLookupView(copyAction = ::copy)
        "http_status_checker" -> DedicatedHttpStatusView()

        // =========================================================================
        // ✍️ DEDICATED TEXT TOOLS
        // =========================================================================
        "text_char_counter", "text_char_count", "character_counter" -> DedicatedCharCounterView()
        "text_sentence_counter", "text_sentence_count", "sentence_counter" -> DedicatedSentenceCounterView()
        "text_line_counter", "text_line_count", "line_counter" -> DedicatedLineCounterView()
        "text_reading_time", "reading_time_calculator" -> DedicatedReadingTimeView(copyAction = ::copy)
        "text_remove_spaces" -> DedicatedRemoveSpacesView(copyAction = ::copy)
        "text_cleaner" -> DedicatedTextCleanerView(copyAction = ::copy)
        "text_reverse" -> DedicatedReverseTextView(copyAction = ::copy)
        "text_remove_dup_lines" -> DedicatedRemoveDuplicateLinesView(copyAction = ::copy)
        "text_sort_lines" -> DedicatedSortLinesView(copyAction = ::copy)
        "text_find_replace" -> DedicatedFindReplaceView(copyAction = ::copy)
        "text_to_qr" -> DedicatedContactQrView()

        // =========================================================================
        // 📸 IMAGE TOOLS (CONTINUED)
        // =========================================================================
        "image_rotator" -> {
            var selectedUri by remember { mutableStateOf<Uri?>(null) }
            var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var rotatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var currentAngle by remember { mutableStateOf(0f) }

            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedUri = uri
                uri?.let {
                    runCatching {
                        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)) { d, _, _ -> d.isMutableRequired = true }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        }
                        originalBitmap = bmp
                        rotatedBitmap = bmp
                        currentAngle = 0f
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUri != null) "Change Photo" else "Select Photo to Rotate", color = Color.White, fontWeight = FontWeight.Bold)
                }

                rotatedBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.cardSurface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(bitmap = bmp.asImageBitmap(), contentDescription = "Rotated Image", modifier = Modifier.fillMaxSize().padding(10.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                currentAngle = (currentAngle + 90f) % 360f
                                originalBitmap?.let { orig ->
                                    coroutineScope.launch {
                                        rotatedBitmap = cropperEngine.transformBitmap(orig, rotationDegrees = currentAngle)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Rotate 90°", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val outFile = File(context.cacheDir, "ONE_rotated_${System.currentTimeMillis()}.jpg")
                                    cropperEngine.saveBitmapToFile(bmp, outFile)
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, "Share Rotated Photo"))
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Export", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        "image_flipper" -> {
            var selectedUri by remember { mutableStateOf<Uri?>(null) }
            var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var flippedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var flipH by remember { mutableStateOf(false) }
            var flipV by remember { mutableStateOf(false) }

            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedUri = uri
                uri?.let {
                    runCatching {
                        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)) { d, _, _ -> d.isMutableRequired = true }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        }
                        originalBitmap = bmp
                        flippedBitmap = bmp
                        flipH = false
                        flipV = false
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUri != null) "Change Photo" else "Select Photo to Flip", color = Color.White, fontWeight = FontWeight.Bold)
                }

                flippedBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.cardSurface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(bitmap = bmp.asImageBitmap(), contentDescription = "Flipped Image", modifier = Modifier.fillMaxSize().padding(10.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                flipH = !flipH
                                originalBitmap?.let { orig ->
                                    coroutineScope.launch {
                                        flippedBitmap = cropperEngine.transformBitmap(orig, flipHorizontal = flipH, flipVertical = flipV)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Flip Horiz", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                flipV = !flipV
                                originalBitmap?.let { orig ->
                                    coroutineScope.launch {
                                        flippedBitmap = cropperEngine.transformBitmap(orig, flipHorizontal = flipH, flipVertical = flipV)
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = obsidianButtonColors()
                        ) {
                            Icon(Icons.Default.Flip, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Flip Vert", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val outFile = File(context.cacheDir, "ONE_flipped_${System.currentTimeMillis()}.jpg")
                                cropperEngine.saveBitmapToFile(bmp, outFile)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(share, "Share Flipped Photo"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export & Share", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        "image_blur" -> {
            var selectedUri by remember { mutableStateOf<Uri?>(null) }
            var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var blurredBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var blurRadius by remember { mutableFloatStateOf(8f) }

            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedUri = uri
                uri?.let {
                    runCatching {
                        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)) { d, _, _ -> d.isMutableRequired = true }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        }
                        originalBitmap = bmp
                        coroutineScope.launch {
                            blurredBitmap = effectsEngine.applyBlur(bmp, blurRadius.toInt())
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUri != null) "Change Photo" else "Select Photo to Blur", color = Color.White, fontWeight = FontWeight.Bold)
                }

                blurredBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.cardSurface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(bitmap = bmp.asImageBitmap(), contentDescription = "Blurred Image", modifier = Modifier.fillMaxSize().padding(10.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Blur Radius", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                            Text("${blurRadius.toInt()} px", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = blurRadius,
                            onValueChange = { blurRadius = it },
                            onValueChangeFinished = {
                                originalBitmap?.let { orig ->
                                    coroutineScope.launch {
                                        blurredBitmap = effectsEngine.applyBlur(orig, blurRadius.toInt())
                                    }
                                }
                            },
                            valueRange = 1f..25f
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val outFile = File(context.cacheDir, "ONE_blur_${System.currentTimeMillis()}.jpg")
                                effectsEngine.stripExif(bmp, outFile)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(share, "Share Blurred Photo"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export Blurred Image", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        "image_pixelator" -> {
            var selectedUri by remember { mutableStateOf<Uri?>(null) }
            var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var pixelatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
            var pixelSize by remember { mutableFloatStateOf(16f) }

            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedUri = uri
                uri?.let {
                    runCatching {
                        val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)) { d, _, _ -> d.isMutableRequired = true }
                        } else {
                            @Suppress("DEPRECATION")
                            MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                        }
                        originalBitmap = bmp
                        coroutineScope.launch {
                            pixelatedBitmap = effectsEngine.pixelate(bmp, pixelSize.toInt())
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUri != null) "Change Photo" else "Select Photo to Pixelate", color = Color.White, fontWeight = FontWeight.Bold)
                }

                pixelatedBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppTheme.colors.cardSurface)
                            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(bitmap = bmp.asImageBitmap(), contentDescription = "Pixelated Image", modifier = Modifier.fillMaxSize().padding(10.dp))
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Pixel Block Size", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                            Text("${pixelSize.toInt()} px", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = pixelSize,
                            onValueChange = { pixelSize = it },
                            onValueChangeFinished = {
                                originalBitmap?.let { orig ->
                                    coroutineScope.launch {
                                        pixelatedBitmap = effectsEngine.pixelate(orig, pixelSize.toInt())
                                    }
                                }
                            },
                            valueRange = 4f..50f
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val outFile = File(context.cacheDir, "ONE_pixel_${System.currentTimeMillis()}.jpg")
                                effectsEngine.stripExif(bmp, outFile)
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/jpeg"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(share, "Share Censors/Pixelated Photo"))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(12.dp),
                        colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export Pixelated Image", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        "image_metadata_viewer", "remove_image_metadata" -> {
            var selectedUri by remember { mutableStateOf<Uri?>(null) }
            var metadataMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
            var strippedFile by remember { mutableStateOf<File?>(null) }
            var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

            val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                selectedUri = uri
                uri?.let {
                    coroutineScope.launch {
                        metadataMap = effectsEngine.extractExif(it)
                        runCatching {
                            val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it)) { d, _, _ -> d.isMutableRequired = true }
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                            }
                            originalBitmap = bmp
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
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUri != null) "Select Another Photo" else "Select Photo", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (metadataMap.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("EXIF Metadata", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                                IconButton(onClick = {
                                    val summary = metadataMap.entries.joinToString("\n") { "${it.key}: ${it.value}" }
                                    copy(summary)
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            HorizontalDivider(color = AppTheme.colors.borderSubtle)
                            metadataMap.forEach { (k, v) ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(k, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                    Text(v, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppTheme.colors.textPrimary)
                                }
                            }
                        }
                    }

                    originalBitmap?.let { bmp ->
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val outFile = File(context.cacheDir, "ONE_clean_${System.currentTimeMillis()}.jpg")
                                    effectsEngine.stripExif(bmp, outFile)
                                    strippedFile = outFile
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(share, "Share Clean Photo (Zero EXIF)"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                            shape = RoundedCornerShape(12.dp),
                            colors = accentButtonColors(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Strip Metadata & Export Clean Photo", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 🧮 CALCULATORS
        // =========================================================================
        "calc_percentage", "percentage_calculator" -> {
            var percentInput by remember { mutableStateOf("15") }
            var totalInput by remember { mutableStateOf("850000") }

            val p = percentInput.toDoubleOrNull() ?: 0.0
            val tot = totalInput.toDoubleOrNull() ?: 0.0
            val result = (p / 100.0) * tot
            val plusResult = tot + result
            val minusResult = (tot - result).coerceAtLeast(0.0)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = percentInput,
                        onValueChange = { percentInput = it },
                        label = { Text("Percentage (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = totalInput,
                        onValueChange = { totalInput = it },
                        label = { Text("Total Amount") },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("$p% of $tot is:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = String.format(Locale.US, "%,.2f", result).trimEnd('0').trimEnd('.'),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Add $p% (+):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "%,.2f", plusResult), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtract $p% (-):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "%,.2f", minusResult), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        "calc_discount", "discount_calculator", "money_discount" -> {
            var priceInput by remember { mutableStateOf("120") }
            var discountInput by remember { mutableStateOf("25") }

            val price = priceInput.toDoubleOrNull() ?: 0.0
            val disc = discountInput.toDoubleOrNull() ?: 0.0
            val res = calcEngine.calculateDiscount(price, disc)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        label = { Text("Original Price") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = discountInput,
                        onValueChange = { discountInput = it },
                        label = { Text("Discount (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Final Discounted Price:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = String.format(Locale.US, "%,.2f", res.finalPrice),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Money Saved:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "%,.2f", res.savedAmount), fontWeight = FontWeight.Bold, color = BentoEmerald)
                        }
                    }
                }
            }
        }

        "calc_tax", "tax_calculator", "money_tax" -> {
            var amountInput by remember { mutableStateOf("500") }
            var rateInput by remember { mutableStateOf("7.5") }

            val amount = amountInput.toDoubleOrNull() ?: 0.0
            val rate = rateInput.toDoubleOrNull() ?: 0.0
            val (tax, total) = calcEngine.calculateTax(amount, rate)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Base Amount") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = rateInput,
                        onValueChange = { rateInput = it },
                        label = { Text("Tax Rate (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Total with Tax:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = String.format(Locale.US, "%,.2f", total),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax Amount:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "%,.2f", tax), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        "calc_tip", "tip_calculator", "calc_split_bill", "split_bill_calculator", "split_bill", "money_tip", "money_split_bill" -> {
            var billInput by remember { mutableStateOf("85") }
            var tipPercent by remember { mutableStateOf("15") }
            var peopleCount by remember { mutableStateOf("3") }

            val bill = billInput.toDoubleOrNull() ?: 0.0
            val tipP = tipPercent.toDoubleOrNull() ?: 0.0
            val people = peopleCount.toIntOrNull() ?: 1
            val res = calcEngine.calculateTipAndSplit(bill, tipP, people)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = billInput,
                    onValueChange = { billInput = it },
                    label = { Text("Bill Total ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tipPercent,
                        onValueChange = { tipPercent = it },
                        label = { Text("Tip (%)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = peopleCount,
                        onValueChange = { peopleCount = it },
                        label = { Text("Number of People") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Per Person Amount:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = String.format(Locale.US, "$%,.2f", res.perPersonAmount),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Tip:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "$%,.2f", res.tipAmount), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "$%,.2f", res.grandTotal), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        "calc_bmi", "bmi_calculator" -> {
            var weightInput by remember { mutableStateOf("70") }
            var heightInput by remember { mutableStateOf("175") }

            val weight = weightInput.toDoubleOrNull() ?: 0.0
            val height = heightInput.toDoubleOrNull() ?: 0.0
            val res = calcEngine.calculateBmi(weight, height)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Height (cm)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Body Mass Index (BMI):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = String.format(Locale.US, "%.1f", res.bmi),
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Category:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (res.category) {
                                            "Normal weight" -> BentoEmerald.copy(alpha = 0.2f)
                                            "Overweight" -> BentoHoney.copy(alpha = 0.2f)
                                            else -> BentoCoral.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = res.category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (res.category) {
                                        "Normal weight" -> BentoEmerald
                                        "Overweight" -> Color(0xFFC78B00)
                                        else -> BentoCoral
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        "calc_age", "age_calculator" -> {
            var startYear by remember { mutableStateOf("1998") }
            var startMonth by remember { mutableStateOf("5") }
            var startDay by remember { mutableStateOf("15") }

            val start = runCatching {
                LocalDate.of(startYear.toInt(), startMonth.toInt(), startDay.toInt())
            }.getOrNull() ?: LocalDate.of(2000, 1, 1)

            val now = LocalDate.now()
            val diff = calcEngine.calculateDateDifference(start, now)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Enter Date of Birth / Starting Date:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = startYear, onValueChange = { startYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = startMonth, onValueChange = { startMonth = it }, label = { Text("Month") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                    OutlinedTextField(value = startDay, onValueChange = { startDay = it }, label = { Text("Day") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Calculated Age / Duration:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(
                            text = "${diff.years} years, ${diff.months} months, ${diff.days} days",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Days Lived:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(String.format(Locale.US, "%,d days", diff.totalDays), fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ✍️ TEXT TOOLS
        // =========================================================================
        "text_word_count", "text_word_counter", "word_counter" -> {
            var textInput by remember { mutableStateOf("The quick brown fox jumps over the lazy dog. ONE is an offline everyday utility operating system.") }
            val stats = textEngine.analyze(textInput)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = { Text("Input Text") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Live Text Statistics", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                        HorizontalDivider(color = AppTheme.colors.borderSubtle)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Words:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.words}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Characters (with spaces):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.charactersWithSpaces}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Characters (no spaces):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.charactersWithoutSpaces}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sentences:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.sentences}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Lines:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.lines}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Reading Time:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("${stats.estimatedReadingTimeMinutes} min", fontWeight = FontWeight.Bold, color = BentoEmerald)
                        }
                    }
                }
            }
        }

        "text_uppercase", "uppercase_converter", "text_lowercase", "lowercase_converter", "text_title_case", "title_case_converter", "text_sentence_case", "sentence_case_converter" -> {
            var textInput by remember { mutableStateOf("hello world. this is one utility.") }
            var transformedText by remember {
                mutableStateOf(
                    when (toolId) {
                        "uppercase_converter" -> textEngine.toUpperCase(textInput)
                        "lowercase_converter" -> textEngine.toLowerCase(textInput)
                        "title_case_converter" -> textEngine.toTitleCase(textInput)
                        else -> textEngine.toSentenceCase(textInput)
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = {
                        textInput = it
                        transformedText = when (toolId) {
                            "uppercase_converter" -> textEngine.toUpperCase(it)
                            "lowercase_converter" -> textEngine.toLowerCase(it)
                            "title_case_converter" -> textEngine.toTitleCase(it)
                            else -> textEngine.toSentenceCase(it)
                        }
                    },
                    label = { Text("Input Text") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(14.dp)
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Transformed Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            IconButton(onClick = { copy(transformedText) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(transformedText, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = AppTheme.colors.textPrimary)
                    }
                }
            }
        }

        "text_lorem_ipsum", "lorem_ipsum_generator" -> {
            var paragraphCount by remember { mutableFloatStateOf(2f) }
            var generatedLorem by remember { mutableStateOf(textEngine.generateLoremIpsum(2)) }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Paragraphs: ${paragraphCount.toInt()}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
                Slider(
                    value = paragraphCount,
                    onValueChange = {
                        paragraphCount = it
                        generatedLorem = textEngine.generateLoremIpsum(it.toInt())
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Generated Lorem Ipsum:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            IconButton(onClick = { copy(generatedLorem) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(generatedLorem, fontSize = 13.sp, color = AppTheme.colors.textPrimary, lineHeight = 18.sp)
                    }
                }
            }
        }

        // =========================================================================
        // 🔐 PRIVACY & TECH TOOLS
        // =========================================================================
        "uuid_gen", "uuid_generator", "tech_uuid_tool" -> DedicatedUuidView(copyAction = ::copy)

        "device_info", "device_information", "android_version_info", "screen_resolution", "screen_density", "cpu_info", "cpu_information", "ram_info", "ram_information", "battery_info", "battery_information", "display_info", "network_info", "app_info", "storage_info" -> {
            val config = LocalConfiguration.current
            val density = LocalDensity.current
            val screenW = config.screenWidthDp
            val screenH = config.screenHeightDp
            val densityDpi = density.density * 160

            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1

            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val memInfo = android.app.ActivityManager.MemoryInfo().apply { actManager?.getMemoryInfo(this) }
            val totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            val availRamGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)

            val stat = android.os.StatFs(android.os.Environment.getDataDirectory().path)
            val totalStorageGb = (stat.blockCountLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)
            val availStorageGb = (stat.availableBlocksLong * stat.blockSizeLong) / (1024.0 * 1024.0 * 1024.0)

            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            val activeNet = connMgr?.activeNetwork
            val caps = connMgr?.getNetworkCapabilities(activeNet)
            val netType = when {
                caps == null -> "Offline"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi (Active)"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular (Active)"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Connected"
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Hardware Telemetry", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppTheme.colors.textPrimary)
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Device Model:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text("${Build.MANUFACTURER.uppercase()} ${Build.MODEL}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Android Release:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Screen Resolution:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text("${(screenW * density.density).toInt()} x ${(screenH * density.density).toInt()} px (${densityDpi.toInt()} DPI)", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("CPU Architecture:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("System RAM:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(String.format(Locale.US, "%.1f GB Free / %.1f GB Total", availRamGb, totalRamGb), fontWeight = FontWeight.Bold, color = BentoEmerald)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Internal Storage:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(String.format(Locale.US, "%.1f GB Free / %.1f GB Total", availStorageGb, totalStorageGb), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Network Status:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(netType, fontWeight = FontWeight.Bold, color = if (netType == "Offline") AppTheme.colors.textSecondary else BentoEmerald)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Application Version:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text("ONE Utility OS v1.1.4", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    }
                    if (batteryLevel >= 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Battery Level:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text("$batteryLevel%", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 🔄 CONVERTERS (ALL 17 CONVERTERS)
        // =========================================================================
        else -> {
            if (toolId.startsWith("conv_") || tool.category == com.one.utility.core.router.ToolCategory.CONVERTER) {
                var inputValue by remember { mutableStateOf("10") }
                val matchingCategory = remember(toolId) {
                    when {
                        toolId.contains("length") || toolId.contains("distance") -> unitsEngine.categories.find { it.id == "length_astronomy" }
                        toolId.contains("weight") || toolId.contains("mass") -> unitsEngine.categories.find { it.id == "mass_chemistry" }
                        toolId.contains("data") || toolId.contains("storage") -> unitsEngine.categories.find { it.id == "data_storage" }
                        toolId.contains("time") -> unitsEngine.categories.find { it.id == "time" }
                        toolId.contains("speed") -> unitsEngine.categories.find { it.id == "speed" }
                        toolId.contains("temp") -> unitsEngine.categories.find { it.id == "temperature" }
                        toolId.contains("energy") -> unitsEngine.categories.find { it.id == "energy" }
                        toolId.contains("power") -> unitsEngine.categories.find { it.id == "power" }
                        toolId.contains("pressure") -> unitsEngine.categories.find { it.id == "pressure" }
                        toolId.contains("angle") -> unitsEngine.categories.find { it.id == "angle" }
                        toolId.contains("fuel") -> unitsEngine.categories.find { it.id == "fuel_economy" }
                        else -> unitsEngine.categories.firstOrNull()
                    } ?: unitsEngine.categories.first()
                }
                val units = matchingCategory.units
                var fromUnitIndex by remember { mutableIntStateOf(0) }
                var toUnitIndex by remember { mutableIntStateOf(if (units.size > 1) 1 else 0) }

                val fromUnit = units.getOrNull(fromUnitIndex) ?: units.first()
                val toUnit = units.getOrNull(toUnitIndex) ?: units.first()
                val inVal = inputValue.toDoubleOrNull() ?: 0.0

                val convertedValue = if (fromUnit.id == toUnit.id) {
                    inVal
                } else if (matchingCategory.id == "temperature") {
                    when (fromUnit.id) {
                        "c" -> when (toUnit.id) {
                            "f" -> (inVal * 9.0 / 5.0) + 32.0
                            "k" -> inVal + 273.15
                            else -> inVal
                        }
                        "f" -> when (toUnit.id) {
                            "c" -> (inVal - 32.0) * 5.0 / 9.0
                            "k" -> (inVal - 32.0) * 5.0 / 9.0 + 273.15
                            else -> inVal
                        }
                        "k" -> when (toUnit.id) {
                            "c" -> inVal - 273.15
                            "f" -> (inVal - 273.15) * 9.0 / 5.0 + 32.0
                            else -> inVal
                        }
                        else -> inVal
                    }
                } else {
                    val base = inVal * fromUnit.factorToBase
                    base / toUnit.factorToBase
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Enter Value (${fromUnit.symbol})") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        var fromExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { fromExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${fromUnit.symbol} - ${fromUnit.name}", maxLines = 1, color = AppTheme.colors.textPrimary, fontSize = 12.sp)
                            }
                            DropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                                units.forEachIndexed { idx, u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.name} (${u.symbol})") },
                                        onClick = {
                                            fromUnitIndex = idx
                                            fromExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                val temp = fromUnitIndex
                                fromUnitIndex = toUnitIndex
                                toUnitIndex = temp
                            }
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap", tint = MaterialTheme.colorScheme.primary)
                        }

                        var toExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(
                                onClick = { toExpanded = true },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("${toUnit.symbol} - ${toUnit.name}", maxLines = 1, color = AppTheme.colors.textPrimary, fontSize = 12.sp)
                            }
                            DropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                                units.forEachIndexed { idx, u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.name} (${u.symbol})") },
                                        onClick = {
                                            toUnitIndex = idx
                                            toExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Converted Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                val formatted = if (convertedValue % 1.0 == 0.0) String.format(Locale.US, "%,d", convertedValue.toLong()) else String.format(Locale.US, "%,.4f", convertedValue).trimEnd('0').trimEnd('.')
                                Text(
                                    text = "$formatted ${toUnit.symbol}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { copy("$formatted ${toUnit.symbol}") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                when (tool.category) {
                    com.one.utility.core.router.ToolCategory.IMAGES -> DedicatedImageResizerView()
                    com.one.utility.core.router.ToolCategory.PDF -> DedicatedPdfCompressorView()
                    com.one.utility.core.router.ToolCategory.SCANNER -> DedicatedReceiptScannerView()
                    com.one.utility.core.router.ToolCategory.FILES -> DedicatedFileInfoView()
                    com.one.utility.core.router.ToolCategory.CALCULATOR, com.one.utility.core.router.ToolCategory.MONEY -> DedicatedLoanCalcView(copyAction = ::copy)
                    com.one.utility.core.router.ToolCategory.DATE_TIME -> DedicatedWorldClockView()
                    com.one.utility.core.router.ToolCategory.TEXT -> DedicatedFindReplaceView(copyAction = ::copy)
                    com.one.utility.core.router.ToolCategory.TECH -> DedicatedHashGeneratorView(copyAction = ::copy)
                    com.one.utility.core.router.ToolCategory.CONVERTER -> DedicatedFileInfoView()
                }
            }
        }
    }
}
