package com.one.utility.core.processing

import android.webkit.MimeTypeMap
import java.io.File

data class RenameRule(
    val findText: String = "",
    val replaceWith: String = "",
    val prefix: String = "",
    val suffix: String = "",
    val appendIndex: Boolean = false,
    val startIndex: Int = 1,
    val indexPadding: Int = 3
)

data class RenamePreview(
    val originalName: String,
    val newName: String,
    val file: File
)

data class FileMetadata(
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val mimeType: String,
    val extension: String,
    val lastModified: Long
)

class FileRenamerEngine {

    /**
     * Previews batch rename transformations before applying them to disk.
     */
    fun previewBatchRename(
        files: List<File>,
        rule: RenameRule
    ): List<RenamePreview> {
        return files.mapIndexed { index, file ->
            val ext = file.extension
            val baseName = file.nameWithoutExtension

            var modifiedName = if (rule.findText.isNotEmpty()) {
                baseName.replace(rule.findText, rule.replaceWith)
            } else {
                baseName
            }

            if (rule.prefix.isNotEmpty()) {
                modifiedName = "${rule.prefix}$modifiedName"
            }

            if (rule.suffix.isNotEmpty()) {
                modifiedName = "$modifiedName${rule.suffix}"
            }

            if (rule.appendIndex) {
                val num = rule.startIndex + index
                val formattedNum = "%0${rule.indexPadding}d".format(num)
                modifiedName = "${modifiedName}_$formattedNum"
            }

            val finalFullName = if (ext.isNotEmpty()) "$modifiedName.$ext" else modifiedName
            RenamePreview(originalName = file.name, newName = finalFullName, file = file)
        }
    }

    /**
     * Executes renaming on disk.
     */
    fun applyRename(previews: List<RenamePreview>): Result<Int> {
        return runCatching {
            var successCount = 0
            previews.forEach { preview ->
                val parent = preview.file.parentFile
                if (parent != null) {
                    val dest = File(parent, preview.newName)
                    if (preview.file.renameTo(dest)) {
                        successCount++
                    }
                }
            }
            successCount
        }
    }

    /**
     * Extracts full metadata and MIME type.
     */
    fun inspectFile(file: File): FileMetadata {
        val ext = file.extension.lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        val size = file.length()
        val formattedSize = when {
            size >= 1024 * 1024 -> "${"%.2f".format(size.toDouble() / (1024 * 1024))} MB"
            size >= 1024 -> "${"%.1f".format(size.toDouble() / 1024)} KB"
            else -> "$size B"
        }

        return FileMetadata(
            name = file.name,
            path = file.absolutePath,
            sizeBytes = size,
            formattedSize = formattedSize,
            mimeType = mime,
            extension = ext,
            lastModified = file.lastModified()
        )
    }

    fun renameSingleFile(file: File, newName: String): Result<File> {
        return runCatching {
            val parent = file.parentFile ?: throw IllegalArgumentException("Cannot determine parent directory")
            val cleanName = newName.trim()
            val dest = File(parent, cleanName)
            if (file.renameTo(dest)) {
                dest
            } else {
                throw IllegalStateException("Failed to rename file to $cleanName")
            }
        }
    }

    fun changeFileExtension(file: File, newExt: String): Result<File> {
        val cleanExt = newExt.trim().removePrefix(".")
        val baseName = file.nameWithoutExtension
        val newFullName = if (cleanExt.isNotEmpty()) "$baseName.$cleanExt" else baseName
        return renameSingleFile(file, newFullName)
    }
}
