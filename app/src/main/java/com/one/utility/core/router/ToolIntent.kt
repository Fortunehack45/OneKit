package com.one.utility.core.router

sealed class ToolIntent {
    object ImageToPdf : ToolIntent()
    object BackgroundRemover : ToolIntent()
    object ImageCompressor : ToolIntent()
    object ImageResizer : ToolIntent()
    object ImageConverter : ToolIntent()
    object PdfMerger : ToolIntent()
    object PdfSplitter : ToolIntent()
    object QrGenerator : ToolIntent()
    object QrScanner : ToolIntent()
    object StorageCleaner : ToolIntent()
    object TextTools : ToolIntent()
    object DeveloperTools : ToolIntent()
    object PasswordGenerator : ToolIntent()
    object CoolFonts : ToolIntent()

    // Deterministic Math & Calculations
    data class MathCalculation(val expression: String) : ToolIntent()
    data class PercentageCalculation(val percent: Double, val total: Double) : ToolIntent()
    data class UnitConversion(val value: Double, val fromUnit: String, val toUnit: String) : ToolIntent()

    // Chained Multi-Tool Workflows (e.g. "Compress images and turn into PDF")
    data class ChainedWorkflow(val stepNames: List<String>) : ToolIntent()

    object Unknown : ToolIntent()
}
