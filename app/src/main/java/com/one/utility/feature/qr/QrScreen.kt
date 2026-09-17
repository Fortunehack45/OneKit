package com.one.utility.feature.qr

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.one.utility.core.processing.QrCenterIcon
import com.one.utility.core.processing.QrCustomOptions
import com.one.utility.core.processing.QrDotStyle
import com.one.utility.core.processing.QrEngine
import com.one.utility.feature.tools.QR_PALETTES
import com.one.utility.feature.tools.QrCustomizerSection
import com.one.utility.feature.tools.QrPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // Content types: 0 = URL/Text, 1 = Phone Call (tel:), 2 = Contact Card (vCard), 3 = Wi-Fi
    var selectedTypeIndex by remember { mutableIntStateOf(0) }
    val typeLabels = listOf("URL / Web", "Phone Call", "Contact (vCard)", "Wi-Fi")

    // Input States
    var textInput by remember { mutableStateOf("https://one.utility") }
    var phoneNumber by remember { mutableStateOf("+1 555-0199") }
    var vcardName by remember { mutableStateOf("Alex Morgan") }
    var vcardPhone by remember { mutableStateOf("+1 555-0199") }
    var vcardEmail by remember { mutableStateOf("alex.morgan@company.com") }
    var vcardOrg by remember { mutableStateOf("Acme Corporation") }
    var vcardTitle by remember { mutableStateOf("Lead Architect") }
    var vcardNote by remember { mutableStateOf("") }
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiSecurity by remember { mutableStateOf("WPA") }

    // Customization States
    var selectedStyle by remember { mutableStateOf(QrDotStyle.SQUIRCLE) }
    var selectedPalette by remember { mutableStateOf(QR_PALETTES[1]) } // Obsidian preset
    var selectedCenterIcon by remember { mutableStateOf(QrCenterIcon.LINK) }

    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    fun currentPayload(): String {
        return when (selectedTypeIndex) {
            0 -> textInput.trim()
            1 -> engine.buildPhonePayload(phoneNumber)
            2 -> engine.buildContactVCard(
                name = vcardName,
                phone = vcardPhone,
                email = vcardEmail,
                org = vcardOrg,
                title = vcardTitle,
                note = vcardNote
            )
            3 -> engine.buildWifiPayload(wifiSsid, wifiPassword, wifiSecurity)
            else -> textInput.trim()
        }
    }

    fun generate() {
        val payload = currentPayload()
        if (payload.isBlank()) return

        isGenerating = true
        coroutineScope.launch {
            val options = QrCustomOptions(
                style = selectedStyle,
                darkColor = selectedPalette.darkColor,
                lightColor = selectedPalette.lightColor,
                centerIcon = selectedCenterIcon,
                sizePixels = 700
            )
            val result = engine.generateCustomQrCode(payload, options)
            isGenerating = false
            result.onSuccess { generatedBitmap = it }
        }
    }

    // Auto update QR and sensible default icon when parameters change
    LaunchedEffect(
        selectedTypeIndex, textInput, phoneNumber,
        vcardName, vcardPhone, vcardEmail, vcardOrg, vcardTitle, vcardNote,
        wifiSsid, wifiPassword, wifiSecurity,
        selectedStyle, selectedPalette, selectedCenterIcon
    ) {
        generate()
    }

    Scaffold(
        containerColor = AppTheme.colors.canvasBackground,
        topBar = {
            TopAppBar(
                title = { Text("QR Code Studio", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // 1. QR Code Preview Box with Custom Colors
            item {
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(selectedPalette.lightColor))
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(24.dp))
                        .padding(18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating && generatedBitmap == null) {
                        CircularProgressIndicator(color = Color(selectedPalette.darkColor))
                    } else if (generatedBitmap != null) {
                        Image(
                            bitmap = generatedBitmap!!.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text("Enter information to generate", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }

            // 2. Type Selector Chips
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("QR Content Type", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            typeLabels.forEachIndexed { index, label ->
                                FilterChip(
                                    selected = selectedTypeIndex == index,
                                    onClick = {
                                        selectedTypeIndex = index
                                        // Update fitting default icon
                                        selectedCenterIcon = when (index) {
                                            0 -> QrCenterIcon.LINK
                                            1 -> QrCenterIcon.PHONE
                                            2 -> QrCenterIcon.CONTACT
                                            3 -> QrCenterIcon.WIFI
                                            else -> QrCenterIcon.NONE
                                        }
                                    },
                                    label = { Text(label, fontSize = 11.sp, maxLines = 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        when (selectedTypeIndex) {
                            0 -> {
                                // URL / Text Mode
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    label = { Text("Website URL or Text") },
                                    placeholder = { Text("https://example.com") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }

                            1 -> {
                                // Phone Call Mode (tel:)
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        placeholder = { Text("e.g. +1 555-0199") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    val sanitized = engine.cleanPhoneNumber(phoneNumber)
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Text(
                                                "Dialer Payload: tel:$sanitized\nDirectly opens the phone dialer when scanned by any camera.",
                                                fontSize = 11.sp,
                                                color = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            if (sanitized.isNotBlank()) {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$sanitized"))
                                                context.startActivity(intent)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(44.dp).pressFeedback(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Test Dial on This Device", fontSize = 12.sp)
                                    }
                                }
                            }

                            2 -> {
                                // Contact Card (vCard 3.0) Mode
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = vcardName,
                                        onValueChange = { vcardName = it },
                                        label = { Text("Contact Full Name") },
                                        placeholder = { Text("Jane Doe") },
                                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    OutlinedTextField(
                                        value = vcardPhone,
                                        onValueChange = { vcardPhone = it },
                                        label = { Text("Mobile / Cell Phone") },
                                        placeholder = { Text("+1 555-0199") },
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    OutlinedTextField(
                                        value = vcardEmail,
                                        onValueChange = { vcardEmail = it },
                                        label = { Text("Email Address") },
                                        placeholder = { Text("jane@example.com") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = vcardOrg,
                                            onValueChange = { vcardOrg = it },
                                            label = { Text("Company") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        OutlinedTextField(
                                            value = vcardTitle,
                                            onValueChange = { vcardTitle = it },
                                            label = { Text("Job Title") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                            Text(
                                                "RFC 2426/6350 Compliant vCard 3.0 with standard CRLF delimiters for 100% Android & iOS scanner compatibility.",
                                                fontSize = 11.sp,
                                                color = AppTheme.colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            3 -> {
                                // Wi-Fi Mode
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OutlinedTextField(
                                        value = wifiSsid,
                                        onValueChange = { wifiSsid = it },
                                        label = { Text("Wi-Fi Network Name (SSID)") },
                                        placeholder = { Text("Home_Network_5G") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    OutlinedTextField(
                                        value = wifiPassword,
                                        onValueChange = { wifiPassword = it },
                                        label = { Text("Wi-Fi Password") },
                                        placeholder = { Text("Leave blank for open networks") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp)
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                        FilterChip(selected = wifiSecurity == "WPA", onClick = { wifiSecurity = "WPA" }, label = { Text("WPA/WPA2/WPA3") }, modifier = Modifier.weight(1f))
                                        FilterChip(selected = wifiSecurity == "WEP", onClick = { wifiSecurity = "WEP" }, label = { Text("WEP") }, modifier = Modifier.weight(1f))
                                        FilterChip(selected = wifiSecurity == "nopass", onClick = { wifiSecurity = "nopass" }, label = { Text("Open") }, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. QR Customizer Section (Dot styles, Palettes, Center Icons)
            item {
                QrCustomizerSection(
                    selectedStyle = selectedStyle,
                    onStyleChange = { selectedStyle = it },
                    selectedPalette = selectedPalette,
                    onPaletteChange = { selectedPalette = it },
                    selectedCenterIcon = selectedCenterIcon,
                    onCenterIconChange = { selectedCenterIcon = it }
                )
            }

            // 4. Action Buttons
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    // Export & Share High-Res Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val payload = currentPayload()
                                val highResOptions = QrCustomOptions(
                                    style = selectedStyle,
                                    darkColor = selectedPalette.darkColor,
                                    lightColor = selectedPalette.lightColor,
                                    centerIcon = selectedCenterIcon,
                                    sizePixels = 1400
                                )
                                val highResBmp = engine.generateCustomQrCode(payload, highResOptions).getOrNull() ?: generatedBitmap
                                highResBmp?.let { bmp ->
                                    val outFile = File(context.cacheDir, "ONE_QR_${System.currentTimeMillis()}.png")
                                    withContext(Dispatchers.IO) {
                                        FileOutputStream(outFile).use { out ->
                                            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
                                        }
                                    }
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outFile)
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Styled QR Code"))
                                }
                            }
                        },
                        enabled = generatedBitmap != null,
                        modifier = Modifier.fillMaxWidth().height(52.dp).pressFeedback(),
                        shape = RoundedCornerShape(16.dp),
                        colors = obsidianButtonColors()
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Export & Share High-Res QR", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Copy Payload String Button
                    OutlinedButton(
                        onClick = {
                            val payload = currentPayload()
                            if (payload.isNotBlank()) {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("QR Payload", payload))
                                Toast.makeText(context, "QR code payload copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy Raw QR Payload", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
