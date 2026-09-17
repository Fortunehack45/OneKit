package com.one.utility.feature.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.one.utility.core.designsystem.*
import com.one.utility.core.processing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

data class QrPalette(
    val name: String,
    val darkColor: Int,
    val lightColor: Int
)

val QR_PALETTES = listOf(
    QrPalette("Classic", android.graphics.Color.BLACK, android.graphics.Color.WHITE),
    QrPalette("Obsidian", android.graphics.Color.parseColor("#0F172A"), android.graphics.Color.parseColor("#F8FAFC")),
    QrPalette("Indigo", android.graphics.Color.parseColor("#4338CA"), android.graphics.Color.parseColor("#EEF2FF")),
    QrPalette("Emerald", android.graphics.Color.parseColor("#047857"), android.graphics.Color.parseColor("#ECFDF5")),
    QrPalette("Crimson", android.graphics.Color.parseColor("#BE123C"), android.graphics.Color.parseColor("#FFF1F2")),
    QrPalette("Violet", android.graphics.Color.parseColor("#6D28D9"), android.graphics.Color.parseColor("#F5F3FF")),
    QrPalette("Gold", android.graphics.Color.parseColor("#B45309"), android.graphics.Color.parseColor("#FFFBEB"))
)

@Composable
fun QrCustomizerSection(
    selectedStyle: QrDotStyle,
    onStyleChange: (QrDotStyle) -> Unit,
    selectedPalette: QrPalette,
    onPaletteChange: (QrPalette) -> Unit,
    selectedCenterIcon: QrCenterIcon,
    onCenterIconChange: (QrCenterIcon) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = AppTheme.colors.cardSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Text("QR Code Customizer", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
            }

            // 1. Module Dot Style
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pattern Style", fontSize = 12.sp, color = AppTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "Square" to QrDotStyle.SQUARE,
                        "Dots" to QrDotStyle.ROUNDED_DOTS,
                        "Squircle" to QrDotStyle.SQUIRCLE
                    ).forEach { (label, style) ->
                        FilterChip(
                            selected = selectedStyle == style,
                            onClick = { onStyleChange(style) },
                            label = { Text(label, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 2. Color Palettes
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Color Palette", fontSize = 12.sp, color = AppTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QR_PALETTES.forEach { palette ->
                        val isSelected = selectedPalette.name == palette.name
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else AppTheme.colors.surfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.borderSubtle
                            ),
                            modifier = Modifier.clickable { onPaletteChange(palette) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(palette.darkColor))
                                        .border(1.dp, Color(palette.lightColor), CircleShape)
                                )
                                Text(
                                    text = palette.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else AppTheme.colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // 3. Center Icon / Logo
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Center Icon Badge", fontSize = 12.sp, color = AppTheme.colors.textSecondary, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "None" to QrCenterIcon.NONE,
                        "Phone" to QrCenterIcon.PHONE,
                        "Contact" to QrCenterIcon.CONTACT,
                        "Link" to QrCenterIcon.LINK,
                        "Wi-Fi" to QrCenterIcon.WIFI,
                        "Star" to QrCenterIcon.STAR,
                        "Heart" to QrCenterIcon.HEART
                    ).forEach { (label, icon) ->
                        FilterChip(
                            selected = selectedCenterIcon == icon,
                            onClick = { onCenterIconChange(icon) },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedWifiQrView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val qrEngine = remember { QrEngine() }

    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var security by remember { mutableStateOf("WPA") }

    // Customization state
    var selectedStyle by remember { mutableStateOf(QrDotStyle.SQUARE) }
    var selectedPalette by remember { mutableStateOf(QR_PALETTES[0]) }
    var selectedCenterIcon by remember { mutableStateOf(QrCenterIcon.WIFI) }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun updateQr() {
        if (ssid.isNotBlank()) {
            coroutineScope.launch {
                val payload = qrEngine.buildWifiPayload(ssid, password, security)
                val options = QrCustomOptions(
                    style = selectedStyle,
                    darkColor = selectedPalette.darkColor,
                    lightColor = selectedPalette.lightColor,
                    centerIcon = selectedCenterIcon,
                    sizePixels = 700
                )
                qrBitmap = qrEngine.generateCustomQrCode(payload, options).getOrNull()
            }
        }
    }

    LaunchedEffect(selectedStyle, selectedPalette, selectedCenterIcon) {
        updateQr()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it; updateQr() },
            label = { Text("Network Name (SSID)") },
            placeholder = { Text("e.g. Home_Network_5G") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; updateQr() },
            label = { Text("Wi-Fi Password") },
            placeholder = { Text("Leave blank for open networks") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = security == "WPA", onClick = { security = "WPA"; updateQr() }, label = { Text("WPA/WPA2/WPA3") }, modifier = Modifier.weight(1f))
            FilterChip(selected = security == "WEP", onClick = { security = "WEP"; updateQr() }, label = { Text("WEP") }, modifier = Modifier.weight(1f))
            FilterChip(selected = security == "nopass", onClick = { security = "nopass"; updateQr() }, label = { Text("Open / None") }, modifier = Modifier.weight(1f))
        }

        qrBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(selectedPalette.lightColor))
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Wi-Fi QR", modifier = Modifier.fillMaxSize().padding(16.dp))
            }

            QrCustomizerSection(
                selectedStyle = selectedStyle,
                onStyleChange = { selectedStyle = it },
                selectedPalette = selectedPalette,
                onPaletteChange = { selectedPalette = it },
                selectedCenterIcon = selectedCenterIcon,
                onCenterIconChange = { selectedCenterIcon = it }
            )

            Button(
                onClick = {
                    coroutineScope.launch {
                        val payload = qrEngine.buildWifiPayload(ssid, password, security)
                        val highResOptions = QrCustomOptions(
                            style = selectedStyle,
                            darkColor = selectedPalette.darkColor,
                            lightColor = selectedPalette.lightColor,
                            centerIcon = selectedCenterIcon,
                            sizePixels = 1400
                        )
                        val exportBmp = qrEngine.generateCustomQrCode(payload, highResOptions).getOrNull() ?: bmp
                        val outFile = File(context.cacheDir, "ONE_wifi_qr_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> exportBmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        }
                        shareFile(context, outFile, "image/png", "Share Wi-Fi QR Code")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Custom Wi-Fi QR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedContactQrView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val qrEngine = remember { QrEngine() }

    // Mode: 0 = Direct Phone Call (tel:), 1 = Full Contact Card (vCard 3.0), 2 = SMS Message (smsto:)
    var modeIndex by remember { mutableIntStateOf(0) }

    // Direct Phone Call state
    var phoneNumber by remember { mutableStateOf("+1 555-0199") }

    // Contact Card vCard state
    var fullName by remember { mutableStateOf("Alex Morgan") }
    var contactPhone by remember { mutableStateOf("+1 555-0199") }
    var email by remember { mutableStateOf("alex.morgan@company.com") }
    var company by remember { mutableStateOf("Acme Corporation") }
    var jobTitle by remember { mutableStateOf("Lead Architect") }
    var contactNote by remember { mutableStateOf("") }

    // SMS Message state
    var smsPhone by remember { mutableStateOf("+1 555-0199") }
    var smsMessage by remember { mutableStateOf("Hello, I am contacting you regarding your service.") }

    // Customization state
    var selectedStyle by remember { mutableStateOf(QrDotStyle.SQUIRCLE) }
    var selectedPalette by remember { mutableStateOf(QR_PALETTES[1]) } // Obsidian preset
    var selectedCenterIcon by remember { mutableStateOf(QrCenterIcon.PHONE) }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun currentPayload(): String {
        return when (modeIndex) {
            0 -> qrEngine.buildPhonePayload(phoneNumber)
            1 -> qrEngine.buildContactVCard(
                name = fullName,
                phone = contactPhone,
                email = email,
                org = company,
                title = jobTitle,
                note = contactNote
            )
            else -> qrEngine.buildSmsPayload(smsPhone, smsMessage)
        }
    }

    fun updateQr() {
        val payload = currentPayload()
        if (payload.isNotBlank()) {
            coroutineScope.launch {
                val options = QrCustomOptions(
                    style = selectedStyle,
                    darkColor = selectedPalette.darkColor,
                    lightColor = selectedPalette.lightColor,
                    centerIcon = selectedCenterIcon,
                    sizePixels = 700
                )
                qrBitmap = qrEngine.generateCustomQrCode(payload, options).getOrNull()
            }
        }
    }

    LaunchedEffect(modeIndex, phoneNumber, fullName, contactPhone, email, company, jobTitle, contactNote, smsPhone, smsMessage, selectedStyle, selectedPalette, selectedCenterIcon) {
        // Automatically default icon to Phone for Direct Call and Contact for vCard
        if (modeIndex == 0 && selectedCenterIcon == QrCenterIcon.CONTACT) {
            selectedCenterIcon = QrCenterIcon.PHONE
        } else if (modeIndex == 1 && selectedCenterIcon == QrCenterIcon.PHONE) {
            selectedCenterIcon = QrCenterIcon.CONTACT
        }
        updateQr()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechSegmentedControl(
            options = listOf("Phone Call (tel:)", "Contact Card (vCard)", "SMS (smsto:)"),
            selectedIndex = modeIndex,
            onSelect = { modeIndex = it }
        )

        when (modeIndex) {
            0 -> {
                // === DIRECT PHONE CALL MODE ===
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number to Dial") },
                    placeholder = { Text("e.g. +1 555-0199 or 080 1234 5678") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoEmerald.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald, modifier = Modifier.size(18.dp))
                        Column {
                            Text("Direct Phone Dialer Protocol", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BentoEmerald)
                            Text("Scanners immediately launch the dialer with ${qrEngine.cleanPhoneNumber(phoneNumber)}. No contact saving required.", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                }

                Button(
                    onClick = {
                        val clean = qrEngine.cleanPhoneNumber(phoneNumber)
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$clean"))
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Test Dial on This Device", color = Color.White, fontSize = 12.sp)
                }
            }

            1 -> {
                // === FULL VCARD 3.0 CONTACT CARD MODE ===
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name (First & Last)") },
                    placeholder = { Text("e.g. Jane Doe") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contactPhone,
                    onValueChange = { contactPhone = it },
                    label = { Text("Mobile / Cell Phone") },
                    placeholder = { Text("e.g. +1 555-0199") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("e.g. jane@company.com") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = jobTitle,
                        onValueChange = { jobTitle = it },
                        label = { Text("Job Title") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text("RFC-Compliant vCard 3.0: Formatted with standard CRLF line delimiters and structured name records for 100% scanner compatibility.", fontSize = 11.sp, color = AppTheme.colors.textSecondary)
                    }
                }
            }

            2 -> {
                // === SMS MESSAGE MODE ===
                OutlinedTextField(
                    value = smsPhone,
                    onValueChange = { smsPhone = it },
                    label = { Text("Recipient Phone Number") },
                    placeholder = { Text("e.g. +1 555-0199") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = smsMessage,
                    onValueChange = { smsMessage = it },
                    label = { Text("Pre-filled Text Message") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        qrBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(selectedPalette.lightColor))
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Contact QR",
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }

            // QR Customization Section (Module style, Color palette, Center badge)
            QrCustomizerSection(
                selectedStyle = selectedStyle,
                onStyleChange = { selectedStyle = it },
                selectedPalette = selectedPalette,
                onPaletteChange = { selectedPalette = it },
                selectedCenterIcon = selectedCenterIcon,
                onCenterIconChange = { selectedCenterIcon = it }
            )

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
                        val exportBmp = qrEngine.generateCustomQrCode(payload, highResOptions).getOrNull() ?: bmp
                        val outFile = File(context.cacheDir, "ONE_contact_qr_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> exportBmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        }
                        shareFile(context, outFile, "image/png", "Share Custom Contact QR")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Custom Contact QR", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedBarcodeGeneratorView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val qrEngine = remember { QrEngine() }
    var barcodeText by remember { mutableStateOf("978020137962") }
    var barcodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(barcodeText) {
        if (barcodeText.isNotBlank()) {
            barcodeBitmap = qrEngine.generateBarcode(barcodeText, 700, 260).getOrNull()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = barcodeText,
            onValueChange = { barcodeText = it },
            label = { Text("Barcode Digits or Alphanumeric Text") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        barcodeBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Barcode", modifier = Modifier.fillMaxSize().padding(16.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_barcode_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        }
                        shareFile(context, outFile, "image/png", "Share Barcode")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Barcode", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TechSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = AppTheme.colors.surfaceElevated,
        modifier = modifier.fillMaxWidth().height(48.dp).border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            options.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { onSelect(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else AppTheme.colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DedicatedHashGeneratorView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }
    var tabIndex by remember { mutableIntStateOf(0) }

    // Generate Hash State
    var textInput by remember { mutableStateOf("The quick brown fox jumps over the lazy dog") }
    var selectedAlgorithm by remember { mutableStateOf("SHA-256") }

    val computedHash = remember(textInput, selectedAlgorithm) {
        runCatching { devEngine.hashString(textInput, selectedAlgorithm) }.getOrDefault("")
    }

    // Reverse / Lookup Hash State
    var reverseInput by remember { mutableStateOf("5f4dcc3b5aa765d61d8327deb882cf99") } // sample MD5 for 'password'
    var reverseResult by remember { mutableStateOf<HashReverseResult?>(null) }
    var hasSearchedReverse by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechSegmentedControl(
            options = listOf("Generate Hash", "Reverse / Lookup Hash"),
            selectedIndex = tabIndex,
            onSelect = { tabIndex = it }
        )

        if (tabIndex == 0) {
            // === GENERATE HASH MODE ===
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf("SHA-256", "SHA-512", "MD5", "SHA-1").forEach { algo ->
                    FilterChip(
                        selected = selectedAlgorithm == algo,
                        onClick = { selectedAlgorithm = algo },
                        label = { Text(algo, maxLines = 1) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Input Text or Password") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) textInput = clip
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Paste", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { textInput = "" },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("$selectedAlgorithm Digest Output:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        IconButton(onClick = { copyAction(computedHash) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(
                        text = computedHash,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = {
                            reverseInput = computedHash
                            tabIndex = 1
                            reverseResult = devEngine.reverseHash(computedHash)
                            hasSearchedReverse = true
                        },
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = obsidianButtonColors()
                    ) {
                        Icon(Icons.Default.FindInPage, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Test Reversing This Hash in Lookup Engine", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        } else {
            // === REVERSE / LOOKUP HASH MODE ===
            OutlinedTextField(
                value = reverseInput,
                onValueChange = {
                    reverseInput = it
                    hasSearchedReverse = false
                },
                label = { Text("Paste MD5, SHA-1, or SHA-256 Hash") },
                placeholder = { Text("e.g. 5f4dcc3b5aa765d61d8327deb882cf99") },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) {
                            reverseInput = clip.trim()
                            hasSearchedReverse = false
                        }
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Paste Hash", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        reverseInput = ""
                        reverseResult = null
                        hasSearchedReverse = false
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            }

            // Quick Samples
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "MD5 'password'" to "5f4dcc3b5aa765d61d8327deb882cf99",
                    "MD5 'admin'" to "21232f297a57a5a743894a0e4a801fc3",
                    "SHA-256 '123456'" to "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                    "SHA-1 'root'" to "dc76e9f0c0006e8f919e0c515c66dbba3982f785"
                ).forEach { (label, hashVal) ->
                    AssistChip(
                        onClick = {
                            reverseInput = hashVal
                            reverseResult = devEngine.reverseHash(hashVal)
                            hasSearchedReverse = true
                        },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Button(
                onClick = {
                    reverseResult = devEngine.reverseHash(reverseInput)
                    hasSearchedReverse = true
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reverse & Decrypt Hash", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (hasSearchedReverse) {
                reverseResult?.let { res ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = BentoEmerald.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BentoEmerald, modifier = Modifier.size(20.dp))
                                    Text("Plaintext Found (${res.algorithm})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoEmerald)
                                }
                                IconButton(onClick = { copyAction(res.plainText) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Plaintext", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = res.plainText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = AppTheme.colors.textPrimary
                            )
                            HorizontalDivider(color = BentoEmerald.copy(alpha = 0.2f))
                            Text("Source: ${res.matchType}", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                        }
                    }
                } ?: run {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = AppTheme.colors.cardSurface,
                        modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("No Dictionary Match Found", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppTheme.colors.textPrimary)
                            }
                            Text(
                                "Cryptographic hash algorithms (MD5, SHA-1, SHA-256) are mathematically one-way functions designed not to be reversed. This digest is not present in our high-frequency offline dictionary or common 4-digit PIN table.",
                                fontSize = 12.sp,
                                color = AppTheme.colors.textSecondary,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedBase64CodecView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    var inputText by remember { mutableStateOf("ONE Utility Offline System") }
    var isEncodeMode by remember { mutableStateOf(true) }

    var decodeError by remember { mutableStateOf<String?>(null) }
    val outputText = remember(inputText, isEncodeMode) {
        if (isEncodeMode) {
            decodeError = null
            devEngine.base64Encode(inputText)
        } else {
            if (inputText.isBlank()) {
                decodeError = null
                ""
            } else {
                runCatching {
                    val res = devEngine.base64Decode(inputText.trim())
                    decodeError = null
                    res
                }.getOrElse {
                    decodeError = "Invalid Base64 string: Contains non-base64 characters or improper padding"
                    ""
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechSegmentedControl(
            options = listOf("Encode to Base64", "Decode from Base64"),
            selectedIndex = if (isEncodeMode) 0 else 1,
            onSelect = { isEncodeMode = it == 0 }
        )

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(if (isEncodeMode) "Plaintext String" else "Base64 Encoded String") },
            placeholder = { Text(if (isEncodeMode) "Type or paste plaintext to encode..." else "Paste Base64 string to decode...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clip.isNullOrBlank()) inputText = clip
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Paste", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (outputText.isNotBlank() && decodeError == null) {
                        inputText = outputText
                        isEncodeMode = !isEncodeMode
                    }
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Swap", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { inputText = "" },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear", color = Color.White, fontSize = 12.sp)
            }
        }

        decodeError?.let { err ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isEncodeMode) "Base64 Encoded Result:" else "Decoded Plaintext Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(outputText) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = outputText.ifEmpty { "(No output)" },
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun DedicatedAesEncryptionView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    var plainText by remember { mutableStateOf("Top Secret Note: Meeting at midnight") }
    var password by remember { mutableStateOf("ObsidianGuard#2026") }
    var cipherText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = plainText,
            onValueChange = { plainText = it },
            label = { Text("Secret Text to Encrypt") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Master Encryption Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = {
                cipherText = devEngine.aesEncrypt(plainText, password)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Encrypt with AES-256-CBC", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (cipherText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Encrypted Ciphertext (Base64):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        IconButton(onClick = { copyAction(cipherText) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(cipherText, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = BentoEmerald)
                }
            }
        }
    }
}

@Composable
fun DedicatedAesDecryptionView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    var cipherInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var decryptedText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        val context = LocalContext.current
        val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

        OutlinedTextField(
            value = cipherInput,
            onValueChange = { cipherInput = it },
            label = { Text("Ciphertext (Base64)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clip.isNullOrBlank()) cipherInput = clip.trim()
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Paste", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    cipherInput = ""
                    decryptedText = ""
                    errorMessage = null
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear", color = Color.White, fontSize = 12.sp)
            }
        }

        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Decryption Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = {
                val res = devEngine.aesDecrypt(cipherInput.trim(), passwordInput)
                if (res.startsWith("Decryption failed")) {
                    errorMessage = res
                    decryptedText = ""
                } else {
                    decryptedText = res
                    errorMessage = null
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Decrypt Note", color = Color.White, fontWeight = FontWeight.Bold)
        }

        errorMessage?.let { err ->
            Text(err, color = MaterialTheme.colorScheme.error, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        if (decryptedText.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BentoEmerald.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().border(1.dp, BentoEmerald.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Decrypted Secret Note:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        IconButton(onClick = { copyAction(decryptedText) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(decryptedText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
fun DedicatedJsonFormatterView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    var jsonInput by remember { mutableStateOf("{\"app\":\"ONE\",\"version\":1.0,\"offline\":true,\"features\":[\"pdf\",\"image\",\"crypto\"]}") }
    var formattedOutput by remember { mutableStateOf("") }

    LaunchedEffect(jsonInput) {
        formattedOutput = devEngine.formatJson(jsonInput, 2)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = jsonInput,
            onValueChange = { jsonInput = it },
            label = { Text("Raw JSON Input") },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { formattedOutput = devEngine.formatJson(jsonInput, 2) },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Text("Beautify (2 Spaces)", color = Color.White)
            }

            Button(
                onClick = { formattedOutput = devEngine.minifyJson(jsonInput) },
                modifier = Modifier.weight(1f).height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Text("Minify JSON", color = Color.White)
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Formatted JSON Output:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(formattedOutput) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(formattedOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
            }
        }
    }
}

@Composable
fun DedicatedPassphraseGenView(copyAction: (String) -> Unit) {
    val pwdEngine = remember { PasswordGeneratorEngine() }
    var wordCount by remember { mutableFloatStateOf(4f) }
    var currentPassphrase by remember { mutableStateOf(pwdEngine.generatePassphrase(4)) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Words: ${wordCount.toInt()}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        }
        Slider(
            value = wordCount,
            onValueChange = {
                wordCount = it
                currentPassphrase = pwdEngine.generatePassphrase(it.toInt())
            },
            valueRange = 3f..8f,
            steps = 4
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Memorable Diceware Passphrase:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(currentPassphrase) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(currentPassphrase, fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = { currentPassphrase = pwdEngine.generatePassphrase(wordCount.toInt()) },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Generate New Passphrase", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DedicatedColorPickerView(copyAction: (String) -> Unit) {
    var r by remember { mutableFloatStateOf(79f) }
    var g by remember { mutableFloatStateOf(70f) }
    var b by remember { mutableFloatStateOf(229f) }

    val currentColor = Color(r.toInt(), g.toInt(), b.toInt())
    val hexString = String.format(Locale.US, "#%02X%02X%02X", r.toInt(), g.toInt(), b.toInt())
    val rgbString = "rgb(${r.toInt()}, ${g.toInt()}, ${b.toInt()})"

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(currentColor)
                .border(2.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                hexString,
                color = if (r * 0.299 + g * 0.587 + b * 0.114 > 150) Color.Black else Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Red: ${r.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                Slider(value = r, onValueChange = { r = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color(0xFFEF4444), activeTrackColor = Color(0xFFEF4444)))

                Text("Green: ${g.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                Slider(value = g, onValueChange = { g = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981)))

                Text("Blue: ${b.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                Slider(value = b, onValueChange = { b = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color(0xFF3B82F6), activeTrackColor = Color(0xFF3B82F6)))

                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("HEX: $hexString", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
                    IconButton(onClick = { copyAction(hexString) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy HEX", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("RGB: $rgbString", fontWeight = FontWeight.Bold, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(rgbString) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy RGB", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedColorConverterView(copyAction: (String) -> Unit) {
    DedicatedColorPickerView(copyAction = copyAction)
}

@Composable
fun DedicatedColorPaletteGeneratorView(copyAction: (String) -> Unit) {
    var baseHex by remember { mutableStateOf("#4F46E5") }
    val devEngine = remember { DeveloperToolsEngine() }
    val details = remember(baseHex) { devEngine.parseColor(baseHex) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = baseHex,
            onValueChange = { baseHex = it },
            label = { Text("Base Color HEX") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        details?.let { d ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Harmonious Color Palette:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)

                    Row(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(d.hex, d.complementaryHex, "#10B981", "#F59E0B", "#EF4444").forEach { hex ->
                            val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color)
                                    .clickable { copyAction(hex) }
                            )
                        }
                    }

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base: ${d.hex}", fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                        Text("Complementary: ${d.complementaryHex}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoEmerald)
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedContrastCheckerView() {
    var textColorHex by remember { mutableStateOf("#FFFFFF") }
    var bgColorHex by remember { mutableStateOf("#15151B") }
    val devEngine = remember { DeveloperToolsEngine() }

    val textParsed = runCatching { devEngine.parseColor(textColorHex) }.getOrNull()
    val bgParsed = runCatching { devEngine.parseColor(bgColorHex) }.getOrNull()

    val contrastRatio = remember(textParsed, bgParsed) {
        if (textParsed != null && bgParsed != null) {
            val l1 = (textParsed.l / 100.0)
            val l2 = (bgParsed.l / 100.0)
            val maxL = maxOf(l1, l2)
            val minL = minOf(l1, l2)
            String.format(Locale.US, "%.1f", (maxL + 0.05) / (minL + 0.05))
        } else "4.5"
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = textColorHex, onValueChange = { textColorHex = it }, label = { Text("Text Color (HEX)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = bgColorHex, onValueChange = { bgColorHex = it }, label = { Text("Background (HEX)") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("WCAG Contrast Ratio:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                Text("$contrastRatio : 1", fontSize = 36.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    val ratioNum = contrastRatio.toDoubleOrNull() ?: 4.5
                    Surface(shape = RoundedCornerShape(8.dp), color = if (ratioNum >= 4.5) BentoEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f), modifier = Modifier.weight(1f)) {
                        Text("AA Normal: ${if (ratioNum >= 4.5) "PASS" else "FAIL"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (ratioNum >= 4.5) BentoEmerald else MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = if (ratioNum >= 7.0) BentoEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f), modifier = Modifier.weight(1f)) {
                        Text("AAA Enhanced: ${if (ratioNum >= 7.0) "PASS" else "FAIL"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (ratioNum >= 7.0) BentoEmerald else MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedIpInfoView(copyAction: (String) -> Unit) {
    val interfaces = remember {
        val list = mutableListOf<Pair<String, String>>()
        runCatching {
            val nics = java.net.NetworkInterface.getNetworkInterfaces()
            while (nics.hasMoreElements()) {
                val nic = nics.nextElement()
                val addrs = nic.inetAddresses
                while (addrs.hasMoreElements()) {
                    val addr = addrs.nextElement()
                    if (!addr.isLoopbackAddress && addr.hostAddress?.contains(':') == false) {
                        list.add(nic.displayName to (addr.hostAddress ?: "Unknown"))
                    }
                }
            }
        }
        if (list.isEmpty()) list.add("Local Interface" to "127.0.0.1 (Loopback)")
        list
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Local Device Network Interfaces:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                interfaces.forEach { (name, ip) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                            Text(ip, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                        }
                        IconButton(onClick = { copyAction(ip) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy IP", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedDnsLookupView(copyAction: (String) -> Unit) {
    var hostInput by remember { mutableStateOf("google.com") }
    var resolvedIps by remember { mutableStateOf<List<String>>(emptyList()) }
    var isResolving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Hostname to Resolve (e.g. google.com)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    isResolving = true
                    errorMsg = null
                    withContext(Dispatchers.IO) {
                        runCatching {
                            val addrs = java.net.InetAddress.getAllByName(hostInput.trim())
                            resolvedIps = addrs.map { it.hostAddress ?: "" }
                        }.onFailure {
                            errorMsg = it.message ?: "Failed to resolve host"
                            resolvedIps = emptyList()
                        }
                    }
                    isResolving = false
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            if (isResolving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            else {
                Icon(Icons.Default.Dns, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Resolve DNS", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        if (resolvedIps.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resolved IP Addresses (${resolvedIps.size}):", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                    resolvedIps.forEach { ip ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(ip, fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = BentoEmerald, fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { copyAction(ip) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

@Composable
fun DedicatedHttpStatusView() {
    var searchQuery by remember { mutableStateOf("") }
    val codes = remember {
        listOf(
            "200" to "OK: Standard response for successful HTTP requests.",
            "201" to "Created: Request fulfilled, new resource created.",
            "204" to "No Content: Successfully processed request, no content returned.",
            "301" to "Moved Permanently: Resource URL changed permanently.",
            "302" to "Found: Temporary redirect to another URI.",
            "304" to "Not Modified: Resource cached and unchanged.",
            "400" to "Bad Request: Server cannot process due to client syntax error.",
            "401" to "Unauthorized: Authentication required to access resource.",
            "403" to "Forbidden: Server understood request but refuses authorization.",
            "404" to "Not Found: Requested resource could not be located.",
            "405" to "Method Not Allowed: HTTP method not supported for resource.",
            "429" to "Too Many Requests: Rate limiting quota exceeded.",
            "500" to "Internal Server Error: Generic server-side exception.",
            "502" to "Bad Gateway: Invalid response received from upstream server.",
            "503" to "Service Unavailable: Server overloaded or down for maintenance.",
            "504" to "Gateway Timeout: Upstream server failed to respond in time."
        )
    }

    val filtered = codes.filter { (code, desc) ->
        searchQuery.isEmpty() || code.contains(searchQuery) || desc.contains(searchQuery, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Status Codes (e.g. 404, Gateway)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        filtered.forEach { (code, desc) ->
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(14.dp))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    val badgeColor = when {
                        code.startsWith("2") -> BentoEmerald
                        code.startsWith("3") -> Color(0xFF3B82F6)
                        code.startsWith("4") -> Color(0xFFF59E0B)
                        else -> MaterialTheme.colorScheme.error
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = badgeColor.copy(alpha = 0.15f)) {
                        Text(code, fontWeight = FontWeight.Black, fontSize = 16.sp, color = badgeColor, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(desc, fontSize = 12.sp, color = AppTheme.colors.textPrimary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DedicatedUserAgentView(copyAction: (String) -> Unit) {
    val ua = remember {
        val osVersion = android.os.Build.VERSION.RELEASE
        val model = android.os.Build.MODEL
        val id = android.os.Build.ID
        "Mozilla/5.0 (Linux; Android $osVersion; $model Build/$id; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/120.0.0.0 Mobile Safari/537.36"
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Device User Agent String:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(ua) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UA", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(ua, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
fun DedicatedUrlParserView() {
    var urlInput by remember { mutableStateOf("https://example.com:8080/api/v1/search?query=android&limit=25#top") }
    val uri = runCatching { java.net.URI(urlInput.trim()) }.getOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Input URL to Parse") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        uri?.let { u ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Parsed URL Components:", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AppTheme.colors.textPrimary)
                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    listOf(
                        "Scheme / Protocol" to (u.scheme ?: "None"),
                        "Host" to (u.host ?: "None"),
                        "Port" to (if (u.port != -1) u.port.toString() else "Default (80/443)"),
                        "Path" to (u.path.ifEmpty { "/" }),
                        "Query" to (u.query ?: "None"),
                        "Fragment / Hash" to (u.fragment ?: "None")
                    ).forEach { (label, value) ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(label, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                            Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedUrlCodecView(copyAction: (String) -> Unit) {
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    var rawText by remember { mutableStateOf("https://example.com/search?q=One Utility & App 2026") }
    var isEncode by remember { mutableStateOf(true) }
    var decodeError by remember { mutableStateOf<String?>(null) }

    val result = remember(rawText, isEncode) {
        if (isEncode) {
            decodeError = null
            runCatching { java.net.URLEncoder.encode(rawText, "UTF-8") }.getOrDefault("")
        } else {
            if (rawText.isBlank()) {
                decodeError = null
                ""
            } else {
                runCatching {
                    val decoded = java.net.URLDecoder.decode(rawText.trim(), "UTF-8")
                    decodeError = null
                    decoded
                }.getOrElse {
                    decodeError = "Malformed URL percent-encoding: Invalid % sequence or charset"
                    ""
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechSegmentedControl(
            options = listOf("URL Encode", "URL Decode"),
            selectedIndex = if (isEncode) 0 else 1,
            onSelect = { isEncode = it == 0 }
        )

        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            label = { Text(if (isEncode) "Text / URL to Encode" else "Encoded URL to Decode") },
            placeholder = { Text(if (isEncode) "Enter URL or query parameters to encode..." else "Paste %20 encoded URL string to decode...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                    if (!clip.isNullOrBlank()) rawText = clip
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Paste", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    if (result.isNotBlank() && decodeError == null) {
                        rawText = result
                        isEncode = !isEncode
                    }
                },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Swap", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = { rawText = "" },
                modifier = Modifier.weight(1f).height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Clear", color = Color.White, fontSize = 12.sp)
            }
        }

        decodeError?.let { err ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isEncode) "URL Encoded Output:" else "Decoded Plaintext Output:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result.ifEmpty { "(No output)" }, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedJwtDecoderView(copyAction: (String) -> Unit) {
    var tokenInput by remember { mutableStateOf("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c") }
    val devEngine = remember { DeveloperToolsEngine() }
    val payload = runCatching { devEngine.decodeJwt(tokenInput) }.getOrNull()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = tokenInput,
            onValueChange = { tokenInput = it },
            label = { Text("JWT Token") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(14.dp)
        )

        payload?.let { p ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Header JSON:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFEF4444))
                    Text(p.headerJson, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    Text("Payload (Claims) JSON:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    Text(p.bodyJson, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                }
            }
        }
    }
}

@Composable
fun DedicatedRegexTesterView() {
    var pattern by remember { mutableStateOf("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") }
    var testText by remember { mutableStateOf("Contact support@oneapp.com or sales@company.org for details.") }

    val regex = runCatching { Regex(pattern) }.getOrNull()
    val matches = remember(regex, testText) {
        regex?.findAll(testText)?.map { it.value }?.toList() ?: emptyList()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = pattern,
            onValueChange = { pattern = it },
            label = { Text("Regular Expression Pattern") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = testText,
            onValueChange = { testText = it },
            label = { Text("Test String") },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Matches Found: ${matches.size}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (matches.isNotEmpty()) BentoEmerald else AppTheme.colors.textSecondary)
                matches.forEach { m ->
                    Surface(shape = RoundedCornerShape(8.dp), color = BentoEmerald.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Text(m, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = BentoEmerald, modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedRandomNumberView(copyAction: (String) -> Unit) {
    var minVal by remember { mutableStateOf("1") }
    var maxVal by remember { mutableStateOf("100") }
    var count by remember { mutableStateOf("5") }
    var generatedNumbers by remember { mutableStateOf<List<Int>>(emptyList()) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(value = minVal, onValueChange = { minVal = it }, label = { Text("Min") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = maxVal, onValueChange = { maxVal = it }, label = { Text("Max") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = count, onValueChange = { count = it }, label = { Text("Count") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp))
        }

        Button(
            onClick = {
                val min = minVal.toIntOrNull() ?: 1
                val max = maxVal.toIntOrNull() ?: 100
                val c = count.toIntOrNull()?.coerceIn(1, 100) ?: 5
                if (max >= min) {
                    val rng = java.util.Random()
                    generatedNumbers = (1..c).map { rng.nextInt((max - min) + 1) + min }
                }
            },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Casino, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Generate Random Numbers", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (generatedNumbers.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fmt = generatedNumbers.joinToString(", ")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Results:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        IconButton(onClick = { copyAction(fmt) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(fmt, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun DedicatedRandomStringView(copyAction: (String) -> Unit) {
    var length by remember { mutableFloatStateOf(16f) }
    var generatedStr by remember { mutableStateOf("") }

    fun generate() {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*"
        val rng = java.util.Random()
        generatedStr = (1..length.toInt()).map { chars[rng.nextInt(chars.length)] }.joinToString("")
    }

    LaunchedEffect(Unit) { generate() }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Length: ${length.toInt()}", fontWeight = FontWeight.Bold, color = AppTheme.colors.textPrimary)
        Slider(value = length, onValueChange = { length = it; generate() }, valueRange = 6f..64f)

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Random String:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(generatedStr) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(generatedStr, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Button(
            onClick = { generate() },
            modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
            shape = RoundedCornerShape(12.dp),
            colors = obsidianButtonColors()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(6.dp))
            Text("Regenerate String", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DedicatedPasswordStrengthView() {
    var password by remember { mutableStateOf("") }

    val lengthScore = (password.length * 4).coerceAtMost(30)
    val upperScore = if (password.any { it.isUpperCase() }) 15 else 0
    val lowerScore = if (password.any { it.isLowerCase() }) 15 else 0
    val digitScore = if (password.any { it.isDigit() }) 20 else 0
    val symbolScore = if (password.any { !it.isLetterOrDigit() }) 20 else 0
    val totalScore = (lengthScore + upperScore + lowerScore + digitScore + symbolScore).coerceIn(0, 100)

    val (label, barColor) = when {
        totalScore < 30 -> "Weak" to Color(0xFFEF4444)
        totalScore < 60 -> "Fair" to Color(0xFFF59E0B)
        totalScore < 80 -> "Strong" to Color(0xFF10B981)
        else -> "Very Strong" to Color(0xFF059669)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password to Test Strength") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Entropy Score: $totalScore / 100 ($label)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = barColor)
                LinearProgressIndicator(
                    progress = { totalScore / 100f },
                    color = barColor,
                    trackColor = AppTheme.colors.surfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )

                HorizontalDivider(color = AppTheme.colors.borderSubtle)

                listOf(
                    "Length (8+ characters)" to (password.length >= 8),
                    "Uppercase Letters" to password.any { it.isUpperCase() },
                    "Lowercase Letters" to password.any { it.isLowerCase() },
                    "Numbers (0-9)" to password.any { it.isDigit() },
                    "Symbols (!@#$)" to password.any { !it.isLetterOrDigit() }
                ).forEach { (crit, ok) ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(crit, fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        Text(if (ok) "✓ Pass" else "✗ Missing", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (ok) BentoEmerald else MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun DedicatedUuidView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    val context = LocalContext.current
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    var tabIndex by remember { mutableIntStateOf(0) }

    // Generate Tab State
    var generatedUuid by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var isUppercase by remember { mutableStateOf(false) }

    val displayUuid = if (isUppercase) generatedUuid.uppercase(Locale.US) else generatedUuid.lowercase(Locale.US)

    // Inspect & Reverse Tab State
    var inspectInput by remember { mutableStateOf(generatedUuid) }
    val parsedDetails = remember(inspectInput) {
        if (inspectInput.isBlank()) null else devEngine.parseUuid(inspectInput)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TechSegmentedControl(
            options = listOf("Generate UUID", "Inspect & Reverse UUID"),
            selectedIndex = tabIndex,
            onSelect = { tabIndex = it }
        )

        if (tabIndex == 0) {
            // === GENERATE TAB ===
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = AppTheme.colors.cardSurface,
                modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("UUID Version 4 (RFC 4122):", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                        IconButton(onClick = { copyAction(displayUuid) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy UUID", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = displayUuid,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(color = AppTheme.colors.borderSubtle)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Uppercase Format", fontSize = 13.sp, color = AppTheme.colors.textPrimary)
                        Switch(
                            checked = isUppercase,
                            onCheckedChange = { isUppercase = it }
                        )
                    }
                }
            }

            Button(
                onClick = { generatedUuid = UUID.randomUUID().toString() },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Generate New UUID v4", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    inspectInput = displayUuid
                    tabIndex = 1
                },
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp),
                colors = obsidianButtonColors()
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Inspect & Reverse This UUID", color = Color.White, fontSize = 12.sp)
            }
        } else {
            // === INSPECT & REVERSE TAB ===
            OutlinedTextField(
                value = inspectInput,
                onValueChange = { inspectInput = it },
                label = { Text("Paste UUID to Inspect & Reverse") },
                placeholder = { Text("e.g. 123e4567-e89b-12d3-a456-426614174000") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) inspectInput = clip.trim()
                    },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Paste", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { inspectInput = UUID.randomUUID().toString() },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Sample v4", color = Color.White, fontSize = 12.sp)
                }

                Button(
                    onClick = { inspectInput = "" },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = obsidianButtonColors()
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear", color = Color.White, fontSize = 12.sp)
                }
            }

            parsedDetails?.let { details ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = AppTheme.colors.cardSurface,
                    modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Reversed & Analyzed Structure:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BentoEmerald)
                            IconButton(onClick = { copyAction(details.rawUuid) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Canonical UUID", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                            }
                        }

                        HorizontalDivider(color = AppTheme.colors.borderSubtle)

                        listOf(
                            "Standard UUID" to details.rawUuid,
                            "Version" to details.versionName,
                            "Variant" to details.variant,
                            "Clock Sequence" to details.clockSequence,
                            "Node / MAC" to details.nodeId,
                            "Extracted Timestamp" to (details.formattedTimestamp ?: "N/A (Non-time-based UUID)")
                        ).forEach { (label, value) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(label, fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = AppTheme.colors.textPrimary)
                            }
                        }

                        HorizontalDivider(color = AppTheme.colors.borderSubtle)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Clean Hex (32 chars):", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                IconButton(onClick = { copyAction(details.hexNoDashes) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Hex", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(details.hexNoDashes, fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Decimal Integer (BigInt):", fontSize = 12.sp, color = AppTheme.colors.textSecondary)
                                IconButton(onClick = { copyAction(details.decimalValue) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Decimal", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(details.decimalValue, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AppTheme.colors.textPrimary)
                        }
                    }
                }
            } ?: run {
                if (inspectInput.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            Text("Invalid UUID string: Expected 32 hex characters with standard 8-4-4-4-12 grouping.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

