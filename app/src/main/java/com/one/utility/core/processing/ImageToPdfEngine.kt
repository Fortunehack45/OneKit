package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class PageSize(val displayName: String, val widthPoints: Int, val heightPoints: Int) {
    A4("A4 (595 × 842)", 595, 842),
    LETTER("Letter (612 × 792)", 612, 792),
    ORIGINAL("Original Dimensions", 0, 0)
}

data class PdfOptions(
    val pageSize: PageSize = PageSize.A4,
    val marginPoints: Int = 24,
    val isLandscape: Boolean = false,
    val fitPage: Boolean = true,
    val addPageNumbers: Boolean = true
)

class ImageToPdfEngine(private val context: Context) {

    suspend fun convertImagesToPdf(
        imageUris: List<Uri>,
        outputFile: File,
        options: PdfOptions = PdfOptions(),
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val document = PdfDocument()
            val paint = Paint(Paint.FILTER_BITMAP_FLAG)

            imageUris.forEachIndexed { index, uri ->
                onProgress(index + 1, imageUris.size)

                // 1. Read bounds without decoding full bitmap into memory
                val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOptions)
                }

                val originalWidth = boundsOptions.outWidth
                val originalHeight = boundsOptions.outHeight

                val (pageWidth, pageHeight) = if (options.pageSize == PageSize.ORIGINAL) {
                    originalWidth to originalHeight
                } else {
                    val w = if (options.isLandscape) options.pageSize.heightPoints else options.pageSize.widthPoints
                    val h = if (options.isLandscape) options.pageSize.widthPoints else options.pageSize.heightPoints
                    w to h
                }

                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
                val page = document.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // 2. Safely decode bitmap using sample sizing
                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = calculateInSampleSize(boundsOptions, pageWidth, pageHeight)
                }
                val bitmap: Bitmap? = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                }

                if (bitmap != null) {
                    val availableWidth = (pageWidth - (options.marginPoints * 2)).toFloat().coerceAtLeast(10f)
                    val availableHeight = (pageHeight - (options.marginPoints * 2)).toFloat().coerceAtLeast(10f)

                    val scale = if (options.fitPage) {
                        minOf(
                            availableWidth / bitmap.width.toFloat(),
                            availableHeight / bitmap.height.toFloat()
                        )
                    } else {
                        maxOf(
                            availableWidth / bitmap.width.toFloat(),
                            availableHeight / bitmap.height.toFloat()
                        )
                    }

                    val scaledWidth = bitmap.width * scale
                    val scaledHeight = bitmap.height * scale

                    val left = options.marginPoints + (availableWidth - scaledWidth) / 2f
                    val top = options.marginPoints + (availableHeight - scaledHeight) / 2f

                    val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
                    canvas.drawBitmap(bitmap, null, destRect, paint)

                    if (options.addPageNumbers) {
                        val textPaint = Paint().apply {
                            textSize = 10f
                            color = Color.DKGRAY
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText(
                            "${index + 1} / ${imageUris.size}",
                            (pageWidth / 2).toFloat(),
                            (pageHeight - 10).toFloat(),
                            textPaint
                        )
                    }

                    document.finishPage(page)
                    bitmap.recycle()
                } else {
                    document.finishPage(page)
                }
            }

            FileOutputStream(outputFile).use { outStream ->
                document.writeTo(outStream)
            }
            document.close()
            outputFile
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (reqWidth > 0 && reqHeight > 0 && (height > reqHeight || width > reqWidth)) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
