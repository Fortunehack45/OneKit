package com.one.utility.core.processing

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest

data class StorageSpaceInfo(
    val totalBytes: Long,
    val freeBytes: Long,
    val usedBytes: Long,
    val usedPercentage: Double
)

data class LargeFileInfo(
    val file: File,
    val sizeBytes: Long,
    val formattedSize: String
)

data class DuplicateGroup(
    val contentHash: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val files: List<File>
)

data class CacheInfo(
    val totalSizeBytes: Long,
    val formattedSize: String,
    val fileCount: Int
)

class StorageAnalyzerEngine(private val context: Context) {

    /**
     * Calculates storage breakdown (Total, Free, Used) using native StatFs.
     */
    fun getStorageBreakdown(): StorageSpaceInfo {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val total = totalBlocks * blockSize
        val free = availableBlocks * blockSize
        val used = (total - free).coerceAtLeast(0L)
        val usedPercent = if (total > 0) (used.toDouble() / total.toDouble()) * 100.0 else 0.0

        return StorageSpaceInfo(total, free, used, usedPercent)
    }

    /**
     * Scans accessible app storage and external cache for files larger than threshold (default: 10MB).
     */
    suspend fun findLargeFiles(
        thresholdBytes: Long = 10 * 1024 * 1024L
    ): List<LargeFileInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<LargeFileInfo>()
        val roots = listOfNotNull(
            context.cacheDir,
            context.filesDir,
            context.externalCacheDir,
            context.getExternalFilesDir(null)
        )

        fun scanDir(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanDir(file)
                } else if (file.isFile && file.length() >= thresholdBytes) {
                    results.add(
                        LargeFileInfo(
                            file = file,
                            sizeBytes = file.length(),
                            formattedSize = formatBytes(file.length())
                        )
                    )
                }
            }
        }

        roots.forEach { scanDir(it) }
        results.sortedByDescending { it.sizeBytes }
    }

    /**
     * Calculates total cache and temporary files across app storage.
     */
    fun getCacheInfo(): CacheInfo {
        var size = 0L
        var count = 0
        val cacheRoots = listOfNotNull(
            context.cacheDir,
            context.codeCacheDir,
            context.externalCacheDir
        )

        fun scanCache(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanCache(file)
                } else if (file.isFile) {
                    size += file.length()
                    count++
                }
            }
        }

        cacheRoots.forEach { scanCache(it) }
        return CacheInfo(size, formatBytes(size), count)
    }

    /**
     * Safely clears temporary cache files and frees space.
     * Returns total bytes deleted.
     */
    suspend fun clearCache(): Long = withContext(Dispatchers.IO) {
        var bytesFreed = 0L
        val cacheRoots = listOfNotNull(
            context.cacheDir,
            context.externalCacheDir
        )

        fun deleteContents(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteContents(file)
                    file.delete()
                } else {
                    bytesFreed += file.length()
                    file.delete()
                }
            }
        }

        cacheRoots.forEach { deleteContents(it) }
        bytesFreed
    }

    /**
     * Detects duplicate files across all app directories using cryptographic SHA-256 hashing.
     */
    suspend fun findAllDuplicateFiles(): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val sizeMap = mutableMapOf<Long, MutableList<File>>()
        val roots = listOfNotNull(
            context.cacheDir,
            context.filesDir,
            context.externalCacheDir,
            context.getExternalFilesDir(null)
        )

        fun groupBySize(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    groupBySize(file)
                } else if (file.isFile && file.length() > 0) {
                    sizeMap.getOrPut(file.length()) { mutableListOf() }.add(file)
                }
            }
        }

        roots.forEach { groupBySize(it) }

        val duplicateGroups = mutableListOf<DuplicateGroup>()
        val candidateGroups = sizeMap.values.filter { it.size > 1 }

        candidateGroups.forEach { candidates ->
            val hashMap = mutableMapOf<String, MutableList<File>>()
            candidates.forEach { file ->
                runCatching {
                    val hash = computeFileHash(file)
                    hashMap.getOrPut(hash) { mutableListOf() }.add(file)
                }
            }
            hashMap.filter { it.value.size > 1 }.forEach { (hash, duplicateFiles) ->
                duplicateGroups.add(
                    DuplicateGroup(
                        contentHash = hash,
                        sizeBytes = duplicateFiles.first().length(),
                        formattedSize = formatBytes(duplicateFiles.first().length()),
                        files = duplicateFiles
                    )
                )
            }
        }

        duplicateGroups.sortedByDescending { it.sizeBytes * it.files.size }
    }

    /**
     * Helper to create 2 small identical sample files in cacheDir for test verification.
     */
    suspend fun createSampleDuplicates(): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val sampleDir = File(context.cacheDir, "sample_tests").apply { mkdirs() }
            val testData = "ONE Utility Offline Test Payload - Duplicate Check - ${System.currentTimeMillis()}".toByteArray()
            val file1 = File(sampleDir, "sample_receipt_copy_1.txt")
            val file2 = File(sampleDir, "sample_receipt_copy_2.txt")
            FileOutputStream(file1).use { it.write(testData) }
            FileOutputStream(file2).use { it.write(testData) }
            true
        }.getOrDefault(false)
    }

    /**
     * Keeps the original (first) existing file in the group and deletes the duplicate copies.
     * Returns count of deleted duplicates.
     */
    suspend fun deleteDuplicateCopies(group: DuplicateGroup): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        val existingFiles = group.files.filter { it.exists() }
        // Keep the first existing file, delete the remaining duplicates
        for (i in 1 until existingFiles.size) {
            val file = existingFiles[i]
            if (file.delete()) {
                deletedCount++
            }
        }
        deletedCount
    }

    private fun computeFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var count: Int
            while (fis.read(buffer).also { count = it } != -1) {
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${"%.2f".format(bytes.toDouble() / (1024 * 1024 * 1024))} GB"
            bytes >= 1024 * 1024 -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }

    suspend fun findEmptyFolders(): List<File> = withContext(Dispatchers.IO) {
        val emptyDirs = mutableListOf<File>()
        val roots = listOfNotNull(context.cacheDir, context.filesDir, context.externalCacheDir, context.getExternalFilesDir(null))
        fun check(dir: File) {
            val children = dir.listFiles()
            if (children != null) {
                if (children.isEmpty()) {
                    emptyDirs.add(dir)
                } else {
                    children.filter { it.isDirectory }.forEach { check(it) }
                }
            }
        }
        roots.forEach { check(it) }
        emptyDirs
    }

    suspend fun deleteEmptyFolders(dirs: List<File>): Int = withContext(Dispatchers.IO) {
        var count = 0
        dirs.forEach {
            if (it.exists() && it.isDirectory && (it.listFiles()?.isEmpty() == true)) {
                if (it.delete()) count++
            }
        }
        count
    }

    suspend fun clearAppCache(): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        val cacheRoots = listOfNotNull(context.cacheDir, context.externalCacheDir)
        fun deleteRecursively(file: File) {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteRecursively(it) }
            }
            val len = file.length()
            if (file.delete()) freed += len
        }
        cacheRoots.forEach { root ->
            root.listFiles()?.forEach { deleteRecursively(it) }
        }
        freed
    }
}
