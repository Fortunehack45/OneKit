package com.one.utility.feature.qr

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.one.utility.core.processing.QrEngine
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { QrEngine() }

    var qrType by remember { mutableStateOf("URL / Text") }
    var textInput by remember { mutableStateOf("https://one.utility") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }

    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    fun generate() {
        val payload = if (qrType == "Wi-Fi") {
            engine.buildWifiPayload(wifiSsid, wifiPassword)
        } else {
            textInput
        }
        if (payload.isBlank()) return

        isGenerating = true
        coroutineScope.launch {
            val result = engine.generateQrCode(payload)
            isGenerating = false
            result.onSuccess { generatedBitmap = it }
        }
    }

    LaunchedEffect(Unit) {
        generate()
    }

    Scaffold(
        containerColor = CanvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("QR Code Generator", fontWeight = FontWeight.Bold, color = TextPrimary) },
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
            // 1. QR Preview Box
            item {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = DockObsidian)
                    } else if (generatedBitmap != null) {
                        Image(
                            bitmap = generatedBitmap!!.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Enter text to generate", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }

            // 2. Input Fields
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("QR Content Type", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("URL / Text", "Wi-Fi").forEach { type ->
                                FilterChip(
                                    selected = qrType == type,
                                    onClick = {
                                        qrType = type
                                        generate()
                                    },
                                    label = { Text(type) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DockObsidian,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        if (qrType == "Wi-Fi") {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = {
                                    wifiSsid = it
                                    generate()
                                },
                                label = { Text("Wi-Fi Network Name (SSID)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = wifiPassword,
                                onValueChange = {
                                    wifiPassword = it
                                    generate()
                                },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                        } else {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = {
                                    textInput = it
                                    generate()
                                },
                                label = { Text("Enter Website URL or Text") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                    }
                }
            }

            // 3. Share Action Button
            item {
                Button(
                    onClick = {
                        val bitmap = generatedBitmap ?: return@Button
                        val file = File(context.cacheDir, "ONE_QR_${System.currentTimeMillis()}.png")
                        FileOutputStream(file).use { out ->
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share QR Code"))
                    },
                    enabled = generatedBitmap != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DockObsidian)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share QR Code", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
