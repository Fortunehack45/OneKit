package com.one.utility.core.router

import com.one.utility.core.processing.ComprehensiveUnitsEngine
import com.one.utility.core.processing.UniversalCalculatorEngine
import java.util.Locale

class ToolRouter {
    private val calcEngine = UniversalCalculatorEngine()
    private val unitsEngine = ComprehensiveUnitsEngine()

    // Regex to match "What's 15% of ₦850,000?", "15% of 850,000", "15 % of $400", "20% tip on $85", "15 percent of 850000 naira"
    private val percentageRegex = Regex("""(?i)(?:what(?:'s|\s+is)\s+)?(\d+(?:\.\d+)?)\s*(?:%|percent)\s*(?:of|on|off)\s*([₦$€£¥₹]?)\s*([\d,]+(?:\.\d+)?)\s*(?:naira|usd|eur|gbp|dollar|dollars|pounds)?\s*\??""")

    // Regex to match "Convert 25 GB to MB", "25 gb to mb", "10 kg into lbs", "72 f to c"
    private val unitRegex = Regex("""(?i)(?:convert\s+)?([\d,]+(?:\.\d+)?)\s*([a-zA-Z°]+)\s*(?:to|in|into)\s*([a-zA-Z°]+)\s*\??""")

    // Regex to match arithmetic expressions e.g. "25 * 40 + 150", "850000 / 12", "(50 + 20) * 3"
    private val mathExpressionRegex = Regex("""^(?:what(?:'s|\s+is)\s+)?([\d\s+\-*/().%^]+)\??$""", RegexOption.IGNORE_CASE)

    fun resolve(input: String): ToolIntent {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ToolIntent.Unknown

        // 1. Percentage check: e.g. "What's 15% of ₦850,000?", "15% of 850000"
        percentageRegex.find(trimmed)?.let { match ->
            val percent = match.groupValues[1].toDoubleOrNull()
            var currencySymbol = match.groupValues[2]
            val totalRaw = match.groupValues[3].replace(",", "")
            val total = totalRaw.toDoubleOrNull()
            if (percent != null && total != null) {
                if (currencySymbol.isEmpty()) {
                    val fullMatch = match.value.lowercase()
                    when {
                        fullMatch.contains("naira") -> currencySymbol = "₦"
                        fullMatch.contains("usd") || fullMatch.contains("dollar") -> currencySymbol = "$"
                        fullMatch.contains("eur") || fullMatch.contains("euro") -> currencySymbol = "€"
                        fullMatch.contains("gbp") || fullMatch.contains("pound") -> currencySymbol = "£"
                    }
                }
                val computed = calcEngine.calculatePercentage(percent, total)
                val formattedNum = formatNumber(computed)
                val formatted = if (currencySymbol.isNotEmpty()) "$currencySymbol$formattedNum" else formattedNum
                return ToolIntent.PercentageCalculation(percent, total, currencySymbol, formatted)
            }
        }

        // 2. Unit conversion check: e.g. "Convert 25 GB to MB", "25 miles to km", "10 kg into lbs", "72 f to c"
        unitRegex.find(trimmed)?.let { match ->
            val valueRaw = match.groupValues[1].replace(",", "")
            val value = valueRaw.toDoubleOrNull() ?: 0.0
            val fromUnit = match.groupValues[2].lowercase()
            val toUnit = match.groupValues[3].lowercase()

            val converted = calcEngine.convertUnits(value, fromUnit, toUnit)
            val formatted = "${formatNumber(converted)} ${toUnit.uppercase()}"
            return ToolIntent.UnitConversion(value, fromUnit, toUnit, formatted)
        }

        // 3. Direct math expression: e.g. "25 * 40 + 150" or "850000 / 12"
        mathExpressionRegex.find(trimmed)?.let { match ->
            val expr = match.groupValues[1].trim()
            if (expr.any { it in "+-*/^" } && expr.any { it.isDigit() }) {
                runCatching {
                    val res = calcEngine.evaluateExpression(expr)
                    val formatted = formatNumber(res)
                    return ToolIntent.MathCalculation(expr, formatted)
                }
            }
        }

        val lower = trimmed.lowercase()

        // 4. Natural language multi-tool workflows:
        // "Make these photos smaller and turn them into a PDF"
        if ((lower.contains("smaller") || lower.contains("compress")) &&
            (lower.contains("pdf") || lower.contains("document"))) {
            return ToolIntent.ChainedWorkflow(
                stepNames = listOf("resizer", "compressor", "image_to_pdf"),
                displayPipeline = "Select photos → Resize → Compress → PDF → Share"
            )
        }

        // "Remove the background and make it 1080px"
        if ((lower.contains("background") || lower.contains("bg") || lower.contains("cutout")) &&
            (lower.contains("1080") || lower.contains("resize") || lower.contains("scale"))) {
            return ToolIntent.ChainedWorkflow(
                stepNames = listOf("background_remover", "resizer"),
                displayPipeline = "Select image → Background Removal → Resize → Save"
            )
        }

        // "Scan to pdf"
        if ((lower.contains("scan") || lower.contains("scanner")) &&
            (lower.contains("pdf") || lower.contains("document"))) {
            return ToolIntent.ChainedWorkflow(
                stepNames = listOf("document_scanner", "image_to_pdf"),
                displayPipeline = "Scan Document → Enhance → PDF → Share"
            )
        }

        // Explicit arrow or "then" or "and" chaining: "crop -> compress -> pdf"
        if (lower.contains("->") || lower.contains("➔") || lower.contains("then") ||
            (lower.contains(" and ") && (lower.contains("pdf") || lower.contains("compress") || lower.contains("crop") || lower.contains("resize")))) {
            val steps = mutableListOf<String>()
            val tokens = lower.split(Regex("""->|➔|then|\band\b""")).map { it.trim() }.filter { it.isNotEmpty() }
            for (tok in tokens) {
                when {
                    containsAny(tok, "crop", "cropper") -> steps.add("image_cropper")
                    containsAny(tok, "resize", "1080", "1920", "dimensions") -> steps.add("resizer")
                    containsAny(tok, "compress", "smaller", "shrink") -> steps.add("compressor")
                    containsAny(tok, "pdf", "doc") -> steps.add("image_to_pdf")
                    containsAny(tok, "bg", "background", "cutout") -> steps.add("background_remover")
                    containsAny(tok, "convert", "png", "jpg", "webp") -> steps.add("image_converter")
                    containsAny(tok, "scan", "scanner") -> steps.add("document_scanner")
                }
            }
            if (steps.size >= 2) {
                val distinctSteps = steps.distinct()
                val pipelineStr = distinctSteps.joinToString(" → ") {
                    when (it) {
                        "image_cropper" -> "Crop"
                        "resizer" -> "Resize"
                        "compressor" -> "Compress"
                        "image_to_pdf" -> "PDF"
                        "background_remover" -> "Background Removal"
                        "document_scanner" -> "Scan"
                        "image_converter" -> "Convert"
                        else -> it.replace('_', ' ').replaceFirstChar { c -> c.uppercase() }
                    }
                } + " → Share"
                return ToolIntent.ChainedWorkflow(distinctSteps, pipelineStr)
            }
        }

        // 5. Tool keyword matching
        return when {
            containsAny(lower, "image to pdf", "turn these photos into a pdf", "photo to pdf", "picture to pdf", "images to pdf", "make a pdf", "photos into a pdf", "make these pictures a pdf", "combine photos")
                && !lower.contains("merge") && !lower.contains("split") -> ToolIntent.ImageToPdf

            containsAny(lower, "remove background", "remove bg", "cutout", "transparent background", "erase background", "remove background from photo") -> ToolIntent.BackgroundRemover

            containsAny(lower, "compress", "make this image smaller", "compress image", "reduce size", "shrink photo", "shrink image", "batch compress") -> ToolIntent.ImageCompressor

            containsAny(lower, "cropper", "crop photo", "crop image", "crop dimensions", "crop picture") -> ToolIntent.ImageCropper

            containsAny(lower, "resize", "resize this image", "1080px", "1920px", "scale photo", "resizer") -> ToolIntent.ImageResizer

            containsAny(lower, "convert png to jpg", "convert image", "png to jpg", "jpg to png", "convert webp", "image converter") -> ToolIntent.ImageConverter

            containsAny(lower, "scan document", "document scanner", "scan receipt", "scan paper", "doc scanner", "scan to image", "scan note") -> ToolIntent.DocumentScanner

            containsAny(lower, "merge pdf", "merge these pdfs", "combine pdf", "join pdf") -> ToolIntent.PdfMerger

            containsAny(lower, "split pdf", "extract pages", "separate pdf", "pdf to image", "pdf to images", "pdf pages", "pdf toolbox") -> ToolIntent.PdfSplitter
 
            containsAny(lower, "qr code", "generate a qr code", "generate qr", "make qr", "create qr", "wifi qr") -> ToolIntent.QrGenerator
 
            containsAny(lower, "scan qr", "read barcode", "barcode scanner", "qr scanner") -> ToolIntent.QrScanner
 
            containsAny(lower, "unit converter", "convert units", "unit conversion", "astronomy units", "light years", "parsec", "metric converter", "celsius to fahrenheit") -> ToolIntent.UnitConverter
 
            containsAny(lower, "calculator", "scientific calculator", "calc", "trig", "keypad calc", "math calculator") -> ToolIntent.Calculator
 
            containsAny(lower, "everyday calculator", "split bill", "tip calculator", "sales tax", "compound interest", "bmi calculator", "fuel trip", "discount calculator", "margin calculator", "vat") -> ToolIntent.EverydayCalculators
 
            containsAny(lower, "currency", "exchange rate", "time zone", "world clock", "world time", "clock") -> ToolIntent.CurrencyAndTime
 
            containsAny(lower, "batch rename", "rename files", "rename photos", "batch file renamer") -> ToolIntent.BatchRename
 
            containsAny(lower, "storage cleaner", "find duplicate photos", "clean storage", "duplicate files", "duplicate", "cleaner", "cache cleaner", "clean cache", "cache", "storage analyzer", "storage", "large files") -> ToolIntent.StorageCleaner

            containsAny(lower, "word count", "character count", "text cleaner", "uppercase", "lowercase", "text tools", "reading time", "deduplicate lines") -> ToolIntent.TextTools

            containsAny(lower, "developer tools", "json format", "base64", "jwt", "hash generator", "sha-256", "unix timestamp", "url encode") -> ToolIntent.DeveloperTools

            containsAny(lower, "password generator", "generate password", "secure password", "create password", "combinations") -> ToolIntent.PasswordGenerator

            containsAny(lower, "cool fonts", "font", "fancy text", "text styler", "bio font", "gothic", "cursive", "small caps", "name font", "japanese font") -> ToolIntent.CoolFonts

            else -> ToolIntent.Unknown
        }
    }

    private fun containsAny(text: String, vararg phrases: String): Boolean {
        return phrases.any { text.contains(it) }
    }

    private fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0) {
            String.format(Locale.US, "%,d", num.toLong())
        } else {
            String.format(Locale.US, "%,.2f", num).trimEnd('0').trimEnd('.')
        }
    }
}
