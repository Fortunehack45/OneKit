package com.one.utility

import com.one.utility.core.processing.UniversalCalculatorEngine
import com.one.utility.core.processing.parseFlexibleDouble
import com.one.utility.core.processing.parseFlexibleInt
import org.junit.Assert.assertEquals
import org.junit.Test

class UniversalCalculatorEngineTest {

    private val engine = UniversalCalculatorEngine()

    @Test
    fun testPercentageCalculation() {
        // "17% of 850000"
        val result = engine.calculatePercentage(17.0, 850000.0)
        assertEquals(144500.0, result, 0.001)
    }

    @Test
    fun testPercentageRatio() {
        val result = engine.calculatePercentageRatio(25.0, 100.0)
        assertEquals(25.0, result, 0.001)
    }

    @Test
    fun testDiscountCalculation() {
        val res = engine.calculateDiscount(200.0, 20.0)
        assertEquals(160.0, res.finalPrice, 0.001)
        assertEquals(40.0, res.savedAmount, 0.001)
    }

    @Test
    fun testSplitBill() {
        val res = engine.calculateSplitBill(100.0, 15.0, 2)
        assertEquals(15.0, res.totalTip, 0.001)
        assertEquals(115.0, res.grandTotal, 0.001)
        assertEquals(57.5, res.perPersonAmount, 0.001)
    }

    @Test
    fun testUnitConversions() {
        // 25 miles to km
        val km = engine.convertUnits(25.0, "miles", "km")
        assertEquals(40.2336, km, 0.01)

        // 10 kg to lbs
        val lbs = engine.convertUnits(10.0, "kg", "lbs")
        assertEquals(22.0462, lbs, 0.01)

        // 0 Celsius to Fahrenheit
        val fahr = engine.convertUnits(0.0, "c", "f")
        assertEquals(32.0, fahr, 0.01)

        // 1 GB to MB
        val mb = engine.convertUnits(1.0, "gb", "mb")
        assertEquals(1024.0, mb, 0.01)
    }

    @Test
    fun testMathExpressionEvaluation() {
        assertEquals(76500.0, engine.evaluateExpression("450000 * 0.17"), 0.001)
        assertEquals(25.0, engine.evaluateExpression("(10 + 15)"), 0.001)
        assertEquals(50.0, engine.evaluateExpression("100 / 2"), 0.001)
        // Unicode operators
        assertEquals(50.0, engine.evaluateExpression("100 ÷ 2"), 0.001)
        assertEquals(200.0, engine.evaluateExpression("100 × 2"), 0.001)
        assertEquals(80.0, engine.evaluateExpression("100 − 20"), 0.001)
        // Thousands grouping commas
        assertEquals(3500.0, engine.evaluateExpression("1,000 + 2,500"), 0.001)
        // European comma decimals
        assertEquals(15.0, engine.evaluateExpression("12,5 + 2,5"), 0.001)
    }

    @Test
    fun testFlexibleDoubleParsing() {
        assertEquals(1234.56, "1,234.56".parseFlexibleDouble()!!, 0.001)
        assertEquals(1234.56, "1.234,56".parseFlexibleDouble()!!, 0.001)
        assertEquals(12.5, "12,5".parseFlexibleDouble()!!, 0.001)
        assertEquals(12.5, "12.5".parseFlexibleDouble()!!, 0.001)
        assertEquals(1000.0, "1,000".parseFlexibleDouble()!!, 0.001)
        assertEquals(null, "abc".parseFlexibleDouble())
        assertEquals(null, "".parseFlexibleDouble())
    }

    @Test
    fun testFlexibleIntParsing() {
        assertEquals(1000, "1,000".parseFlexibleInt())
        assertEquals(250, "250".parseFlexibleInt())
        assertEquals(null, "abc".parseFlexibleInt())
    }
}
