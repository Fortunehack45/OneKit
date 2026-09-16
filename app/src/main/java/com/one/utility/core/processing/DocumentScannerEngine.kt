package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class DocumentFilterMode(val label: String) {
    ORIGINAL("Original"),
    BW_DOCUMENT("Crisp B&W"),
    GRAYSCALE("Grayscale"),
    ENHANCED("Magic Color")
}

class DocumentScannerEngine {

    suspend fun applyDocumentFilter(
        source: Bitmap,
        filterMode: DocumentFilterMode
    ): Bitmap = withContext(Dispatchers.Default) {
        when (filterMode) {
            DocumentFilterMode.ORIGINAL -> source

            DocumentFilterMode.GRAYSCALE -> {
                val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)
                val paint = Paint()
                val colorMatrix = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                canvas.drawBitmap(source, 0f, 0f, paint)
                output
            }

            DocumentFilterMode.ENHANCED -> {
                // Boost contrast and brightness for readability
                val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)
                val paint = Paint()
                val contrast = 1.3f
                val brightness = 15f
                val colorMatrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, brightness,
                        0f, contrast, 0f, 0f, brightness,
                        0f, 0f, contrast, 0f, brightness,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
                canvas.drawBitmap(source, 0f, 0f, paint)
                output
            }

            DocumentFilterMode.BW_DOCUMENT -> {
                // High-contrast binary document thresholding
                val width = source.width
                val height = source.height
                val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val pixels = IntArray(width * height)
                source.getPixels(pixels, 0, width, 0, 0, width, height)

                // Calculate average luminance
                var totalLuminance = 0L
                for (pixel in pixels) {
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)
                    totalLuminance += (0.299 * r + 0.587 * g + 0.114 * b).toLong()
                }
                val avgLuminance = (totalLuminance / pixels.size).toInt().coerceIn(100, 160)

                for (i in pixels.indices) {
                    val pixel = pixels[i]
                    val lum = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)).toInt()
                    pixels[i] = if (lum > avgLuminance) Color.WHITE else Color.BLACK
                }

                output.setPixels(pixels, 0, width, 0, 0, width, height)
                output
            }
        }
    }
}
