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
}
