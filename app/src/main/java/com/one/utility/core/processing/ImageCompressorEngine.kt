package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class CompressionPreset(val displayName: String, val quality: Int, val maxDimension: Int) {
    MAXIMUM("Maximum Quality", 90, 2560),
    HIGH("High (Recommended)", 75, 1920),
    MEDIUM("Medium Compression", 55, 1280),
    SMALL("Small File Size", 35, 800)
}

data class CompressionResult(
    val outputFile: File,
    val originalSizeBytes: Long,
    val compressedSizeBytes: Long,
    val savedPercentage: Double,
    val width: Int,
    val height: Int
)

class ImageCompressorEngine(private val context: Context) {

    suspend fun compressImage(
        inputUri: Uri,
        outputFile: File,
        preset: CompressionPreset = CompressionPreset.HIGH,
        customQuality: Int? = null,
        onProgress: (Float) -> Unit = {}
    ): Result<CompressionResult> = withContext(Dispatchers.IO) {
        runCatching {
            onProgress(0.1f)

            // Measure original file size
            val originalSizeBytes = context.contentResolver.openFileDescriptor(inputUri, "r")?.use {
                it.statSize
            } ?: 0L

            // Read dimensions
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(inputUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            val targetMaxDimension = preset.maxDimension

            // Calculate scale down
            var sampleSize = 1
            val maxOriginal = maxOf(origWidth, origHeight)
            if (maxOriginal > targetMaxDimension) {
                sampleSize = Math.round(maxOriginal.toFloat() / targetMaxDimension.toFloat()).coerceAtLeast(1)
            }

            onProgress(0.4f)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Conserves 50% memory
            }

            val decodedBitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("Could not decode image")

            onProgress(0.7f)

            // Compress to JPEG or WEBP
            val quality = customQuality?.coerceIn(10, 100) ?: preset.quality
            FileOutputStream(outputFile).use { outStream ->
                decodedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream)
            }

            val compressedSizeBytes = outputFile.length()
            val savedBytes = (originalSizeBytes - compressedSizeBytes).coerceAtLeast(0L)
            val savedPercentage = if (originalSizeBytes > 0) {
                (savedBytes.toDouble() / originalSizeBytes.toDouble()) * 100.0
            } else 0.0

            val resultWidth = decodedBitmap.width
            val resultHeight = decodedBitmap.height
            decodedBitmap.recycle()

            onProgress(1.0f)

            CompressionResult(
                outputFile = outputFile,
                originalSizeBytes = originalSizeBytes,
                compressedSizeBytes = compressedSizeBytes,
                savedPercentage = savedPercentage,
                width = resultWidth,
                height = resultHeight
            )
        }
    }
}
