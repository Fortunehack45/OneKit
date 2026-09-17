package com.one.utility.feature.tools

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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

@Composable
fun DedicatedWifiQrView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val qrEngine = remember { QrEngine() }
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var security by remember { mutableStateOf("WPA") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun updateQr() {
        if (ssid.isNotBlank()) {
            coroutineScope.launch {
                val payload = qrEngine.buildWifiPayload(ssid, password, security)
                qrBitmap = qrEngine.generateQrCode(payload, 600).getOrNull()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it; updateQr() },
            label = { Text("Network Name (SSID)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; updateQr() },
            label = { Text("Wi-Fi Password") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = security == "WPA", onClick = { security = "WPA"; updateQr() }, label = { Text("WPA/WPA2") }, modifier = Modifier.weight(1f))
            FilterChip(selected = security == "WEP", onClick = { security = "WEP"; updateQr() }, label = { Text("WEP") }, modifier = Modifier.weight(1f))
            FilterChip(selected = security == "nopass", onClick = { security = "nopass"; updateQr() }, label = { Text("Open / None") }, modifier = Modifier.weight(1f))
        }

        qrBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "Wi-Fi QR", modifier = Modifier.fillMaxSize().padding(14.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_wifi_qr_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        }
                        shareFile(context, outFile, "image/png", "Share Wi-Fi QR Code")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Wi-Fi QR Code", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DedicatedContactQrView() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val qrEngine = remember { QrEngine() }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    fun updateQr() {
        if (name.isNotBlank() || phone.isNotBlank()) {
            coroutineScope.launch {
                val payload = qrEngine.buildContactVCard(name, phone, email, company)
                qrBitmap = qrEngine.generateQrCode(payload, 600).getOrNull()
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it; updateQr() }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = phone, onValueChange = { phone = it; updateQr() }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = email, onValueChange = { email = it; updateQr() }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = company, onValueChange = { company = it; updateQr() }, label = { Text("Company / Organization") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        qrBitmap?.let { bmp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = bmp.asImageBitmap(), contentDescription = "vCard QR", modifier = Modifier.fillMaxSize().padding(14.dp))
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        val outFile = File(context.cacheDir, "ONE_vcard_qr_${System.currentTimeMillis()}.png")
                        withContext(Dispatchers.IO) {
                            FileOutputStream(outFile).use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }
                        }
                        shareFile(context, outFile, "image/png", "Share vCard QR")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp).pressFeedback(),
                shape = RoundedCornerShape(12.dp),
                colors = accentButtonColors(BentoEmerald)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Export & Share Contact QR", color = Color.White, fontWeight = FontWeight.Bold)
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
fun DedicatedHashGeneratorView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    var textInput by remember { mutableStateOf("The quick brown fox jumps over the lazy dog") }
    var selectedAlgorithm by remember { mutableStateOf("SHA-256") }

    val computedHash = remember(textInput, selectedAlgorithm) {
        runCatching { devEngine.hashString(textInput, selectedAlgorithm) }.getOrDefault("")
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            label = { Text("Input Text or Password") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("SHA-256", "SHA-512", "MD5", "SHA-1").forEach { algo ->
                FilterChip(
                    selected = selectedAlgorithm == algo,
                    onClick = { selectedAlgorithm = algo },
                    label = { Text(algo) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("$selectedAlgorithm Digest:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
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
            }
        }
    }
}

@Composable
fun DedicatedBase64CodecView(copyAction: (String) -> Unit) {
    val devEngine = remember { DeveloperToolsEngine() }
    var inputText by remember { mutableStateOf("ONE Utility Offline System") }
    var isEncodeMode by remember { mutableStateOf(true) }

    val outputText: String = remember(inputText, isEncodeMode) {
        if (isEncodeMode) {
            devEngine.base64Encode(inputText)
        } else {
            runCatching { devEngine.base64Decode(inputText) }.getOrElse { "Invalid Base64 string" }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = isEncodeMode, onClick = { isEncodeMode = true }, label = { Text("Encode to Base64") }, modifier = Modifier.weight(1f))
            FilterChip(selected = !isEncodeMode, onClick = { isEncodeMode = false }, label = { Text("Decode from Base64") }, modifier = Modifier.weight(1f))
        }

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(if (isEncodeMode) "Plaintext String" else "Base64 Encoded String") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Resulting Output:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(outputText) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = outputText,
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
        OutlinedTextField(
            value = cipherInput,
            onValueChange = { cipherInput = it },
            label = { Text("Ciphertext (Base64)") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

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
            colors = accentButtonColors(MaterialTheme.colorScheme.primary)
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
    var rawText by remember { mutableStateOf("https://example.com/search?q=One Utility & App 2026") }
    var isEncode by remember { mutableStateOf(true) }

    val result = remember(rawText, isEncode) {
        runCatching {
            if (isEncode) java.net.URLEncoder.encode(rawText, "UTF-8")
            else java.net.URLDecoder.decode(rawText, "UTF-8")
        }.getOrDefault("")
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = rawText,
            onValueChange = { rawText = it },
            label = { Text(if (isEncode) "Text to URL-Encode" else "URL to Decode") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(14.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = isEncode, onClick = { isEncode = true }, label = { Text("URL Encode") }, modifier = Modifier.weight(1f))
            FilterChip(selected = !isEncode, onClick = { isEncode = false }, label = { Text("URL Decode") }, modifier = Modifier.weight(1f))
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = AppTheme.colors.cardSurface,
            modifier = Modifier.fillMaxWidth().border(1.dp, AppTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Result:", fontSize = 13.sp, color = AppTheme.colors.textSecondary)
                    IconButton(onClick = { copyAction(result) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AppTheme.colors.textPrimary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(result, fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = AppTheme.colors.textPrimary)
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

