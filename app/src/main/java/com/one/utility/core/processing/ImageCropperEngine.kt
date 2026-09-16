package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class CropAspectRatio(val label: String, val ratioX: Float, val ratioY: Float) {
    FREE("Free", 0f, 0f),
    SQUARE("1:1 Square", 1f, 1f),
    FOUR_THREE("4:3 Standard", 4f, 3f),
    THREE_FOUR("3:4 Portrait", 3f, 4f),
    SIXTEEN_NINE("16:9 Widescreen", 16f, 9f),
    NINE_SIXTEEN("9:16 Story", 9f, 16f)
}

class ImageCropperEngine {

    suspend fun transformBitmap(
        source: Bitmap,
        rotationDegrees: Float = 0f,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false,
        aspectRatio: CropAspectRatio = CropAspectRatio.FREE
    ): Bitmap = withContext(Dispatchers.Default) {
        val matrix = Matrix()

        if (flipHorizontal) matrix.postScale(-1f, 1f)
        if (flipVertical) matrix.postScale(1f, -1f)
        if (rotationDegrees != 0f) matrix.postRotate(rotationDegrees)

        var transformed = Bitmap.createBitmap(
            source,
            0,
            0,
            source.width,
            source.height,
            matrix,
            true
        )

        // Center crop if specific aspect ratio is requested
        if (aspectRatio != CropAspectRatio.FREE && aspectRatio.ratioX > 0 && aspectRatio.ratioY > 0) {
            val targetRatio = aspectRatio.ratioX / aspectRatio.ratioY
            val currentRatio = transformed.width.toFloat() / transformed.height.toFloat()

            val cropWidth: Int
            val cropHeight: Int
            val startX: Int
            val startY: Int

            if (currentRatio > targetRatio) {
                // Image is wider than desired ratio -> crop width
                cropHeight = transformed.height
                cropWidth = (cropHeight * targetRatio).toInt().coerceAtMost(transformed.width)
                startX = (transformed.width - cropWidth) / 2
                startY = 0
            } else {
                // Image is taller than desired ratio -> crop height
                cropWidth = transformed.width
                cropHeight = (cropWidth / targetRatio).toInt().coerceAtMost(transformed.height)
                startX = 0
                startY = (transformed.height - cropHeight) / 2
            }

            val cropped = Bitmap.createBitmap(transformed, startX, startY, cropWidth, cropHeight)
            if (cropped != transformed) {
                transformed.recycle()
            }
            transformed = cropped
        }

        transformed
    }

    suspend fun saveBitmapToFile(
        bitmap: Bitmap,
        outputFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        quality: Int = 90
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(format, quality, out)
            }
            outputFile
        }
    }
}
