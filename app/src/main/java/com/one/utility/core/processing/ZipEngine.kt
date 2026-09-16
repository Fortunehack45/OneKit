package com.one.utility.core.processing

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipEngine(private val context: Context) {

    suspend fun createZip(
        inputUris: List<Uri>,
        outputZipFile: File,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            ZipOutputStream(FileOutputStream(outputZipFile)).use { zos ->
                val buffer = ByteArray(8192)
                inputUris.forEachIndexed { index, uri ->
                    onProgress(index + 1, inputUris.size)
                    val fileName = "file_${index + 1}"
                    val entry = ZipEntry(fileName)
                    zos.putNextEntry(entry)
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        var count: Int
                        while (input.read(buffer).also { count = it } != -1) {
                            zos.write(buffer, 0, count)
                        }
                    }
                    zos.closeEntry()
                }
            }
            outputZipFile
        }
    }

    suspend fun extractZip(
        zipFile: File,
        targetDir: File
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        runCatching {
            val extractedFiles = mutableListOf<File>()
            val buffer = ByteArray(8192)
            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFile = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            var count: Int
                            while (zis.read(buffer).also { count = it } != -1) {
                                fos.write(buffer, 0, count)
                            }
                        }
                        extractedFiles.add(newFile)
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
            extractedFiles
        }
    }
}
