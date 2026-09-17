package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

class ImageEffectsEngine(private val context: Context) {

    // 1. Grayscale Filter
    suspend fun applyGrayscale(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(0f) }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        result
    }

    // 2. Brightness (-100 to +100)
    suspend fun adjustBrightness(source: Bitmap, brightness: Float): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, brightness,
                0f, 1f, 0f, 0f, brightness,
                0f, 0f, 1f, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        result
    }

    // 3. Contrast (0.5 to 2.0, default 1.0)
    suspend fun adjustContrast(source: Bitmap, contrast: Float): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val scale = contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val cm = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        result
    }

    // 4. Saturation (0.0 to 2.0, default 1.0)
    suspend fun adjustSaturation(source: Bitmap, saturation: Float): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix().apply { setSaturation(saturation) }
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(source, 0f, 0f, paint)
        result
    }

    // 5. Pixelator / Censor (block size in px: 4..50)
    suspend fun pixelate(source: Bitmap, blockSize: Int): Bitmap = withContext(Dispatchers.Default) {
        val bSize = blockSize.coerceIn(2, 80)
        val downWidth = max(1, source.width / bSize)
        val downHeight = max(1, source.height / bSize)

        val small = Bitmap.createScaledBitmap(source, downWidth, downHeight, false)
        val pixelated = Bitmap.createScaledBitmap(small, source.width, source.height, false)
        small.recycle()
        pixelated
    }

    // 6. Fast Box Blur (radius: 1..25)
    suspend fun applyBlur(source: Bitmap, radius: Int): Bitmap = withContext(Dispatchers.Default) {
        val rad = radius.coerceIn(1, 30)
        // Downscale slightly for speed and smooth gaussian appearance
        val scale = 0.4f
        val w = max(1, (source.width * scale).toInt())
        val h = max(1, (source.height * scale).toInt())
        val scaled = Bitmap.createScaledBitmap(source, w, h, true)

        val pixels = IntArray(w * h)
        scaled.getPixels(pixels, 0, w, 0, 0, w, h)
        boxBlurFilter(pixels, w, h, rad)

        val blurredScaled = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        blurredScaled.setPixels(pixels, 0, w, 0, 0, w, h)
        val result = Bitmap.createScaledBitmap(blurredScaled, source.width, source.height, true)

        scaled.recycle()
        blurredScaled.recycle()
        result
    }

    private fun boxBlurFilter(pix: IntArray, w: Int, h: Int, radius: Int) {
        val wm = w - 1
        val hm = h - 1
        val wh = w * h
        val div = radius + radius + 1

        val r = IntArray(wh)
        val g = IntArray(wh)
        val b = IntArray(wh)
        var rsum: Int; var gsum: Int; var bsum: Int
        var x: Int; var y: Int; var i: Int; var p: Int; var yp: Int; var yi: Int; var yw: Int
        val vmin = IntArray(max(w, h))

        var divsum = (div + 1) shr 1
        divsum *= divsum
        val dv = IntArray(256 * divsum)
        for (idx in 0 until 256 * divsum) dv[idx] = idx / divsum

        yw = 0
        yi = 0

        for (curY in 0 until h) {
            rsum = 0; gsum = 0; bsum = 0
            for (curI in -radius..radius) {
                p = pix[yi + min(wm, max(curI, 0))]
                rsum += (p and 0xff0000) shr 16
                gsum += (p and 0x00ff00) shr 8
                bsum += (p and 0x0000ff)
            }
            for (curX in 0 until w) {
                r[yi] = dv[rsum]
                g[yi] = dv[gsum]
                b[yi] = dv[bsum]

                if (curY == 0) vmin[curX] = min(curX + radius + 1, wm)
                val p1 = pix[yw + vmin[curX]]
                val p2 = pix[yw + max(curX - radius, 0)]

                rsum += ((p1 and 0xff0000) - (p2 and 0xff0000)) shr 16
                gsum += ((p1 and 0x00ff00) - (p2 and 0x00ff00)) shr 8
                bsum += (p1 and 0x0000ff) - (p2 and 0x0000ff)
                yi++
            }
            yw += w
        }

        for (curX in 0 until w) {
            rsum = 0; gsum = 0; bsum = 0
            yp = -radius * w
            for (curI in -radius..radius) {
                yi = max(0, yp) + curX
                rsum += r[yi]
                gsum += g[yi]
                bsum += b[yi]
                yp += w
            }
            yi = curX
            for (curY in 0 until h) {
                pix[yi] = (0xff000000.toInt() or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum])
                if (curX == 0) vmin[curY] = min(curY + radius + 1, hm) * w
                val p1 = curX + vmin[curY]
                val p2 = curX + max(curY - radius, 0) * w

                rsum += r[p1] - r[p2]
                gsum += g[p1] - g[p2]
                bsum += b[p1] - b[p2]
                yi += w
            }
        }
    }

    // 7. Sharpen Filter (3x3 Kernel)
    suspend fun applySharpen(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val w = source.width
        val h = source.height
        val srcPixels = IntArray(w * h)
        val dstPixels = IntArray(w * h)
        source.getPixels(srcPixels, 0, w, 0, 0, w, h)

        // Sharpen Kernel:
        // [  0, -1,  0 ]
        // [ -1,  5, -1 ]
        // [  0, -1,  0 ]
        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val idx = y * w + x
                val cC = srcPixels[idx]
                val cT = srcPixels[(y - 1) * w + x]
                val cB = srcPixels[(y + 1) * w + x]
                val cL = srcPixels[y * w + (x - 1)]
                val cR = srcPixels[y * w + (x + 1)]

                val r = (5 * ((cC shr 16) and 0xFF) - ((cT shr 16) and 0xFF) - ((cB shr 16) and 0xFF) - ((cL shr 16) and 0xFF) - ((cR shr 16) and 0xFF)).coerceIn(0, 255)
                val g = (5 * ((cC shr 8) and 0xFF) - ((cT shr 8) and 0xFF) - ((cB shr 8) and 0xFF) - ((cL shr 8) and 0xFF) - ((cR shr 8) and 0xFF)).coerceIn(0, 255)
                val b = (5 * (cC and 0xFF) - (cT and 0xFF) - (cB and 0xFF) - (cL and 0xFF) - (cR and 0xFF)).coerceIn(0, 255)
                val a = (cC shr 24) and 0xFF

                dstPixels[idx] = (a shl 24) or (r shl 16) or (g shl 8) or b
            }
        }

        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        result.setPixels(dstPixels, 0, w, 0, 0, w, h)
        result
    }

    // 8. Background Color Fill (For transparent PNGs)
    suspend fun changeBackground(source: Bitmap, backgroundColor: Int): Bitmap = withContext(Dispatchers.Default) {
        val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(source, 0f, 0f, null)
        result
    }

    // 9. EXIF Metadata Extraction
    suspend fun extractExif(uri: Uri): Map<String, String> = withContext(Dispatchers.IO) {
        val map = mutableMapOf<String, String>()
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.getAttribute(ExifInterface.TAG_MAKE)?.let { map["Camera Make"] = it }
                exif.getAttribute(ExifInterface.TAG_MODEL)?.let { map["Camera Model"] = it }
                exif.getAttribute(ExifInterface.TAG_DATETIME)?.let { map["Date & Time"] = it }
                exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)?.let { map["Width"] = "${it}px" }
                exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)?.let { map["Height"] = "${it}px" }
                exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS)?.let { map["ISO"] = it }
                exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { map["Aperture"] = "f/$it" }
                exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { map["Shutter Speed"] = "${it}s" }
                exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { map["Focal Length"] = "${it}mm" }
                exif.getAttribute(ExifInterface.TAG_FLASH)?.let { map["Flash"] = it }
                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    map["GPS Coordinates"] = String.format("%.4f, %.4f", latLong[0], latLong[1])
                }
            }
        }
        if (map.isEmpty()) {
            map["Status"] = "No EXIF metadata found (or image was stripped)"
        }
        map
    }

    // 10. Strip EXIF & Export Clean Image
    suspend fun stripExif(sourceBitmap: Bitmap, outFile: File): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            FileOutputStream(outFile).use { out ->
                sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            outFile
        }
    }
}
