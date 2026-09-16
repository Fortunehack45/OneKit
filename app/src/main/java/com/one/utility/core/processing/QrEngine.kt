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
}
