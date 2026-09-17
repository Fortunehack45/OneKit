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

        // 2. Unit conversion check: e.g. "25 miles to km", "10 kg into lbs", "72 f to c", "1 ly to au"
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

        // 4. Chained multi-tool workflow check:
        // Handles "compress -> pdf", "crop -> compress -> pdf", "scan -> pdf", etc.
        if (lower.contains("->") || lower.contains("➔") || lower.contains("then") || (lower.contains(" and ") && (lower.contains("pdf") || lower.contains("compress")))) {
            val steps = mutableListOf<String>()
            val tokens = lower.split(Regex("""->|➔|then|\band\b""")).map { it.trim() }.filter { it.isNotEmpty() }
            for (tok in tokens) {
                when {
                    containsAny(tok, "crop", "cropper") -> steps.add("image_cropper")
                    containsAny(tok, "compress", "smaller", "shrink") -> steps.add("compressor")
                    containsAny(tok, "pdf", "doc") -> steps.add("image_to_pdf")
                    containsAny(tok, "bg", "background", "cutout") -> steps.add("background_remover")
                    containsAny(tok, "convert", "png", "jpg", "webp") -> steps.add("resizer")
                    containsAny(tok, "scan", "scanner") -> steps.add("document_scanner")
                }
            }
            if (steps.size >= 2) {
                return ToolIntent.ChainedWorkflow(steps.distinct())
            }
        }

        if ((lower.contains("smaller") || lower.contains("compress")) &&
            (lower.contains("pdf") || lower.contains("document"))) {
            return ToolIntent.ChainedWorkflow(listOf("compressor", "image_to_pdf"))
        }

        // 5. Tool keyword matching
        return when {
            containsAny(lower, "image to pdf", "turn these photos into a pdf", "photo to pdf", "picture to pdf", "images to pdf", "make a pdf", "photos into a pdf", "make these pictures a pdf")
                && !lower.contains("merge") && !lower.contains("split") -> ToolIntent.ImageToPdf

            containsAny(lower, "remove background", "remove bg", "cutout", "transparent background", "erase background", "remove background from photo") -> ToolIntent.BackgroundRemover

            containsAny(lower, "compress", "make this image smaller", "compress image", "reduce size", "shrink photo", "shrink image") -> ToolIntent.ImageCompressor

            containsAny(lower, "cropper", "crop photo", "crop image", "crop dimensions", "crop picture") -> ToolIntent.ImageCropper

            containsAny(lower, "resize", "resize this image", "1080px", "1920px", "scale photo") -> ToolIntent.ImageResizer

            containsAny(lower, "convert png to jpg", "convert image", "png to jpg", "jpg to png", "convert webp") -> ToolIntent.ImageConverter

            containsAny(lower, "scan document", "document scanner", "scan receipt", "scan paper", "doc scanner") -> ToolIntent.DocumentScanner

            containsAny(lower, "merge pdf", "merge these pdfs", "combine pdf", "join pdf") -> ToolIntent.PdfMerger

            containsAny(lower, "split pdf", "extract pages", "separate pdf") -> ToolIntent.PdfSplitter

            containsAny(lower, "qr code", "generate a qr code", "generate qr", "make qr", "create qr", "wifi qr") -> ToolIntent.QrGenerator

            containsAny(lower, "scan qr", "read barcode", "barcode scanner", "qr scanner") -> ToolIntent.QrScanner

            containsAny(lower, "unit converter", "convert units", "unit conversion", "astronomy units", "light years", "parsec", "metric converter") -> ToolIntent.UnitConverter

            containsAny(lower, "calculator", "scientific calculator", "calc", "trig", "keypad calc", "math calculator") -> ToolIntent.Calculator

            containsAny(lower, "everyday calculator", "split bill", "tip calculator", "sales tax", "compound interest", "bmi calculator", "fuel trip") -> ToolIntent.EverydayCalculators

            containsAny(lower, "batch rename", "rename files", "rename photos", "batch file renamer") -> ToolIntent.BatchRename

            containsAny(lower, "storage cleaner", "find duplicate photos", "clean storage", "duplicate files", "cleaner") -> ToolIntent.StorageCleaner

            containsAny(lower, "word count", "character count", "text cleaner", "uppercase", "lowercase", "text tools") -> ToolIntent.TextTools

            containsAny(lower, "developer tools", "json format", "base64", "jwt", "hash generator", "sha-256", "unix timestamp") -> ToolIntent.DeveloperTools

            containsAny(lower, "password generator", "generate password", "secure password", "create password", "combinations") -> ToolIntent.PasswordGenerator

            containsAny(lower, "cool fonts", "font", "fancy text", "text styler", "bio font", "gothic", "cursive", "small caps", "name font", "japanese font") -> ToolIntent.CoolFonts

            else -> ToolIntent.Unknown
        }
    }

    private fun containsAny(text: String, vararg phrases: String): Boolean {
        return phrases.any { text.contains(it) }
    }
}
