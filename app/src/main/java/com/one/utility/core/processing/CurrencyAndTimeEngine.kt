package com.one.utility.core.processing

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

data class TimeZoneConversionResult(
    val fromZone: String,
    val fromTimeFormatted: String,
    val toZone: String,
    val toTimeFormatted: String,
    val hourDifference: Double
)

class CurrencyAndTimeEngine {

    /**
     * Converts currency based on user-entered exchange rate.
     * Guaranteed 100% offline, displaying transparent rate provenance.
     */
    fun convertCurrency(
        amount: Double,
        exchangeRate: Double
    ): Double {
        return amount * exchangeRate
    }

    /**
     * Converts a specific time from one time-zone to another
     * using the device's local tzdb (e.g. Africa/Lagos -> Asia/Tokyo).
     */
    fun convertTimeZone(
        time: LocalTime,
        fromZoneIdStr: String,
        toZoneIdStr: String
    ): TimeZoneConversionResult {
        val fromZone = ZoneId.of(fromZoneIdStr)
        val toZone = ZoneId.of(toZoneIdStr)

        val nowInFrom = ZonedDateTime.now(fromZone).with(time)
        val convertedInTo = nowInFrom.withZoneSameInstant(toZone)

        val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        val diffHours = (convertedInTo.offset.totalSeconds - nowInFrom.offset.totalSeconds) / 3600.0

        return TimeZoneConversionResult(
            fromZone = fromZoneIdStr,
            fromTimeFormatted = nowInFrom.format(formatter),
            toZone = toZoneIdStr,
            toTimeFormatted = convertedInTo.format(formatter),
            hourDifference = diffHours
        )
    }

    /**
     * List of major world time-zone identifiers.
     */
    fun getMajorTimeZones(): List<String> = listOf(
        "UTC",
        "Africa/Lagos",
        "America/New_York",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Dubai",
        "Asia/Tokyo",
        "Asia/Singapore",
        "Australia/Sydney"
    )
}
