package com.one.utility.core.processing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class PdfPageInfo(
    val pageIndex: Int,
    val width: Int,
    val height: Int
)

class PdfToolboxEngine(private val context: Context) {

    fun getPdfPageCount(pdfUri: Uri): Int {
        return runCatching {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return 0
            val renderer = PdfRenderer(pfd)
            val count = renderer.pageCount
            renderer.close()
            pfd.close()
            count
        }.getOrDefault(0)
    }

    /**
     * Converts pages of a PDF to Bitmap images using Android's native PdfRenderer.
     * 100% on-device and memory safe.
     */
    suspend fun pdfToImages(
        pdfUri: Uri,
        outputDir: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 90,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            outputDir.mkdirs()
            val fileDescriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF descriptor")

            fileDescriptor.use { pfd ->
                val renderer = PdfRenderer(pfd)
                try {
                    val pageCount = renderer.pageCount
                    val resultFiles = mutableListOf<File>()
                    val scale = 2

                    for (i in 0 until pageCount) {
                        onProgress(i + 1, pageCount)
                        val page = renderer.openPage(i)

                        // High quality 2x supersampling for crisp text
                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()

                        val ext = if (format == Bitmap.CompressFormat.PNG) "png" else "jpg"
                        val outFile = File(outputDir, "ONE_page_${i + 1}.$ext")
                        FileOutputStream(outFile).use { out ->
                            bitmap.compress(format, quality, out)
                        }
                        bitmap.recycle()
                        resultFiles.add(outFile)
                    }
                    resultFiles
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Merges multiple PDF files into a single unified PDF document using PdfRenderer.
     */
    suspend fun mergePdfs(
        pdfUris: List<Uri>,
        outputFile: File,
        onProgress: (docIndex: Int, totalDocs: Int) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val newDocument = PdfDocument()
            var globalPageNumber = 1
            val scale = 2

            pdfUris.forEachIndexed { docIdx, uri ->
                onProgress(docIdx + 1, pdfUris.size)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@forEachIndexed
                pfd.use { descriptor ->
                    val renderer = PdfRenderer(descriptor)
                    try {
                        for (pageIdx in 0 until renderer.pageCount) {
                            val page = renderer.openPage(pageIdx)
                            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, globalPageNumber++).create()
                            val newPage = newDocument.startPage(pageInfo)

                            val bitmap = Bitmap.createBitmap(
                                (page.width * scale).coerceAtLeast(1),
                                (page.height * scale).coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            page.close()

                            val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                            val destRect = android.graphics.Rect(0, 0, pageInfo.pageWidth, pageInfo.pageHeight)
                            newPage.canvas.drawBitmap(bitmap, srcRect, destRect, null)
                            newDocument.finishPage(newPage)
                            bitmap.recycle()
                        }
                    } finally {
                        renderer.close()
                    }
                }
            }

            if (globalPageNumber == 1) {
                newDocument.close()
                throw IllegalArgumentException("No pages found to merge")
            }

            FileOutputStream(outputFile).use { out ->
                newDocument.writeTo(out)
            }
            newDocument.close()
            outputFile
        }
    }

    /**
     * Splits a PDF by extracting specific page ranges (e.g. "1-3, 5") while preserving user page ordering.
     */
    suspend fun splitPdf(
        pdfUri: Uri,
        pageIndicesToKeep: Collection<Int>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            if (pageIndicesToKeep.isEmpty()) {
                throw IllegalArgumentException("No pages selected to extract")
            }
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    var newPageNum = 1
                    val scale = 2

                    for (pageIdx in pageIndicesToKeep) {
                        if (pageIdx in 0 until renderer.pageCount) {
                            val page = renderer.openPage(pageIdx)
                            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, newPageNum++).create()
                            val newPage = newDocument.startPage(pageInfo)

                            val bitmap = Bitmap.createBitmap(
                                (page.width * scale).coerceAtLeast(1),
                                (page.height * scale).coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            page.close()

                            val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                            val destRect = android.graphics.Rect(0, 0, pageInfo.pageWidth, pageInfo.pageHeight)
                            newPage.canvas.drawBitmap(bitmap, srcRect, destRect, null)
                            newDocument.finishPage(newPage)
                            bitmap.recycle()
                        }
                    }

                    if (newPageNum == 1) {
                        newDocument.close()
                        throw IllegalArgumentException("None of the selected pages were found in the document")
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Rotates all or selected PDF pages by 90, 180, or 270 degrees.
     */
    suspend fun rotatePdf(
        pdfUri: Uri,
        degrees: Int,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    val normDegrees = ((degrees % 360) + 360) % 360
                    val isSwapped = normDegrees == 90 || normDegrees == 270

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pWidth = if (isSwapped) page.height else page.width
                        val pHeight = if (isSwapped) page.width else page.height
                        val pageInfo = PdfDocument.PageInfo.Builder(pWidth, pHeight, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val scale = 2
                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        val canvas = newPage.canvas
                        canvas.save()
                        when (normDegrees) {
                            90 -> {
                                canvas.translate(pWidth.toFloat(), 0f)
                                canvas.rotate(90f)
                            }
                            180 -> {
                                canvas.translate(pWidth.toFloat(), pHeight.toFloat())
                                canvas.rotate(180f)
                            }
                            270 -> {
                                canvas.translate(0f, pHeight.toFloat())
                                canvas.rotate(270f)
                            }
                        }
                        val destRect = android.graphics.Rect(0, 0, page.width, page.height)
                        canvas.drawBitmap(bitmap, null, destRect, null)
                        canvas.restore()
                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Compresses PDF pages by re-encoding rendered pages as optimized JPEGs.
     */
    suspend fun compressPdf(
        pdfUri: Uri,
        outputFile: File,
        quality: Int = 60
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val bitmap = Bitmap.createBitmap(
                            page.width.coerceAtLeast(1),
                            page.height.coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        val baos = java.io.ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(20, 95), baos)
                        val compressedBmp = android.graphics.BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size())

                        newPage.canvas.drawBitmap(compressedBmp, null, android.graphics.Rect(0, 0, page.width, page.height), null)
                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                        compressedBmp.recycle()
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Deletes specific 0-based page indices and saves the remaining pages as a new PDF.
     */
    suspend fun deletePages(
        pdfUri: Uri,
        pageIndicesToDelete: Set<Int>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val total = getPdfPageCount(pdfUri)
            val remaining = (0 until total).filter { it !in pageIndicesToDelete }
            if (remaining.isEmpty()) {
                throw IllegalArgumentException("Cannot delete all pages from PDF")
            }
            splitPdf(pdfUri, remaining, outputFile).getOrThrow()
        }
    }

    /**
     * Duplicates specific 0-based page indices in a PDF.
     */
    suspend fun duplicatePages(
        pdfUri: Uri,
        pageIndicesToDuplicate: Collection<Int>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    var newPageNum = 1
                    val scale = 2

                    for (i in 0 until renderer.pageCount) {
                        val count = if (i in pageIndicesToDuplicate) 2 else 1
                        for (k in 0 until count) {
                            val page = renderer.openPage(i)
                            val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, newPageNum++).create()
                            val newPage = newDocument.startPage(pageInfo)

                            val bitmap = Bitmap.createBitmap(
                                (page.width * scale).coerceAtLeast(1),
                                (page.height * scale).coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                            page.close()

                            newPage.canvas.drawBitmap(bitmap, null, android.graphics.Rect(0, 0, page.width, page.height), null)
                            newDocument.finishPage(newPage)
                            bitmap.recycle()
                        }
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Stamped diagonal watermark across each PDF page.
     */
    suspend fun watermarkPdf(
        pdfUri: Uri,
        watermarkText: String,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    val scale = 2

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        newPage.canvas.drawBitmap(bitmap, null, android.graphics.Rect(0, 0, page.width, page.height), null)

                        // Draw watermark
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            alpha = 75
                            textSize = (page.width / 9f).coerceAtLeast(28f)
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                        }
                        newPage.canvas.save()
                        newPage.canvas.rotate(-45f, page.width / 2f, page.height / 2f)
                        newPage.canvas.drawText(watermarkText, page.width / 2f, page.height / 2f, paint)
                        newPage.canvas.restore()

                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Converts page dimensions of a PDF to standard sizes (A4, Letter, Legal, A3).
     */
    suspend fun convertPageSize(
        pdfUri: Uri,
        targetWidth: Int,
        targetHeight: Int,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pageInfo = PdfDocument.PageInfo.Builder(targetWidth, targetHeight, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val scale = 2
                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        // Center and fit within target aspect ratio
                        val pageAspect = page.width.toFloat() / page.height.toFloat()
                        val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()
                        val destRect = if (pageAspect > targetAspect) {
                            val h = (targetWidth / pageAspect).toInt()
                            val top = (targetHeight - h) / 2
                            android.graphics.Rect(0, top, targetWidth, top + h)
                        } else {
                            val w = (targetHeight * pageAspect).toInt()
                            val left = (targetWidth - w) / 2
                            android.graphics.Rect(left, 0, left + w, targetHeight)
                        }

                        newPage.canvas.drawBitmap(bitmap, null, destRect, null)
                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                    }

                    FileOutputStream(outputFile).use { out ->
                        newDocument.writeTo(out)
                    }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Stamps custom text at a specified position (TOP, CENTER, BOTTOM) on all pages of a PDF.
     */
    suspend fun addTextToPdf(
        pdfUri: Uri,
        text: String,
        position: String = "BOTTOM",
        fontSizePt: Float = 14f,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    val scale = 2

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        newPage.canvas.drawBitmap(bitmap, null, android.graphics.Rect(0, 0, page.width, page.height), null)

                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = fontSizePt
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        val yPos = when (position) {
                            "TOP" -> 40f
                            "CENTER" -> page.height / 2f
                            else -> page.height - 30f
                        }
                        newPage.canvas.drawText(text, page.width / 2f, yPos, paint)
                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                    }

                    FileOutputStream(outputFile).use { out -> newDocument.writeTo(out) }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Stamps a transparent signature bitmap onto the specified page of a PDF.
     */
    suspend fun addSignatureToPdf(
        pdfUri: Uri,
        signatureBitmap: Bitmap,
        targetPageIndex: Int = 0,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            outputFile.parentFile?.mkdirs()
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")

            pfd.use { descriptor ->
                val renderer = PdfRenderer(descriptor)
                try {
                    val newDocument = PdfDocument()
                    val scale = 2

                    for (i in 0 until renderer.pageCount) {
                        val page = renderer.openPage(i)
                        val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                        val newPage = newDocument.startPage(pageInfo)

                        val bitmap = Bitmap.createBitmap(
                            (page.width * scale).coerceAtLeast(1),
                            (page.height * scale).coerceAtLeast(1),
                            Bitmap.Config.ARGB_8888
                        )
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                        page.close()

                        newPage.canvas.drawBitmap(bitmap, null, android.graphics.Rect(0, 0, page.width, page.height), null)

                        // If this is the target page, stamp the signature at bottom-right
                        if (i == targetPageIndex) {
                            val sigWidth = (page.width * 0.35f).toInt()
                            val sigHeight = (sigWidth * (signatureBitmap.height.toFloat() / signatureBitmap.width.toFloat())).toInt()
                            val left = (page.width - sigWidth - 40).toInt()
                            val top = (page.height - sigHeight - 40).toInt()
                            val destRect = android.graphics.Rect(left, top, left + sigWidth, top + sigHeight)
                            newPage.canvas.drawBitmap(signatureBitmap, null, destRect, null)
                        }

                        newDocument.finishPage(newPage)
                        bitmap.recycle()
                    }

                    FileOutputStream(outputFile).use { out -> newDocument.writeTo(out) }
                    newDocument.close()
                    outputFile
                } finally {
                    renderer.close()
                }
            }
        }
    }

    /**
     * Inspects PDF page count, dimensions, and file size.
     */
    fun getDetailedMetadata(pdfUri: Uri): Map<String, String> {
        val result = mutableMapOf<String, String>()
        runCatching {
            val count = getPdfPageCount(pdfUri)
            result["Total Pages"] = "$count pages"
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
            pfd?.use {
                val size = it.statSize
                if (size > 0) {
                    val formattedSize = when {
                        size >= 1024 * 1024 -> "${"%.2f".format(size.toDouble() / (1024 * 1024))} MB"
                        size >= 1024 -> "${"%.1f".format(size.toDouble() / 1024)} KB"
                        else -> "$size bytes"
                    }
                    result["File Size"] = formattedSize
                }
                val renderer = PdfRenderer(it)
                if (renderer.pageCount > 0) {
                    val firstPage = renderer.openPage(0)
                    result["Dimensions"] = "${firstPage.width} x ${firstPage.height} pt"
                    val pageType = when {
                        Math.abs(firstPage.width - 595) < 10 && Math.abs(firstPage.height - 842) < 10 -> "A4 (Standard)"
                        Math.abs(firstPage.width - 612) < 10 && Math.abs(firstPage.height - 792) < 10 -> "US Letter"
                        Math.abs(firstPage.width - 612) < 10 && Math.abs(firstPage.height - 1008) < 10 -> "US Legal"
                        else -> "Custom Size"
                    }
                    result["Format Standard"] = pageType
                    firstPage.close()
                }
                renderer.close()
            }
        }
        return result
    }
}
