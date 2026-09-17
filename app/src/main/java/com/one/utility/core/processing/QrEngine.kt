package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QrEngine {

    suspend fun generateQrCode(
        content: String,
        sizePixels: Int = 800,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to 2
            )

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePixels, sizePixels, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) darkColor else lightColor)
                }
            }

            bitmap
        }
    }

    fun buildWifiPayload(ssid: String, password: String, encryption: String = "WPA"): String {
        return "WIFI:T:$encryption;S:$ssid;P:$password;;"
    }

    fun buildContactVCard(name: String, phone: String, email: String, org: String = ""): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            if (phone.isNotEmpty()) appendLine("TEL:$phone")
            if (email.isNotEmpty()) appendLine("EMAIL:$email")
            if (org.isNotEmpty()) appendLine("ORG:$org")
            append("END:VCARD")
        }
    }

    fun buildEmailPayload(email: String, subject: String = "", body: String = ""): String {
        val qParams = mutableListOf<String>()
        if (subject.isNotEmpty()) qParams.add("subject=${java.net.URLEncoder.encode(subject, "UTF-8")}")
        if (body.isNotEmpty()) qParams.add("body=${java.net.URLEncoder.encode(body, "UTF-8")}")
        val query = if (qParams.isNotEmpty()) "?${qParams.joinToString("&")}" else ""
        return "mailto:$email$query"
    }

    fun buildPhonePayload(phone: String): String = "tel:$phone"

    suspend fun generateBarcode(
        content: String,
        widthPixels: Int = 800,
        heightPixels: Int = 260
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val writer = com.google.zxing.MultiFormatWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, widthPixels, heightPixels)
            val w = bitMatrix.width
            val h = bitMatrix.height
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        }
    }
}
