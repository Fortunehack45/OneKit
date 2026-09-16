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
            val fileDescriptor: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF descriptor")

            val renderer = PdfRenderer(fileDescriptor)
            val pageCount = renderer.pageCount
            val resultFiles = mutableListOf<File>()

            for (i in 0 until pageCount) {
                onProgress(i + 1, pageCount)
                val page = renderer.openPage(i)

                // High quality 2x supersampling for crisp text
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
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

            renderer.close()
            fileDescriptor.close()
            resultFiles
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
            val newDocument = PdfDocument()
            var globalPageNumber = 1

            pdfUris.forEachIndexed { docIdx, uri ->
                onProgress(docIdx + 1, pdfUris.size)
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@forEachIndexed
                val renderer = PdfRenderer(pfd)

                for (pageIdx in 0 until renderer.pageCount) {
                    val page = renderer.openPage(pageIdx)
                    val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, globalPageNumber++).create()
                    val newPage = newDocument.startPage(pageInfo)

                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    newDocument.finishPage(newPage)
                    bitmap.recycle()
                }

                renderer.close()
                pfd.close()
            }

            FileOutputStream(outputFile).use { out ->
                newDocument.writeTo(out)
            }
            newDocument.close()
            outputFile
        }
    }

    /**
     * Splits a PDF by extracting specific page ranges (e.g. "1-3, 5").
     */
    suspend fun splitPdf(
        pdfUri: Uri,
        pageIndicesToKeep: Set<Int>,
        outputFile: File
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
                ?: throw IllegalArgumentException("Cannot open PDF")
            val renderer = PdfRenderer(pfd)
            val newDocument = PdfDocument()
            var newPageNum = 1

            for (i in 0 until renderer.pageCount) {
                if (pageIndicesToKeep.contains(i)) {
                    val page = renderer.openPage(i)
                    val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, newPageNum++).create()
                    val newPage = newDocument.startPage(pageInfo)

                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    page.close()

                    newPage.canvas.drawBitmap(bitmap, 0f, 0f, null)
                    newDocument.finishPage(newPage)
                    bitmap.recycle()
                }
            }

            renderer.close()
            pfd.close()

            FileOutputStream(outputFile).use { out ->
                newDocument.writeTo(out)
            }
            newDocument.close()
            outputFile
        }
    }
}
