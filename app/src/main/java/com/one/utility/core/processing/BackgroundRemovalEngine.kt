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
 * Studio-Grade Hybrid Background Segmentation Engine:
 * 1. AI-Powered On-Device Neural Segmentation (Google ML Kit) with Sub-Pixel Bilinear Edge Sampling,
 *    Smoothstep Hermite feathering, and Color Decontamination (Photoshop-style edge de-fringing).
 * 2. Multi-Seed Perimeter Variance Flood Fill with Morphological Erosion, Anti-Aliased Gaussian
 *    Feathering, and Foreground Matting for non-human subjects, products, logos, and seed selections.
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

        // Fallback to enhanced boundary flood fill
        removeWithFloodFill(bitmap, tolerance, null)
    }

    private suspend fun removeWithMlKit(bitmap: Bitmap, tolerance: Double): Bitmap {
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

            val maskWidth = segmentationMask.width
            val maskHeight = segmentationMask.height
            val totalPixels = maskWidth * maskHeight
            val rawConf = FloatArray(totalPixels)

            val maskBuffer = segmentationMask.buffer.asFloatBuffer()
            maskBuffer.rewind()
            maskBuffer.get(rawConf)

            var maxConfidence = 0.0f
            var highConfCount = 0
            for (c in rawConf) {
                if (c > maxConfidence) maxConfidence = c
                if (c > 0.5f) highConfCount++
            }

            // Fallback if no distinct subject detected
            if (maxConfidence < 0.42f || highConfCount < totalPixels * 0.01) {
                throw IllegalStateException("No distinct subject detected by ML Kit")
            }

            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            // Dynamic threshold driven by tolerance slider
            val baseThreshold = (0.72f - (tolerance.toFloat() / 100f) * 0.45f).coerceIn(0.20f, 0.75f)
            val featherBand = 0.10f

            fun sampleMaskBilinear(u: Float, v: Float): Float {
                val x0 = u.toInt().coerceIn(0, maskWidth - 1)
                val x1 = (x0 + 1).coerceIn(0, maskWidth - 1)
                val y0 = v.toInt().coerceIn(0, maskHeight - 1)
                val y1 = (y0 + 1).coerceIn(0, maskHeight - 1)

                val dx = (u - x0).coerceIn(0f, 1f)
                val dy = (v - y0).coerceIn(0f, 1f)

                val c00 = rawConf[y0 * maskWidth + x0]
                val c10 = rawConf[y0 * maskWidth + x1]
                val c01 = rawConf[y1 * maskWidth + x0]
                val c11 = rawConf[y1 * maskWidth + x1]

                val top = c00 * (1f - dx) + c10 * dx
                val bottom = c01 * (1f - dx) + c11 * dx
                return top * (1f - dy) + bottom * dy
            }

            fun hermiteInterpolate(edge0: Float, edge1: Float, x: Float): Float {
                val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
                return t * t * (3f - 2f * t)
            }

            val scaleX = maskWidth.toFloat() / width
            val scaleY = maskHeight.toFloat() / height
            val alphaMap = FloatArray(width * height)

            // Step 1: Compute sub-pixel alpha map
            for (y in 0 until height) {
                val v = y * scaleY
                for (x in 0 until width) {
                    val u = x * scaleX
                    val conf = sampleMaskBilinear(u, v)

                    val alphaFactor: Float = when {
                        conf >= baseThreshold + featherBand -> 1.0f
                        conf <= baseThreshold - featherBand -> 0.0f
                        else -> hermiteInterpolate(baseThreshold - featherBand, baseThreshold + featherBand, conf)
                    }
                    alphaMap[y * width + x] = alphaFactor
                }
            }

            // Step 2: Photoshop-Grade Color Decontamination on boundary transition pixels
            // Samples adjacent solid foreground pixels to neutralize background color spill/halos
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val resultPixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    val alphaFactor = alphaMap[idx]
                    val origColor = pixels[idx]
                    val origAlpha = Color.alpha(origColor)
                    val finalAlpha = (origAlpha * alphaFactor).toInt().coerceIn(0, 255)

                    if (finalAlpha == 0) {
                        resultPixels[idx] = 0
                    } else if (alphaFactor in 0.08f..0.92f) {
                        // Edge decontamination pass
                        var fgR = 0
                        var fgG = 0
                        var fgB = 0
                        var fgCount = 0

                        val radius = 2
                        for (dy in -radius..radius) {
                            val ny = y + dy
                            if (ny in 0 until height) {
                                for (dx in -radius..radius) {
                                    val nx = x + dx
                                    if (nx in 0 until width) {
                                        val nIdx = ny * width + nx
                                        if (alphaMap[nIdx] >= 0.92f) {
                                            val c = pixels[nIdx]
                                            fgR += Color.red(c)
                                            fgG += Color.green(c)
                                            fgB += Color.blue(c)
                                            fgCount++
                                        }
                                    }
                                }
                            }
                        }

                        if (fgCount > 0) {
                            val cleanR = fgR / fgCount
                            val cleanG = fgG / fgCount
                            val cleanB = fgB / fgCount

                            // Blend between original and decontaminated color
                            val blendRatio = (1f - alphaFactor) * 0.75f
                            val blendedR = (Color.red(origColor) * (1f - blendRatio) + cleanR * blendRatio).toInt().coerceIn(0, 255)
                            val blendedG = (Color.green(origColor) * (1f - blendRatio) + cleanG * blendRatio).toInt().coerceIn(0, 255)
                            val blendedB = (Color.blue(origColor) * (1f - blendRatio) + cleanB * blendRatio).toInt().coerceIn(0, 255)

                            resultPixels[idx] = Color.argb(finalAlpha, blendedR, blendedG, blendedB)
                        } else {
                            resultPixels[idx] = Color.argb(
                                finalAlpha,
                                Color.red(origColor),
                                Color.green(origColor),
                                Color.blue(origColor)
                            )
                        }
                    } else {
                        resultPixels[idx] = Color.argb(
                            finalAlpha,
                            Color.red(origColor),
                            Color.green(origColor),
                            Color.blue(origColor)
                        )
                    }
                }
            }

            output.setPixels(resultPixels, 0, width, 0, 0, width, height)
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

        if (seedPoint != null) {
            val sx = (seedPoint.first * scale).toInt().coerceIn(0, width - 1)
            val sy = (seedPoint.second * scale).toInt().coerceIn(0, height - 1)
            val seedIdx = sy * width + sx
            val refColor = pixels[seedIdx]
            queue.add(seedIdx)
            visited[seedIdx] = true

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
        } else {
            // Multi-seed candidate border clustering
            val samplePositions = listOf(
                0, width - 1, (height - 1) * width, (height - 1) * width + (width - 1),
                width / 2, (height - 1) * width + (width / 2),
                (height / 2) * width, (height / 2) * width + (width - 1)
            )
            val bgSeeds = samplePositions.map { pixels[it] }

            fun isBgCandidate(color: Int, tol: Double): Boolean {
                return bgSeeds.any { colorDistance(color, it) <= tol }
            }

            // Seed entire perimeter with adaptive tolerance
            for (x in 0 until width) {
                val topIdx = x
                val botIdx = (height - 1) * width + x
                if (!visited[topIdx] && isBgCandidate(pixels[topIdx], tolerance * 1.35)) {
                    queue.add(topIdx)
                    visited[topIdx] = true
                }
                if (!visited[botIdx] && isBgCandidate(pixels[botIdx], tolerance * 1.35)) {
                    queue.add(botIdx)
                    visited[botIdx] = true
                }
            }
            for (y in 0 until height) {
                val leftIdx = y * width
                val rightIdx = y * width + (width - 1)
                if (!visited[leftIdx] && isBgCandidate(pixels[leftIdx], tolerance * 1.35)) {
                    queue.add(leftIdx)
                    visited[leftIdx] = true
                }
                if (!visited[rightIdx] && isBgCandidate(pixels[rightIdx], tolerance * 1.35)) {
                    queue.add(rightIdx)
                    visited[rightIdx] = true
                }
            }

            val dx = intArrayOf(1, -1, 0, 0)
            val dy = intArrayOf(0, 0, 1, -1)

            while (!queue.isEmpty()) {
                val curr = queue.poll() ?: break
                val cx = curr % width
                val cy = curr / width
                mask[curr] = 0.toByte()

                val currColor = pixels[curr]

                for (i in 0 until 4) {
                    val nx = cx + dx[i]
                    val ny = cy + dy[i]
                    if (nx in 0 until width && ny in 0 until height) {
                        val nIdx = ny * width + nx
                        if (!visited[nIdx]) {
                            val neighborColor = pixels[nIdx]
                            val distToCurr = colorDistance(neighborColor, currColor)
                            val isNearSeed = isBgCandidate(neighborColor, tolerance)

                            if (distToCurr <= tolerance * 0.9 || isNearSeed) {
                                visited[nIdx] = true
                                queue.add(nIdx)
                            }
                        }
                    }
                }
            }
        }

        // 1-Pixel Morphological Erosion pass to prevent color halos
        val erodedMask = mask.clone()
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                if (mask[idx] != 0.toByte()) {
                    if (mask[idx - 1] == 0.toByte() || mask[idx + 1] == 0.toByte() ||
                        mask[idx - width] == 0.toByte() || mask[idx + width] == 0.toByte()) {
                        erodedMask[idx] = 0.toByte()
                    }
                }
            }
        }

        // 3x3 Anti-Aliased Gaussian Feathering pass
        val featheredMask = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val idx = y * width + x
                if (erodedMask[idx] == 0.toByte()) {
                    featheredMask[idx] = 0.toByte()
                } else {
                    var bgNeighbors = 0
                    val radius = 1
                    for (fy in max(0, y - radius)..min(height - 1, y + radius)) {
                        for (fx in max(0, x - radius)..min(width - 1, x + radius)) {
                            if (erodedMask[fy * width + fx] == 0.toByte()) bgNeighbors++
                        }
                    }
                    if (bgNeighbors > 0) {
                        val alpha = (255 * (1.0 - (bgNeighbors.toDouble() / 9.0) * 0.65)).toInt()
                        featheredMask[idx] = alpha.coerceIn(0, 255).toByte()
                    } else {
                        featheredMask[idx] = 255.toByte()
                    }
                }
            }
        }

        // Apply feathered mask with color preservation
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
