package com.one.utility.core.processing

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
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
    val files: List<File>
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
     * Scans accessible app storage and external cache for files larger than threshold (default: 50MB).
     */
    suspend fun findLargeFiles(
        thresholdBytes: Long = 50 * 1024 * 1024L
    ): List<LargeFileInfo> = withContext(Dispatchers.IO) {
        val results = mutableListOf<LargeFileInfo>()
        val roots = listOfNotNull(context.cacheDir, context.filesDir, context.externalCacheDir)

        fun scanDir(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    scanDir(file)
                } else if (file.isFile && file.length() >= thresholdBytes) {
                    results.add(
                        LargeFileInfo(
                            file = file,
                            sizeBytes = file.length(),
                            formattedSize = "${"%.1f".format(file.length().toDouble() / (1024 * 1024))} MB"
                        )
                    )
                }
            }
        }

        roots.forEach { scanDir(it) }
        results.sortedByDescending { it.sizeBytes }
    }

    /**
     * Detects identical duplicate files using cryptographic SHA-256 hashing.
     * Guaranteed to match exact file contents, not just names.
     */
    suspend fun findDuplicateFiles(
        targetDirectory: File
    ): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val sizeMap = mutableMapOf<Long, MutableList<File>>()

        // 1. Group files by exact byte size first (super fast pruning)
        fun groupBySize(dir: File) {
            dir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    groupBySize(file)
                } else if (file.isFile && file.length() > 0) {
                    sizeMap.getOrPut(file.length()) { mutableListOf() }.add(file)
                }
            }
        }
        groupBySize(targetDirectory)

        // 2. Hash only files that share the exact same byte length
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
                        files = duplicateFiles
                    )
                )
            }
        }

        duplicateGroups
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
}
