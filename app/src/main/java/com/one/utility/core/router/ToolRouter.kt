package com.one.utility.core.router

class ToolRouter {
    private val percentageRegex = Regex("""(?i)(\d+(?:\.\d+)?)\s*%\s*(?:of)\s*(\d+(?:\.\d+)?)""")
    private val unitRegex = Regex("""(?i)(\d+(?:\.\d+)?)\s*([a-zA-Z°]+)\s*(?:to|in|into)\s*([a-zA-Z°]+)""")
    private val mathExpressionRegex = Regex("""^[\d\s+\-*/().%^]+$""")

    fun resolve(input: String): ToolIntent {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ToolIntent.Unknown

        // 1. Percentage check: e.g. "17% of 850000" or "15 % of 400"
        percentageRegex.find(trimmed)?.let { match ->
            val percent = match.groupValues[1].toDoubleOrNull()
            val total = match.groupValues[2].toDoubleOrNull()
            if (percent != null && total != null) {
                return ToolIntent.PercentageCalculation(percent, total)
            }
        }

        // 2. Unit conversion check: e.g. "25 miles to km", "10 kg into lbs", "72 f to c"
        unitRegex.find(trimmed)?.let { match ->
            val value = match.groupValues[1].toDoubleOrNull() ?: 0.0
            val fromUnit = match.groupValues[2].lowercase()
            val toUnit = match.groupValues[3].lowercase()
            return ToolIntent.UnitConversion(value, fromUnit, toUnit)
        }

        // 3. Direct math expression: e.g. "450000 * 0.17" or "800000 / 12"
        if (mathExpressionRegex.matches(trimmed) && trimmed.any { it in "+-*/^" }) {
            return ToolIntent.MathCalculation(trimmed)
        }

        val lower = trimmed.lowercase()

        // 4. Chained multi-tool workflow check: e.g. "make these pictures smaller and put them in a pdf"
        if ((lower.contains("smaller") || lower.contains("compress")) &&
            (lower.contains("pdf") || lower.contains("document"))) {
            return ToolIntent.ChainedWorkflow(listOf("compressor", "image_to_pdf"))
        }

        // 5. Tool keyword matching
        return when {
            containsAny(lower, "turn these photos into a pdf", "image to pdf", "photo to pdf", "picture to pdf", "images to pdf", "make a pdf", "photos into a pdf", "make these pictures a pdf")
                && !lower.contains("merge") && !lower.contains("split") -> ToolIntent.ImageToPdf

            containsAny(lower, "remove background", "remove bg", "cutout", "transparent background", "erase background", "remove background from photo") -> ToolIntent.BackgroundRemover

            containsAny(lower, "make this image smaller", "compress", "compress image", "reduce size", "shrink photo", "shrink image") -> ToolIntent.ImageCompressor

            containsAny(lower, "resize", "resize this image", "1080px", "1920px", "scale photo", "crop dimensions") -> ToolIntent.ImageResizer

            containsAny(lower, "convert png to jpg", "convert image", "png to jpg", "jpg to png", "convert webp") -> ToolIntent.ImageConverter

            containsAny(lower, "merge these pdfs", "merge pdf", "combine pdf", "join pdf") -> ToolIntent.PdfMerger

            containsAny(lower, "split pdf", "extract pages", "separate pdf") -> ToolIntent.PdfSplitter

            containsAny(lower, "generate a qr code", "qr code", "generate qr", "make qr", "create qr") -> ToolIntent.QrGenerator

            containsAny(lower, "scan qr", "scan document", "read barcode", "barcode scanner", "qr scanner") -> ToolIntent.QrScanner

            containsAny(lower, "find duplicate photos", "storage cleaner", "clean storage", "duplicate files", "cleaner") -> ToolIntent.StorageCleaner

            containsAny(lower, "word count", "character count", "text cleaner", "uppercase", "lowercase", "text tools") -> ToolIntent.TextTools

            containsAny(lower, "json", "json format", "base64", "jwt", "hash generator", "sha-256", "developer tools", "unix timestamp") -> ToolIntent.DeveloperTools

            containsAny(lower, "password generator", "generate password", "secure password", "create password") -> ToolIntent.PasswordGenerator

            else -> ToolIntent.Unknown
        }
    }

    private fun containsAny(text: String, vararg phrases: String): Boolean {
        return phrases.any { text.contains(it) }
    }
}
