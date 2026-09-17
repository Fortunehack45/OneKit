package com.one.utility.core.processing

import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.pow

data class ProfitMarginResult(
    val profit: Double,
    val profitMarginPercent: Double,
    val markupPercent: Double
)

data class CompoundInterestResult(
    val totalAmount: Double,
    val totalInterestEarned: Double
)

data class BmiResult(
    val bmi: Double,
    val category: String
)

data class FuelCostResult(
    val totalCost: Double,
    val fuelNeeded: Double
)

data class DateDifferenceResult(
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long
)

data class DiscountResult(
    val finalPrice: Double,
    val savedAmount: Double
)

data class TipSplitResult(
    val tipAmount: Double,
    val grandTotal: Double,
    val perPersonAmount: Double
)

class EverydayCalculatorsEngine {

    // 1. Profit & Margin: Cost Price vs Selling Price
    fun calculateProfitAndMargin(costPrice: Double, sellingPrice: Double): ProfitMarginResult {
        val profit = sellingPrice - costPrice
        val margin = if (sellingPrice > 0) (profit / sellingPrice) * 100.0 else 0.0
        val markup = if (costPrice > 0) (profit / costPrice) * 100.0 else 0.0
        return ProfitMarginResult(profit, margin, markup)
    }

    // 2. Sales Tax / VAT
    fun calculateTax(amount: Double, taxRatePercent: Double): Pair<Double, Double> {
        val tax = (taxRatePercent / 100.0) * amount
        val total = amount + tax
        return Pair(tax, total)
    }

    // 3. Discount Calculator
    fun calculateDiscount(originalPrice: Double, discountPercent: Double): DiscountResult {
        val saved = (discountPercent / 100.0) * originalPrice
        val finalPrice = (originalPrice - saved).coerceAtLeast(0.0)
        return DiscountResult(finalPrice, saved)
    }

    // 4. Tip & Split Bill
    fun calculateTipAndSplit(billAmount: Double, tipPercent: Double, numberOfPeople: Int): TipSplitResult {
        val people = numberOfPeople.coerceAtLeast(1)
        val tip = (tipPercent / 100.0) * billAmount
        val grandTotal = billAmount + tip
        val perPerson = grandTotal / people
        return TipSplitResult(tip, grandTotal, perPerson)
    }

    // 5. Simple & Compound Interest
    fun calculateSimpleInterest(principal: Double, annualRatePercent: Double, timeYears: Double): Double {
        return principal * (annualRatePercent / 100.0) * timeYears
    }

    fun calculateCompoundInterest(
        principal: Double,
        annualRatePercent: Double,
        timeYears: Double,
        compoundingFrequencyPerYear: Int = 12
    ): CompoundInterestResult {
        val freq = compoundingFrequencyPerYear.coerceAtLeast(1)
        val r = (annualRatePercent / 100.0) / freq
        val n = freq * timeYears
        val totalAmount = principal * (1.0 + r).pow(n)
        val interestEarned = totalAmount - principal
        return CompoundInterestResult(totalAmount, interestEarned)
    }

    // 6. Body Mass Index (BMI)
    fun calculateBmi(weightKg: Double, heightCm: Double): BmiResult {
        if (heightCm <= 0.0 || weightKg <= 0.0) return BmiResult(0.0, "Invalid height or weight")
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
        return BmiResult(bmi, category)
    }

    // 7. Fuel Trip Cost
    fun calculateFuelCost(distanceKm: Double, fuelConsumptionPer100Km: Double, pricePerLiter: Double): FuelCostResult {
        val liters = (distanceKm / 100.0) * fuelConsumptionPer100Km
        val cost = liters * pricePerLiter
        return FuelCostResult(cost, liters)
    }

    // 8. Date Difference & Age Calculator
    fun calculateDateDifference(startDate: LocalDate, endDate: LocalDate): DateDifferenceResult {
        val (start, end) = if (startDate.isAfter(endDate)) Pair(endDate, startDate) else Pair(startDate, endDate)
        val period = Period.between(start, end)
        val totalDays = ChronoUnit.DAYS.between(start, end)
        return DateDifferenceResult(period.years, period.months, period.days, totalDays)
    }
}
