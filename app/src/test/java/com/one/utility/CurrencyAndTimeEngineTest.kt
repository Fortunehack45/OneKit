package com.one.utility

import com.one.utility.core.processing.CurrencyAndTimeEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class CurrencyAndTimeEngineTest {

    private val engine = CurrencyAndTimeEngine()

    @Test
    fun testCurrencyConversion() {
        val converted = engine.convertCurrency(amount = 250.0, exchangeRate = 1.08)
        assertEquals(270.0, converted, 0.001)
    }

    @Test
    fun testTimeZoneConversion() {
        val time = LocalTime.of(20, 0) // 8:00 PM
        val res = engine.convertTimeZone(time, "Africa/Lagos", "Asia/Tokyo")
        assertEquals("Africa/Lagos", res.fromZone)
        assertEquals("Asia/Tokyo", res.toZone)
        // Tokyo (UTC+9) is 8 hours ahead of Lagos (UTC+1)
        assertEquals(8.0, res.hourDifference, 0.1)
    }
}
