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

data class LoanCalculationResult(
    val monthlyPayment: Double,
    val totalPayment: Double,
    val totalInterest: Double
)

data class MortgageCalculationResult(
    val loanAmount: Double,
    val monthlyPayment: Double,
    val totalPayment: Double,
    val totalInterest: Double
)

data class SavingsGoalResult(
    val monthlyDepositNeeded: Double,
    val totalDeposited: Double,
    val totalInterestEarned: Double
)

data class SavingsAccumulationResult(
    val totalWealth: Double,
    val totalDeposited: Double,
    val totalInterestEarned: Double
)

data class InvestmentReturnResult(
    val totalGain: Double,
    val totalRoiPercent: Double,
    val annualizedRoiPercent: Double
)

data class FractionCalculationResult(
    val numerator: Long,
    val denominator: Long,
    val decimalValue: Double,
    val mixedRepresentation: String
)

data class AverageCalculationResult(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double
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

    // 9. Loan & EMI Calculation
    fun calculateLoan(principal: Double, annualRatePercent: Double, termMonths: Int): LoanCalculationResult {
        if (principal <= 0.0 || termMonths <= 0) return LoanCalculationResult(0.0, 0.0, 0.0)
        val monthlyRate = (annualRatePercent / 100.0) / 12.0
        if (monthlyRate == 0.0) {
            val monthly = principal / termMonths
            return LoanCalculationResult(monthly, principal, 0.0)
        }
        val factor = (1.0 + monthlyRate).pow(termMonths.toDouble())
        val monthlyPayment = principal * (monthlyRate * factor) / (factor - 1.0)
        val totalPayment = monthlyPayment * termMonths
        val totalInterest = (totalPayment - principal).coerceAtLeast(0.0)
        return LoanCalculationResult(monthlyPayment, totalPayment, totalInterest)
    }

    // 10. Mortgage Calculator
    fun calculateMortgage(homePrice: Double, downPayment: Double, annualRatePercent: Double, termYears: Int): MortgageCalculationResult {
        val loanAmount = (homePrice - downPayment).coerceAtLeast(0.0)
        val months = termYears * 12
        val loanResult = calculateLoan(loanAmount, annualRatePercent, months)
        return MortgageCalculationResult(loanAmount, loanResult.monthlyPayment, loanResult.totalPayment, loanResult.totalInterest)
    }

    // 11. Savings Goal
    fun calculateSavingsGoal(targetAmount: Double, annualRatePercent: Double, years: Double): SavingsGoalResult {
        if (targetAmount <= 0.0 || years <= 0.0) return SavingsGoalResult(0.0, 0.0, 0.0)
        val months = years * 12.0
        val r = (annualRatePercent / 100.0) / 12.0
        val monthlyDeposit = if (r == 0.0) {
            targetAmount / months
        } else {
            targetAmount * r / ((1.0 + r).pow(months) - 1.0)
        }
        val totalDeposited = monthlyDeposit * months
        val totalInterest = (targetAmount - totalDeposited).coerceAtLeast(0.0)
        return SavingsGoalResult(monthlyDeposit, totalDeposited, totalInterest)
    }

    // 12. Savings Accumulation (Wealth growth with monthly contributions)
    fun calculateSavingsAccumulation(initialDeposit: Double, monthlyDeposit: Double, annualRatePercent: Double, years: Double): SavingsAccumulationResult {
        val months = (years * 12.0).toInt().coerceAtLeast(1)
        val r = (annualRatePercent / 100.0) / 12.0
        var balance = initialDeposit
        var totalDeposited = initialDeposit
        for (i in 0 until months) {
            balance += monthlyDeposit
            totalDeposited += monthlyDeposit
            balance += balance * r
        }
        val interest = (balance - totalDeposited).coerceAtLeast(0.0)
        return SavingsAccumulationResult(balance, totalDeposited, interest)
    }

    // 13. Investment Return (ROI & Annualized Return)
    fun calculateInvestmentReturn(initialInvestment: Double, finalValue: Double, years: Double): InvestmentReturnResult {
        if (initialInvestment <= 0.0) return InvestmentReturnResult(0.0, 0.0, 0.0)
        val gain = finalValue - initialInvestment
        val totalRoi = (gain / initialInvestment) * 100.0
        val annualizedRoi = if (years > 0.0 && finalValue > 0.0) {
            ((finalValue / initialInvestment).pow(1.0 / years) - 1.0) * 100.0
        } else {
            0.0
        }
        return InvestmentReturnResult(gain, totalRoi, annualizedRoi)
    }

    // 14. Ratio Calculator: a:b = c:d -> solves d
    fun calculateRatio(a: Double, b: Double, c: Double): Double {
        if (a == 0.0) return 0.0
        return (b * c) / a
    }

    // 15. Fraction Calculator
    fun calculateFraction(num1: Long, den1: Long, num2: Long, den2: Long, op: String): FractionCalculationResult {
        val d1 = if (den1 == 0L) 1L else den1
        val d2 = if (den2 == 0L) 1L else den2

        fun gcd(a: Long, b: Long): Long {
            var x = kotlin.math.abs(a)
            var y = kotlin.math.abs(b)
            while (y != 0L) {
                val t = y
                y = x % y
                x = t
            }
            return if (x == 0L) 1L else x
        }

        var resNum: Long
        var resDen: Long
        when (op) {
            "+" -> {
                resNum = num1 * d2 + num2 * d1
                resDen = d1 * d2
            }
            "-" -> {
                resNum = num1 * d2 - num2 * d1
                resDen = d1 * d2
            }
            "×", "*" -> {
                resNum = num1 * num2
                resDen = d1 * d2
            }
            "÷", "/" -> {
                resNum = num1 * d2
                resDen = d1 * (if (num2 == 0L) 1L else num2)
            }
            else -> {
                resNum = num1
                resDen = d1
            }
        }

        val g = gcd(resNum, resDen)
        resNum /= g
        resDen /= g
        if (resDen < 0) {
            resNum = -resNum
            resDen = -resDen
        }

        val decimal = if (resDen != 0L) resNum.toDouble() / resDen.toDouble() else 0.0
        val whole = resNum / resDen
        val rem = kotlin.math.abs(resNum % resDen)
        val mixed = if (resDen == 1L) {
            "$resNum"
        } else if (whole != 0L && rem != 0L) {
            "$whole $rem/$resDen"
        } else {
            "$resNum/$resDen"
        }

        return FractionCalculationResult(resNum, resDen, decimal, mixed)
    }

    // 16. Statistics / Average Calculator
    fun calculateAverage(numbers: List<Double>): AverageCalculationResult {
        if (numbers.isEmpty()) return AverageCalculationResult(0, 0.0, 0.0, 0.0, emptyList(), 0.0, 0.0, 0.0)
        val sorted = numbers.sorted()
        val count = sorted.size
        val sum = sorted.sum()
        val mean = sum / count
        val median = if (count % 2 == 1) {
            sorted[count / 2]
        } else {
            (sorted[count / 2 - 1] + sorted[count / 2]) / 2.0
        }
        val freqMap = numbers.groupingBy { it }.eachCount()
        val maxFreq = freqMap.values.maxOrNull() ?: 1
        val mode = if (maxFreq > 1) freqMap.filter { it.value == maxFreq }.keys.toList() else emptyList()
        val min = sorted.first()
        val max = sorted.last()
        val range = max - min
        return AverageCalculationResult(count, sum, mean, median, mode, min, max, range)
    }

    // 17. Speed / Distance / Time
    fun calculateSpeed(distanceKm: Double, timeHours: Double): Double = if (timeHours > 0.0) distanceKm / timeHours else 0.0
    fun calculateDistance(speedKmh: Double, timeHours: Double): Double = speedKmh * timeHours
    fun calculateTime(distanceKm: Double, speedKmh: Double): Double = if (speedKmh > 0.0) distanceKm / speedKmh else 0.0
}
