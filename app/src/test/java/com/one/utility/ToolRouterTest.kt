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
}
