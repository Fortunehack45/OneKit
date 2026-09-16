package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class ResizePreset(val label: String, val targetWidth: Int, val targetHeight: Int) {
    HD_1080("1080px (Full HD)", 1080, 1920),
    HD_1920("1920px (Desktop)", 1920, 1080),
    INSTAGRAM_SQUARE("Instagram (1:1)", 1080, 1080),
    INSTAGRAM_STORY("Story (9:16)", 1080, 1920),
    PROFILE("Profile Avatar", 512, 512)
}

enum class OutputFormat(val extension: String, val compressFormat: Bitmap.CompressFormat) {
    JPEG("jpg", Bitmap.CompressFormat.JPEG),
    PNG("png", Bitmap.CompressFormat.PNG),
    WEBP("webp", Bitmap.CompressFormat.WEBP)
}

class ImageResizerEngine(private val context: Context) {

    suspend fun resizeImage(
        inputUri: Uri,
        outputFile: File,
        targetWidth: Int,
        targetHeight: Int,
        format: OutputFormat = OutputFormat.JPEG,
        quality: Int = 90
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(inputUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, bounds)
            }

            val origWidth = bounds.outWidth
            val origHeight = bounds.outHeight

            var sampleSize = 1
            if (origWidth > targetWidth || origHeight > targetHeight) {
                val halfWidth = origWidth / 2
                val halfHeight = origHeight / 2
                while ((halfWidth / sampleSize) >= targetWidth && (halfHeight / sampleSize) >= targetHeight) {
                    sampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }
            val originalBitmap = context.contentResolver.openInputStream(inputUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: throw IllegalStateException("Could not decode image for resizing")

            val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)

            FileOutputStream(outputFile).use { out ->
                scaledBitmap.compress(format.compressFormat, quality, out)
            }

            if (scaledBitmap != originalBitmap) {
                originalBitmap.recycle()
            }
            scaledBitmap.recycle()

            outputFile
        }
    }
}
