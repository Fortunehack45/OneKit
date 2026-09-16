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
}
