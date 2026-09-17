package com.one.utility

import com.one.utility.core.router.ToolIntent
import com.one.utility.core.router.ToolRouter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ToolRouterTest {

    private val router = ToolRouter()

    @Test
    fun testPercentageIntent() {
        val intent = router.resolve("17% of 850000")
        assertTrue(intent is ToolIntent.PercentageCalculation)
        val calc = intent as ToolIntent.PercentageCalculation
        assertEquals(17.0, calc.percent, 0.001)
        assertEquals(850000.0, calc.total, 0.001)
    }

    @Test
    fun testPercentageWithDecimals() {
        val intent = router.resolve("15.5% of 1200")
        assertTrue(intent is ToolIntent.PercentageCalculation)
        val calc = intent as ToolIntent.PercentageCalculation
        assertEquals(15.5, calc.percent, 0.001)
        assertEquals(1200.0, calc.total, 0.001)
    }

    @Test
    fun testUnitConversionIntent() {
        val intent = router.resolve("25 miles to km")
        assertTrue(intent is ToolIntent.UnitConversion)
        val unit = intent as ToolIntent.UnitConversion
        assertEquals(25.0, unit.value, 0.001)
        assertEquals("miles", unit.fromUnit)
        assertEquals("km", unit.toUnit)
    }

    @Test
    fun testImageToPdfNaturalLanguage() {
        assertEquals(ToolIntent.ImageToPdf, router.resolve("turn these photos into a pdf"))
        assertEquals(ToolIntent.ImageToPdf, router.resolve("make these pictures a pdf"))
        assertEquals(ToolIntent.ImageToPdf, router.resolve("image to pdf"))
    }

    @Test
    fun testBackgroundRemoverIntent() {
        assertEquals(ToolIntent.BackgroundRemover, router.resolve("remove background from photo"))
        assertEquals(ToolIntent.BackgroundRemover, router.resolve("cutout"))
    }

    @Test
    fun testCompressorIntent() {
        assertEquals(ToolIntent.ImageCompressor, router.resolve("make this image smaller"))
        assertEquals(ToolIntent.ImageCompressor, router.resolve("compress photo"))
    }

    @Test
    fun testChainedWorkflowIntent() {
        val intent = router.resolve("make these pictures smaller and put them in a pdf")
        assertTrue(intent is ToolIntent.ChainedWorkflow)
    }

    @Test
    fun testDirectMathIntent() {
        val intent = router.resolve("450000 * 0.17")
        assertTrue(intent is ToolIntent.MathCalculation)
        assertEquals("450000 * 0.17", (intent as ToolIntent.MathCalculation).expression)
    }

    @Test
    fun testPercentageWithCurrencySymbolAndQuestion() {
        val intent = router.resolve("What's 15% of ₦850,000?")
        assertTrue(intent is ToolIntent.PercentageCalculation)
        val calc = intent as ToolIntent.PercentageCalculation
        assertEquals(15.0, calc.percent, 0.001)
        assertEquals(850000.0, calc.total, 0.001)
        assertEquals("₦127,500", calc.formattedResult)
    }

    @Test
    fun testDigitalDataUnitConversion() {
        val intent = router.resolve("Convert 25 GB to MB")
        assertTrue(intent is ToolIntent.UnitConversion)
        val conv = intent as ToolIntent.UnitConversion
        assertEquals(25.0, conv.value, 0.001)
        assertEquals("gb", conv.fromUnit)
        assertEquals("mb", conv.toUnit)
        assertEquals("25,600 MB", conv.formattedResult)
    }

    @Test
    fun testMakePhotosSmallerAndTurnIntoPdfWorkflow() {
        val intent = router.resolve("Make these photos smaller and turn them into a PDF")
        assertTrue(intent is ToolIntent.ChainedWorkflow)
        val workflow = intent as ToolIntent.ChainedWorkflow
        assertEquals(listOf("resizer", "compressor", "image_to_pdf"), workflow.stepNames)
        assertEquals("Select photos → Resize → Compress → PDF → Share", workflow.displayPipeline)
    }

    @Test
    fun testRemoveBackgroundAndMake1080pxWorkflow() {
        val intent = router.resolve("Remove the background and make it 1080px")
        assertTrue(intent is ToolIntent.ChainedWorkflow)
        val workflow = intent as ToolIntent.ChainedWorkflow
        assertEquals(listOf("background_remover", "resizer"), workflow.stepNames)
        assertEquals("Select image → Background Removal → Resize → Save", workflow.displayPipeline)
    }

    @Test
    fun testScanToPdfWorkflow() {
        val intent = router.resolve("Scan to pdf")
        assertTrue(intent is ToolIntent.ChainedWorkflow)
        val workflow = intent as ToolIntent.ChainedWorkflow
        assertEquals(listOf("document_scanner", "image_to_pdf"), workflow.stepNames)
    }

    @Test
    fun testPdfToolboxRouting() {
        assertEquals(ToolIntent.PdfSplitter, router.resolve("split pdf"))
        assertEquals(ToolIntent.PdfSplitter, router.resolve("pdf to image"))
        assertEquals(ToolIntent.PdfSplitter, router.resolve("pdf to images"))
        assertEquals(ToolIntent.PdfMerger, router.resolve("merge pdf"))
    }

    @Test
    fun testStorageCleanerRouting() {
        assertEquals(ToolIntent.StorageCleaner, router.resolve("duplicate files"))
        assertEquals(ToolIntent.StorageCleaner, router.resolve("clean cache"))
        assertEquals(ToolIntent.StorageCleaner, router.resolve("storage cleaner"))
    }

    @Test
    fun testCurrencyAndTimeRouting() {
        assertEquals(ToolIntent.CurrencyAndTime, router.resolve("currency"))
        assertEquals(ToolIntent.CurrencyAndTime, router.resolve("world clock"))
    }
}
