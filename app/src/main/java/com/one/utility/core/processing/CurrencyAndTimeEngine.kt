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

    data class WorldClockCity(
        val city: String,
        val country: String,
        val zoneId: String,
        val timeFormatted: String,
        val offsetFormatted: String,
        val dateFormatted: String
    )

    fun getWorldClockCities(): List<WorldClockCity> {
        val targets = listOf(
            Triple("London", "United Kingdom", "Europe/London"),
            Triple("New York", "United States", "America/New_York"),
            Triple("Tokyo", "Japan", "Asia/Tokyo"),
            Triple("Paris", "France", "Europe/Paris"),
            Triple("Dubai", "United Arab Emirates", "Asia/Dubai"),
            Triple("Lagos", "Nigeria", "Africa/Lagos"),
            Triple("Singapore", "Singapore", "Asia/Singapore"),
            Triple("Sydney", "Australia", "Australia/Sydney"),
            Triple("Los Angeles", "United States", "America/Los_Angeles"),
            Triple("UTC", "Coordinated Universal Time", "UTC")
        )

        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US)
        val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

        return targets.map { (city, country, zoneStr) ->
            val zone = ZoneId.of(zoneStr)
            val now = ZonedDateTime.now(zone)
            val offsetHours = now.offset.totalSeconds / 3600
            val offsetSign = if (offsetHours >= 0) "+" else ""
            WorldClockCity(
                city = city,
                country = country,
                zoneId = zoneStr,
                timeFormatted = now.format(timeFormatter),
                offsetFormatted = "UTC$offsetSign$offsetHours",
                dateFormatted = now.format(dateFormatter)
            )
        }
    }

    fun calculateBusinessDays(startDate: java.time.LocalDate, endDate: java.time.LocalDate): Long {
        val (start, end) = if (startDate.isAfter(endDate)) Pair(endDate, startDate) else Pair(startDate, endDate)
        var count = 0L
        var curr = start
        while (!curr.isAfter(end)) {
            val dayOfWeek = curr.dayOfWeek.value // 1 = Monday, 7 = Sunday
            if (dayOfWeek != 6 && dayOfWeek != 7) {
                count++
            }
            curr = curr.plusDays(1)
        }
        return count
    }

    fun getIsoWeekNumber(date: java.time.LocalDate): Int {
        val field = java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        return date.get(field)
    }

    fun addTimeToDate(date: java.time.LocalDate, days: Long, months: Long, years: Long): java.time.LocalDate {
        return date.plusYears(years).plusMonths(months).plusDays(days)
    }
}
