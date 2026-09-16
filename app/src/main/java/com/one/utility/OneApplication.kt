package com.one.utility

import android.app.Application

class OneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize local-only services and clean stale temp cache
        cleanOldTempFiles()
    }

    private fun cleanOldTempFiles() {
        runCatching {
            val cacheDir = cacheDir
            val threshold = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24 hours
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.lastModified() < threshold) {
                    file.delete()
                }
            }
        }
    }
}
