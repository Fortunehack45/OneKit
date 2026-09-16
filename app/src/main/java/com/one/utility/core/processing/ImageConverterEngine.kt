package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ImageFormat(val extension: String, val mimeType: String, val compressFormat: Bitmap.CompressFormat) {
    JPEG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
    PNG("png", "image/png", Bitmap.CompressFormat.PNG),
    WEBP("webp", "image/webp", Bitmap.CompressFormat.WEBP)
}

data class ConversionResult(
    val outputFile: File,
    val format: ImageFormat,
    val sizeBytes: Long
)

class ImageConverterEngine(private val context: Context) {

    /**
     * Converts an image to the target format (JPEG, PNG, WEBP).
     * If converting transparent PNG to JPEG, fills background with solid white
     * to prevent black background artifacts.
     */
    suspend fun convertImage(
        inputUri: Uri,
        targetFormat: ImageFormat,
        quality: Int = 90,
        outputFile: File
    ): Result<ConversionResult> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: throw IllegalArgumentException("Could not decode source image")

            val finalBitmap = if (targetFormat == ImageFormat.JPEG && bitmap.hasAlpha()) {
                // Flatten alpha onto a clean white canvas
                val solidBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(solidBitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                bitmap.recycle()
                solidBitmap
            } else {
                bitmap
            }

            FileOutputStream(outputFile).use { out ->
                finalBitmap.compress(targetFormat.compressFormat, quality.coerceIn(10, 100), out)
            }

            val size = outputFile.length()
            finalBitmap.recycle()

            ConversionResult(
                outputFile = outputFile,
                format = targetFormat,
                sizeBytes = size
            )
        }
    }
}
