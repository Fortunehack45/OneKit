package com.one.utility

import com.one.utility.core.processing.EverydayCalculatorsEngine
import org.junit.Assert.assertEquals
import org.junit.Test

class EverydayCalculatorsTest {

    private val engine = EverydayCalculatorsEngine()

    @Test
    fun testProfitAndMargin() {
        val res = engine.calculateProfitAndMargin(costPrice = 100.0, sellingPrice = 150.0)
        assertEquals(50.0, res.profit, 0.001)
        assertEquals(33.333, res.profitMarginPercent, 0.01)
        assertEquals(50.0, res.markupPercent, 0.01)
    }

    @Test
    fun testSalesTax() {
        val (tax, total) = engine.calculateTax(amount = 200.0, taxRatePercent = 10.0)
        assertEquals(20.0, tax, 0.001)
        assertEquals(220.0, total, 0.001)
    }

    @Test
    fun testBmi() {
        val res = engine.calculateBmi(weightKg = 70.0, heightCm = 175.0)
        assertEquals(22.857, res.bmi, 0.01)
        assertEquals("Normal weight", res.category)
    }

    @Test
    fun testFuelCost() {
        val res = engine.calculateFuelCost(distanceKm = 500.0, fuelConsumptionPer100Km = 8.0, pricePerLiter = 1.50)
        assertEquals(40.0, res.fuelNeeded, 0.001)
        assertEquals(60.0, res.totalCost, 0.001)
    }

    @Test
    fun testDiscount() {
        val res = engine.calculateDiscount(originalPrice = 100.0, discountPercent = 20.0)
        assertEquals(80.0, res.finalPrice, 0.001)
        assertEquals(20.0, res.savedAmount, 0.001)
    }

    @Test
    fun testTipAndSplit() {
        val res = engine.calculateTipAndSplit(billAmount = 100.0, tipPercent = 15.0, numberOfPeople = 2)
        assertEquals(15.0, res.tipAmount, 0.001)
        assertEquals(115.0, res.grandTotal, 0.001)
        assertEquals(57.5, res.perPersonAmount, 0.001)
    }

    @Test
    fun testCompoundInterest() {
        val res = engine.calculateCompoundInterest(principal = 1000.0, annualRatePercent = 5.0, timeYears = 1.0, compoundingFrequencyPerYear = 1)
        assertEquals(1050.0, res.totalAmount, 0.01)
        assertEquals(50.0, res.totalInterestEarned, 0.01)
    }

    @Test
    fun testDateDifference() {
        val start = java.time.LocalDate.of(2023, 1, 1)
        val end = java.time.LocalDate.of(2024, 1, 1)
        val res = engine.calculateDateDifference(start, end)
        assertEquals(1, res.years)
        assertEquals(365L, res.totalDays)
    }
}
