package com.one.utility.core.provider

import android.graphics.Bitmap
import java.io.File

/**
 * Isolated interface for optional monetization/advertising.
 * Guarantees that ONE operates 100% reliably even when no ad SDK is present.
 */
interface AdProvider {
    fun isAvailable(): Boolean = false
    fun showBannerAd() {}
    fun showInterstitialAd(onDismissed: () -> Unit) { onDismissed() }
}

/**
 * Isolated interface for future ONE Pro subscriptions/purchases.
 */
interface BillingProvider {
    val isProUser: Boolean get() = true
    suspend fun purchasePro(): Result<Boolean> = Result.success(true)
}

/**
 * Isolated contract for on-device OCR text extraction.
 */
interface OcrEngine {
    suspend fun extractText(bitmap: Bitmap): Result<String>
}

/**
 * Generic contract for chaining file processing operations.
 */
interface FileProcessor {
    suspend fun processFile(inputFile: File, options: Map<String, Any>): Result<File>
}
