package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface BackgroundRemovalEngine {
    suspend fun removeBackground(
        bitmap: Bitmap,
        tolerance: Double = 36.0,
        seedPoint: Pair<Int, Int>? = null
    ): Result<Bitmap>
}

/**
 * Hybrid background segmentation engine:
 * 1. AI-Powered On-Device Neural Segmentation (Google ML Kit) for portraits, selfies, and people.
 * 2. Boundary-Connected Flood Fill with Edge Feathering for non-human subjects, products, logos, and seed selections.
 */
class LocalBackgroundRemovalEngine : BackgroundRemovalEngine {

    override suspend fun removeBackground(
        bitmap: Bitmap,
        tolerance: Double,
        seedPoint: Pair<Int, Int>?
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        // If a specific seed point was tapped, prefer boundary flood fill from that seed
        if (seedPoint != null) {
            return@withContext removeWithFloodFill(bitmap, tolerance, seedPoint)
        }

        // Try AI ML Kit segmentation first
        val mlResult = runCatching { removeWithMlKit(bitmap, tolerance) }
        if (mlResult.isSuccess && mlResult.getOrNull() != null) {
            return@withContext mlResult
        }

        // Fallback to boundary flood fill
        removeWithFloodFill(bitmap, tolerance, null)
    }

    private suspend fun removeWithMlKit(bitmap: Bitmap, tolerance: Double): Bitmap {
        // ML Kit works on ARGB_8888 bitmap
        val workingBitmap = if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else {
            bitmap
        }

        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        val segmenter = Segmentation.getClient(options)

        return try {
            val inputImage = InputImage.fromBitmap(workingBitmap, 0)
            val segmentationMask = suspendCancellableCoroutine { cont ->
                segmenter.process(inputImage)
                    .addOnSuccessListener { mask -> cont.resume(mask) }
                    .addOnFailureListener { ex -> cont.resumeWithException(ex) }
            }

            val maskBuffer = segmentationMask.buffer.asFloatBuffer()
            val maskWidth = segmentationMask.width
            val maskHeight = segmentationMask.height

            // Calculate confidence statistics to verify subject detection
            var maxConfidence = 0.0f
            val totalPixels = maskWidth * maskHeight
            var highConfCount = 0

            maskBuffer.rewind()
            while (maskBuffer.hasRemaining()) {
                val conf = maskBuffer.get()
                if (conf > maxConfidence) maxConfidence = conf
                if (conf > 0.5f) highConfCount++
            }

            // If no person was detected with reasonable confidence, throw to fallback
            if (maxConfidence < 0.45f || highConfCount < totalPixels * 0.01) {
                throw IllegalStateException("No distinct subject detected by ML Kit")
            }

            // Create output transparent bitmap
            val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

            // Dynamic threshold controlled by tolerance slider (20f - 80f -> 0.65f - 0.30f)
            val baseThreshold = (0.75f - (tolerance.toFloat() / 100f) * 0.5f).coerceIn(0.2f, 0.8f)
            val featherBand = 0.12f

            maskBuffer.rewind()
            val scaleX = maskWidth.toFloat() / bitmap.width
            val scaleY = maskHeight.toFloat() / bitmap.height

            for (y in 0 until bitmap.height) {
                val maskY = (y * scaleY).toInt().coerceIn(0, maskHeight - 1)
                for (x in 0 until bitmap.width) {
                    val maskX = (x * scaleX).toInt().coerceIn(0, maskWidth - 1)
                    val conf = maskBuffer.get(maskY * maskWidth + maskX)

                    val pixelIndex = y * bitmap.width + x
                    val origColor = pixels[pixelIndex]

                    val alphaFactor: Float = when {
                        conf >= baseThreshold + featherBand -> 1.0f
                        conf <= baseThreshold - featherBand -> 0.0f
                        else -> (conf - (baseThreshold - featherBand)) / (2f * featherBand)
                    }

                    val origAlpha = Color.alpha(origColor)
                    val newAlpha = (origAlpha * alphaFactor).toInt().coerceIn(0, 255)

                    pixels[pixelIndex] = Color.argb(
                        newAlpha,
                        Color.red(origColor),
                        Color.green(origColor),
                        Color.blue(origColor)
                    )
                }
            }

            output.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            output
        } finally {
            segmenter.close()
        }
    }

    private fun removeWithFloodFill(
        bitmap: Bitmap,
        tolerance: Double,
        seedPoint: Pair<Int, Int>?
    ): Result<Bitmap> = runCatching {
        // Safety limit: scale down working bitmap to maximum 1200px to avoid OOM
        val maxDimension = 1200
        val scale = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            maxDimension.toFloat() / max(bitmap.width, bitmap.height)
        } else 1.0f

        val workingWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val workingHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)

        val scaledBitmap = if (scale < 1.0f) {
            Bitmap.createScaledBitmap(bitmap, workingWidth, workingHeight, true)
        } else {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        }

        val width = scaledBitmap.width
        val height = scaledBitmap.height
        val pixels = IntArray(width * height)
        scaledBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val mask = ByteArray(width * height) { 255.toByte() }
        val visited = BooleanArray(width * height)
        val queue = ArrayDeque<Int>()

        fun colorDistance(c1: Int, c2: Int): Double {
            val rDiff = Color.red(c1) - Color.red(c2)
            val gDiff = Color.green(c1) - Color.green(c2)
            val bDiff = Color.blue(c1) - Color.blue(c2)
            return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
        }

        val refColor: Int
        if (seedPoint != null) {
            val sx = (seedPoint.first * scale).toInt().coerceIn(0, width - 1)
            val sy = (seedPoint.second * scale).toInt().coerceIn(0, height - 1)
            val seedIdx = sy * width + sx
            refColor = pixels[seedIdx]
            queue.add(seedIdx)
            visited[seedIdx] = true
        } else {
            val borderSamples = mutableListOf<Int>()
            for (x in 0 until width step max(1, width / 20)) {
                borderSamples.add(pixels[x])
                borderSamples.add(pixels[(height - 1) * width + x])
            }
            for (y in 0 until height step max(1, height / 20)) {
                borderSamples.add(pixels[y * width])
                borderSamples.add(pixels[y * width + (width - 1)])
            }

            val avgR = borderSamples.sumOf { Color.red(it) } / borderSamples.size
            val avgG = borderSamples.sumOf { Color.green(it) } / borderSamples.size
            val avgB = borderSamples.sumOf { Color.blue(it) } / borderSamples.size
            refColor = Color.rgb(avgR, avgG, avgB)

            for (x in 0 until width) {
                val topIdx = x
                val bottomIdx = (height - 1) * width + x
                if (colorDistance(pixels[topIdx], refColor) <= tolerance * 1.3) {
                    queue.add(topIdx)
                    visited[topIdx] = true
                }
                if (colorDistance(pixels[bottomIdx], refColor) <= tolerance * 1.3) {
                    queue.add(bottomIdx)
                    visited[bottomIdx] = true
                }
            }
            for (y in 0 until height) {
                val leftIdx = y * width
                val rightIdx = y * width + (width - 1)
                if (colorDistance(pixels[leftIdx], refColor) <= tolerance * 1.3 && !visited[leftIdx]) {
                    queue.add(leftIdx)
                    visited[leftIdx] = true
                }
                if (colorDistance(pixels[rightIdx], refColor) <= tolerance * 1.3 && !visited[rightIdx]) {
                    queue.add(rightIdx)
                    visited[rightIdx] = true
                }
            }
        }

        val dx = intArrayOf(1, -1, 0, 0)
        val dy = intArrayOf(0, 0, 1, -1)

        while (!queue.isEmpty()) {
            val curr = queue.poll() ?: break
            val cx = curr % width
            val cy = curr / width
            mask[curr] = 0.toByte()

            for (i in 0 until 4) {
                val nx = cx + dx[i]
                val ny = cy + dy[i]
                if (nx in 0 until width && ny in 0 until height) {
                    val nIdx = ny * width + nx
                    if (!visited[nIdx]) {
                        val dist = colorDistance(pixels[nIdx], refColor)
                        if (dist <= tolerance) {
                            visited[nIdx] = true
                            queue.add(nIdx)
                        }
                    }
                }
            }
        }

        // Feathering pass
        val featheredMask = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (mask[idx] == 0.toByte()) {
                    featheredMask[idx] = 0.toByte()
                } else {
                    var bgNeighbors = 0
                    val radius = 1
                    for (fy in max(0, y - radius)..min(height - 1, y + radius)) {
                        for (fx in max(0, x - radius)..min(width - 1, x + radius)) {
                            if (mask[fy * width + fx] == 0.toByte()) bgNeighbors++
                        }
                    }
                    if (bgNeighbors > 0) {
                        val alpha = (255 * (1.0 - (bgNeighbors.toDouble() / 9.0) * 0.7)).toInt()
                        featheredMask[idx] = alpha.coerceIn(0, 255).toByte()
                    } else {
                        featheredMask[idx] = 255.toByte()
                    }
                }
            }
        }

        // Apply feathered mask
        for (i in pixels.indices) {
            val alpha = featheredMask[i].toInt() and 0xFF
            val origColor = pixels[i]
            val origAlpha = Color.alpha(origColor)
            val finalAlpha = (origAlpha * (alpha / 255f)).toInt().coerceIn(0, 255)
            pixels[i] = Color.argb(finalAlpha, Color.red(origColor), Color.green(origColor), Color.blue(origColor))
        }

        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        resultBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        if (scale < 1.0f) {
            Bitmap.createScaledBitmap(resultBitmap, bitmap.width, bitmap.height, true)
        } else {
            resultBitmap
        }
    }
}
