package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

enum class QrDotStyle {
    SQUARE,
    ROUNDED_DOTS,
    SQUIRCLE
}

enum class QrCenterIcon {
    NONE,
    PHONE,
    CONTACT,
    LINK,
    WIFI,
    STAR,
    HEART
}

data class QrCustomOptions(
    val style: QrDotStyle = QrDotStyle.SQUARE,
    val darkColor: Int = Color.BLACK,
    val lightColor: Int = Color.WHITE,
    val centerIcon: QrCenterIcon = QrCenterIcon.NONE,
    val sizePixels: Int = 800,
    val margin: Int = 2
)

class QrEngine {

    suspend fun generateQrCode(
        content: String,
        sizePixels: Int = 800,
        darkColor: Int = Color.BLACK,
        lightColor: Int = Color.WHITE
    ): Result<Bitmap> {
        return generateCustomQrCode(
            content = content,
            options = QrCustomOptions(
                style = QrDotStyle.SQUARE,
                darkColor = darkColor,
                lightColor = lightColor,
                sizePixels = sizePixels
            )
        )
    }

    suspend fun generateCustomQrCode(
        content: String,
        options: QrCustomOptions
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
                EncodeHintType.MARGIN to options.margin
            )

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, options.sizePixels, options.sizePixels, hints)

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val bitmap = Bitmap.createBitmap(options.sizePixels, options.sizePixels, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Fill background
            canvas.drawColor(options.lightColor)

            val moduleW = options.sizePixels.toFloat() / matrixWidth
            val moduleH = options.sizePixels.toFloat() / matrixHeight

            val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = options.darkColor
                style = Paint.Style.FILL
            }

            fun isInFinderPattern(x: Int, y: Int): Boolean {
                val inTopLeft = x in 0..7 && y in 0..7
                val inTopRight = x in (matrixWidth - 8) until matrixWidth && y in 0..7
                val inBottomLeft = x in 0..7 && y in (matrixHeight - 8) until matrixHeight
                return inTopLeft || inTopRight || inBottomLeft
            }

            val centerPx = options.sizePixels / 2f
            val badgeRadius = options.sizePixels * 0.12f
            val hasCenterIcon = options.centerIcon != QrCenterIcon.NONE

            // Draw QR modules
            for (x in 0 until matrixWidth) {
                for (y in 0 until matrixHeight) {
                    if (bitMatrix[x, y]) {
                        val left = x * moduleW
                        val top = y * moduleH
                        val right = (x + 1) * moduleW
                        val bottom = (y + 1) * moduleH
                        val cx = (left + right) / 2f
                        val cy = (top + bottom) / 2f

                        // If center icon is present, don't draw dark modules inside the central badge area
                        if (hasCenterIcon) {
                            val dist = Math.hypot((cx - centerPx).toDouble(), (cy - centerPx).toDouble()).toFloat()
                            if (dist < badgeRadius * 0.95f) {
                                continue
                            }
                        }

                        if (isInFinderPattern(x, y)) {
                            // Draw finder pattern with crisp shape to ensure instant scanner lock
                            if (options.style == QrDotStyle.SQUIRCLE || options.style == QrDotStyle.ROUNDED_DOTS) {
                                canvas.drawRoundRect(RectF(left, top, right, bottom), moduleW * 0.25f, moduleH * 0.25f, darkPaint)
                            } else {
                                canvas.drawRect(left, top, right, bottom, darkPaint)
                            }
                        } else {
                            when (options.style) {
                                QrDotStyle.ROUNDED_DOTS -> {
                                    canvas.drawCircle(cx, cy, (moduleW / 2f) * 0.92f, darkPaint)
                                }
                                QrDotStyle.SQUIRCLE -> {
                                    val padX = moduleW * 0.08f
                                    val padY = moduleH * 0.08f
                                    canvas.drawRoundRect(
                                        RectF(left + padX, top + padY, right - padX, bottom - padY),
                                        moduleW * 0.35f,
                                        moduleH * 0.35f,
                                        darkPaint
                                    )
                                }
                                QrDotStyle.SQUARE -> {
                                    canvas.drawRect(left, top, right, bottom, darkPaint)
                                }
                            }
                        }
                    }
                }
            }

            // Draw center badge and icon if selected
            if (hasCenterIcon) {
                drawCenterIcon(
                    canvas = canvas,
                    icon = options.centerIcon,
                    cx = centerPx,
                    cy = centerPx,
                    badgeRadius = badgeRadius,
                    bgColor = options.lightColor,
                    fgColor = options.darkColor
                )
            }

            bitmap
        }
    }

    private fun drawCenterIcon(
        canvas: Canvas,
        icon: QrCenterIcon,
        cx: Float,
        cy: Float,
        badgeRadius: Float,
        bgColor: Int,
        fgColor: Int
    ) {
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.STROKE
            strokeWidth = badgeRadius * 0.12f
        }

        // Draw protective circular badge
        canvas.drawCircle(cx, cy, badgeRadius, bgPaint)
        canvas.drawCircle(cx, cy, badgeRadius, strokePaint)

        val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.FILL
        }
        val iconStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fgColor
            style = Paint.Style.STROKE
            strokeWidth = badgeRadius * 0.16f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val r = badgeRadius * 0.52f
        when (icon) {
            QrCenterIcon.PHONE -> {
                val path = Path().apply {
                    moveTo(cx - r * 0.5f, cy - r * 0.6f)
                    cubicTo(cx - r * 0.6f, cy - r * 0.2f, cx - r * 0.2f, cy + r * 0.6f, cx + r * 0.6f, cy + r * 0.5f)
                    lineTo(cx + r * 0.7f, cy + r * 0.2f)
                    lineTo(cx + r * 0.35f, cy + r * 0.1f)
                    lineTo(cx + r * 0.2f, cy + r * 0.25f)
                    cubicTo(cx, cy + r * 0.1f, cx - r * 0.1f, cy, cx - r * 0.25f, cy - r * 0.2f)
                    lineTo(cx - r * 0.1f, cy - r * 0.35f)
                    lineTo(cx - r * 0.2f, cy - r * 0.7f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            QrCenterIcon.CONTACT -> {
                canvas.drawCircle(cx, cy - r * 0.35f, r * 0.35f, iconPaint)
                val bodyRect = RectF(cx - r * 0.65f, cy + r * 0.05f, cx + r * 0.65f, cy + r * 0.85f)
                canvas.drawArc(bodyRect, 180f, 180f, true, iconPaint)
            }
            QrCenterIcon.LINK -> {
                val linkRect1 = RectF(cx - r * 0.6f, cy - r * 0.4f, cx + r * 0.1f, cy + r * 0.4f)
                val linkRect2 = RectF(cx - r * 0.1f, cy - r * 0.4f, cx + r * 0.6f, cy + r * 0.4f)
                canvas.drawRoundRect(linkRect1, r * 0.3f, r * 0.3f, iconStroke)
                canvas.drawRoundRect(linkRect2, r * 0.3f, r * 0.3f, iconStroke)
            }
            QrCenterIcon.WIFI -> {
                canvas.drawCircle(cx, cy + r * 0.45f, r * 0.18f, iconPaint)
                val arc1 = RectF(cx - r * 0.5f, cy - r * 0.1f, cx + r * 0.5f, cy + r * 0.9f)
                canvas.drawArc(arc1, 205f, 130f, false, iconStroke)
                val arc2 = RectF(cx - r * 0.85f, cy - r * 0.5f, cx + r * 0.85f, cy + r * 1.2f)
                canvas.drawArc(arc2, 210f, 120f, false, iconStroke)
            }
            QrCenterIcon.STAR -> {
                val path = Path()
                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) r else r * 0.42f
                    val angle = Math.toRadians((i * 36 - 90).toDouble())
                    val px = (cx + radius * Math.cos(angle)).toFloat()
                    val py = (cy + radius * Math.sin(angle)).toFloat()
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
                canvas.drawPath(path, iconPaint)
            }
            QrCenterIcon.HEART -> {
                val path = Path().apply {
                    moveTo(cx, cy + r * 0.7f)
                    cubicTo(cx - r, cy + r * 0.1f, cx - r * 0.8f, cy - r * 0.6f, cx, cy - r * 0.2f)
                    cubicTo(cx + r * 0.8f, cy - r * 0.6f, cx + r, cy + r * 0.1f, cx, cy + r * 0.7f)
                    close()
                }
                canvas.drawPath(path, iconPaint)
            }
            QrCenterIcon.NONE -> {}
        }
    }

    fun cleanPhoneNumber(phone: String): String {
        val trimmed = phone.trim()
        val hasPlus = trimmed.startsWith("+")
        val digits = trimmed.filter { it.isDigit() }
        return if (hasPlus) "+$digits" else digits
    }

    fun buildPhonePayload(phone: String): String {
        val clean = cleanPhoneNumber(phone)
        return "tel:$clean"
    }

    fun buildSmsPayload(phone: String, message: String = ""): String {
        val clean = cleanPhoneNumber(phone)
        return if (message.isNotBlank()) "smsto:$clean:$message" else "smsto:$clean"
    }

    fun buildContactVCard(
        name: String,
        phone: String,
        email: String = "",
        org: String = "",
        title: String = "",
        note: String = ""
    ): String {
        val cleanPhone = cleanPhoneNumber(phone)
        val nameParts = name.trim().split("\\s+".toRegex())
        val lastName = if (nameParts.size > 1) nameParts.last() else ""
        val firstName = if (nameParts.size > 1) nameParts.dropLast(1).joinToString(" ") else name.trim()

        // Strict CRLF (\r\n) per RFC 2426 / RFC 6350 is required for camera scanner compatibility!
        return buildString {
            append("BEGIN:VCARD\r\n")
            append("VERSION:3.0\r\n")
            append("N:$lastName;$firstName;;;\r\n")
            append("FN:${name.trim()}\r\n")
            if (org.isNotBlank()) append("ORG:${org.trim()}\r\n")
            if (title.isNotBlank()) append("TITLE:${title.trim()}\r\n")
            if (cleanPhone.isNotBlank()) {
                append("TEL;TYPE=CELL,VOICE:$cleanPhone\r\n")
            }
            if (email.isNotBlank()) {
                append("EMAIL;TYPE=INTERNET:${email.trim()}\r\n")
            }
            if (note.isNotBlank()) {
                append("NOTE:${note.trim()}\r\n")
            }
            append("END:VCARD\r\n")
        }
    }

    fun buildWifiPayload(ssid: String, password: String, encryption: String = "WPA"): String {
        return "WIFI:T:$encryption;S:$ssid;P:$password;;"
    }

    fun buildEmailPayload(email: String, subject: String = "", body: String = ""): String {
        val qParams = mutableListOf<String>()
        if (subject.isNotEmpty()) qParams.add("subject=${URLEncoder.encode(subject, "UTF-8")}")
        if (body.isNotEmpty()) qParams.add("body=${URLEncoder.encode(body, "UTF-8")}")
        val query = if (qParams.isNotEmpty()) "?${qParams.joinToString("&")}" else ""
        return "mailto:$email$query"
    }

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
