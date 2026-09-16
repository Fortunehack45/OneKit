package com.one.utility.core.processing

import android.graphics.Bitmap
import android.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
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
 * Robust on-device background segmentation using Boundary-Connected Flood Fill & Edge Feathering.
 * Prevents wiping inner subject details (e.g. white clothes, teeth) by flood-filling only
 * from outside borders or a user-selected seed point.
 */
class LocalBackgroundRemovalEngine : BackgroundRemovalEngine {

    override suspend fun removeBackground(
        bitmap: Bitmap,
        tolerance: Double,
        seedPoint: Pair<Int, Int>?
    ): Result<Bitmap> = withContext(Dispatchers.Default) {
        runCatching {
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

            // Transparency mask: 0 = transparent (background), 255 = opaque (foreground)
            val mask = ByteArray(width * height) { 255.toByte() }
            val visited = BooleanArray(width * height)
            val queue = ArrayDeque<Int>()

            fun colorDistance(c1: Int, c2: Int): Double {
                val rDiff = Color.red(c1) - Color.red(c2)
                val gDiff = Color.green(c1) - Color.green(c2)
                val bDiff = Color.blue(c1) - Color.blue(c2)
                return sqrt((rDiff * rDiff + gDiff * gDiff + bDiff * bDiff).toDouble())
            }

            // Reference background color
            val refColor: Int
            if (seedPoint != null) {
                val sx = (seedPoint.first * scale).toInt().coerceIn(0, width - 1)
                val sy = (seedPoint.second * scale).toInt().coerceIn(0, height - 1)
                val seedIdx = sy * width + sx
                refColor = pixels[seedIdx]
                queue.add(seedIdx)
                visited[seedIdx] = true
            } else {
                // Average border samples for robust baseline
                val borderSamples = mutableListOf<Int>()
                for (x in 0 until width step max(1, width / 20)) {
                    borderSamples.add(pixels[x]) // Top
                    borderSamples.add(pixels[(height - 1) * width + x]) // Bottom
                }
                for (y in 0 until height step max(1, height / 20)) {
                    borderSamples.add(pixels[y * width]) // Left
                    borderSamples.add(pixels[y * width + (width - 1)]) // Right
                }

                val avgR = borderSamples.sumOf { Color.red(it) } / borderSamples.size
                val avgG = borderSamples.sumOf { Color.green(it) } / borderSamples.size
                val avgB = borderSamples.sumOf { Color.blue(it) } / borderSamples.size
                refColor = Color.rgb(avgR, avgG, avgB)

                // Enqueue all boundary pixels that match the background profile within initial tolerance
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
                    if (!visited[leftIdx] && colorDistance(pixels[leftIdx], refColor) <= tolerance * 1.3) {
                        queue.add(leftIdx)
                        visited[leftIdx] = true
                    }
                    if (!visited[rightIdx] && colorDistance(pixels[rightIdx], refColor) <= tolerance * 1.3) {
                        queue.add(rightIdx)
                        visited[rightIdx] = true
                    }
                }
            }

            // Breadth-First Search (BFS) Flood Fill
            val dx = intArrayOf(0, 0, 1, -1)
            val dy = intArrayOf(1, -1, 0, 0)

            while (!queue.isEmpty()) {
                val curr = queue.poll() ?: break
                mask[curr] = 0 // Mark as background

                val cx = curr % width
                val cy = curr / width
                val currColor = pixels[curr]

                for (i in 0..3) {
                    val nx = cx + dx[i]
                    val ny = cy + dy[i]

                    if (nx in 0 until width && ny in 0 until height) {
                        val nIdx = ny * width + nx
                        if (!visited[nIdx]) {
                            val neighborColor = pixels[nIdx]
                            // Distance to both root reference color and immediate neighbor color
                            val distToRef = colorDistance(neighborColor, refColor)
                            val distToCurr = colorDistance(neighborColor, currColor)

                            if (distToRef <= tolerance || (distToCurr <= tolerance * 0.7 && distToRef <= tolerance * 1.5)) {
                                visited[nIdx] = true
                                queue.add(nIdx)
                            }
                        }
                    }
                }
            }

            // Apply alpha feathering near edges for smooth, anti-aliased cutout
            val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val outputPixels = IntArray(width * height)

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val idx = y * width + x
                    val origPixel = pixels[idx]

                    if (mask[idx] == 0.toByte()) {
                        // Fully transparent background
                        outputPixels[idx] = Color.TRANSPARENT
                    } else {
                        // Check if bordering background for 1-pixel feathering
                        var hasBgNeighbor = false
                        for (i in 0..3) {
                            val nx = x + dx[i]
                            val ny = y + dy[i]
                            if (nx in 0 until width && ny in 0 until height) {
                                if (mask[ny * width + nx] == 0.toByte()) {
                                    hasBgNeighbor = true
                                    break
                                }
                            }
                        }

                        if (hasBgNeighbor) {
                            val alpha = 160
                            outputPixels[idx] = Color.argb(
                                alpha,
                                Color.red(origPixel),
                                Color.green(origPixel),
                                Color.blue(origPixel)
                            )
                        } else {
                            outputPixels[idx] = origPixel
                        }
                    }
                }
            }

            outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)
            outputBitmap
        }
    }
}
