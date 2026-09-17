package com.one.utility.core.processing

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

class UniversalCalculatorEngine {

    // 1. Percentage: X% of Y
    fun calculatePercentage(percent: Double, total: Double): Double {
        return (percent / 100.0) * total
    }

    // 2. What % is X of Y?
    fun calculatePercentageRatio(part: Double, total: Double): Double {
        if (total == 0.0) return 0.0
        return (part / total) * 100.0
    }

    // 3. Discount Calculator
    data class DiscountResult(
        val finalPrice: Double,
        val savedAmount: Double
    )

    fun calculateDiscount(originalPrice: Double, discountPercent: Double): DiscountResult {
        val saved = (discountPercent / 100.0) * originalPrice
        val finalPrice = (originalPrice - saved).coerceAtLeast(0.0)
        return DiscountResult(finalPrice, saved)
    }

    // 4. Tip & Split Bill
    data class SplitBillResult(
        val totalTip: Double,
        val grandTotal: Double,
        val perPersonAmount: Double
    )

    fun calculateSplitBill(billAmount: Double, tipPercent: Double, numberOfPeople: Int): SplitBillResult {
        val count = numberOfPeople.coerceAtLeast(1)
        val tip = (tipPercent / 100.0) * billAmount
        val total = billAmount + tip
        val perPerson = total / count
        return SplitBillResult(tip, total, perPerson)
    }

    // 5. Loan Payment (Standard Amortization Formula)
    data class LoanResult(
        val monthlyPayment: Double,
        val totalPayment: Double,
        val totalInterest: Double
    )

    fun calculateLoan(principal: Double, annualRatePercent: Double, termMonths: Int): LoanResult {
        if (termMonths <= 0) return LoanResult(0.0, 0.0, 0.0)
        val monthlyRate = (annualRatePercent / 100.0) / 12.0
        if (monthlyRate == 0.0) {
            val monthly = principal / termMonths
            return LoanResult(monthly, principal, 0.0)
        }
        val factor = (1.0 + monthlyRate).pow(termMonths.toDouble())
        val monthlyPayment = principal * (monthlyRate * factor) / (factor - 1.0)
        val totalPayment = monthlyPayment * termMonths
        val totalInterest = totalPayment - principal
        return LoanResult(monthlyPayment, totalPayment, totalInterest)
    }

    // 6. Unit Conversion Engine
    fun convertUnits(value: Double, fromUnit: String, toUnit: String): Double {
        val f = fromUnit.lowercase().trim()
        val t = toUnit.lowercase().trim()

        // Temperature
        if ((f == "c" || f == "celsius") && (t == "f" || t == "fahrenheit")) return (value * 9.0 / 5.0) + 32.0
        if ((f == "f" || f == "fahrenheit") && (t == "c" || t == "celsius")) return (value - 32.0) * 5.0 / 9.0

        // Length
        val lengthInMeters = when (f) {
            "km", "kilometer", "kilometers" -> value * 1000.0
            "m", "meter", "meters" -> value
            "mile", "miles", "mi" -> value * 1609.344
            "ft", "foot", "feet" -> value * 0.3048
            "inch", "inches", "in" -> value * 0.0254
            else -> null
        }

        if (lengthInMeters != null) {
            return when (t) {
                "km", "kilometer", "kilometers" -> lengthInMeters / 1000.0
                "m", "meter", "meters" -> lengthInMeters
                "mile", "miles", "mi" -> lengthInMeters / 1609.344
                "ft", "foot", "feet" -> lengthInMeters / 0.3048
                "inch", "inches", "in" -> lengthInMeters / 0.0254
                else -> value
            }
        }

        // Mass / Weight
        val massInKg = when (f) {
            "kg", "kilogram", "kilograms" -> value
            "g", "gram", "grams" -> value / 1000.0
            "lb", "lbs", "pound", "pounds" -> value * 0.45359237
            "oz", "ounce", "ounces" -> value * 0.028349523
            else -> null
        }

        if (massInKg != null) {
            return when (t) {
                "kg", "kilogram", "kilograms" -> massInKg
                "g", "gram", "grams" -> massInKg * 1000.0
                "lb", "lbs", "pound", "pounds" -> massInKg / 0.45359237
                "oz", "ounce", "ounces" -> massInKg / 0.028349523
                else -> value
            }
        }

        // Digital Data Storage
        val dataInBytes = when (f) {
            "b", "byte", "bytes" -> value
            "kb" -> value * 1024.0
            "mb" -> value * 1024.0 * 1024.0
            "gb" -> value * 1024.0 * 1024.0 * 1024.0
            "tb" -> value * 1024.0 * 1024.0 * 1024.0 * 1024.0
            else -> null
        }

        if (dataInBytes != null) {
            return when (t) {
                "b", "byte", "bytes" -> dataInBytes
                "kb" -> dataInBytes / 1024.0
                "mb" -> dataInBytes / (1024.0 * 1024.0)
                "gb" -> dataInBytes / (1024.0 * 1024.0 * 1024.0)
                "tb" -> dataInBytes / (1024.0 * 1024.0 * 1024.0 * 1024.0)
                else -> value
            }
        }

        return value
    }

    // 7. Lightweight Math Expression Evaluator
    fun evaluateExpression(expr: String): Double {
        val sanitized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace(" ", "")
            .replace(Regex("""(?<=\d),(?=\d{3}(?!\d))"""), "")
            .replace(',', '.')
        return MathParser(sanitized).parse()
    }

    private class MathParser(private val str: String) {
        private var pos = -1
        private var ch = ' '

        private fun nextChar() {
            pos++
            ch = if (pos < str.length) str[pos] else '\u0000'
        }

        private fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw IllegalArgumentException("Unexpected: '$ch'")
            return x
        }

        private fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+') -> x += parseTerm()
                    eat('-') -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        private fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*') -> x *= parseFactor()
                    eat('/') -> {
                        val divisor = parseFactor()
                        if (divisor == 0.0) throw ArithmeticException("Division by zero")
                        x /= divisor
                    }
                    eat('%') -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        private fun parseFactor(): Double {
            if (eat('+')) return +parseFactor()
            if (eat('-')) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('(')) {
                x = parseExpression()
                eat(')')
            } else if ((ch in '0'..'9') || ch == '.') {
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch in 'a'..'z') {
                while (ch in 'a'..'z') nextChar()
                val func = str.substring(startPos, pos)
                if (eat('(')) {
                    x = parseExpression()
                    eat(')')
                } else {
                    x = parseFactor()
                }
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(Math.toRadians(x))
                    "cos" -> cos(Math.toRadians(x))
                    "tan" -> tan(Math.toRadians(x))
                    "ln" -> ln(x)
                    "log" -> log10(x)
                    else -> throw RuntimeException("Unknown function: $func")
                }
            } else {
                throw RuntimeException("Unexpected character: $ch")
            }

            if (eat('^')) x = x.pow(parseFactor())

            return x
        }
    }
}

/**
 * Robustly parses numbers from user input across diverse locales (e.g. "12.5" vs "12,5" vs "1,250.00").
 * Converts decimal commas to periods while safely honoring thousands grouping.
 */
fun String.parseFlexibleDouble(): Double? {
    val clean = this.trim()
    if (clean.isEmpty()) return null
    return if (clean.contains(',') && clean.contains('.')) {
        if (clean.lastIndexOf('.') > clean.lastIndexOf(',')) {
            // e.g. "1,234.56"
            clean.replace(",", "").toDoubleOrNull()
        } else {
            // e.g. "1.234,56"
            clean.replace(".", "").replace(',', '.').toDoubleOrNull()
        }
    } else if (clean.contains(',')) {
        if (clean.matches(Regex("""^-?\d{1,3}(,\d{3})+$"""))) {
            // e.g. "1,000" or "850,000"
            clean.replace(",", "").toDoubleOrNull()
        } else {
            // e.g. "12,5" or "0,75"
            clean.replace(',', '.').toDoubleOrNull()
        }
    } else {
        clean.toDoubleOrNull()
    }
}

/**
 * Robustly parses integers from user input, tolerating fractional representations like "2.0".
 */
fun String.parseFlexibleInt(): Int? {
    val clean = this.trim()
    if (clean.isEmpty()) return null
    return clean.toIntOrNull() ?: clean.parseFlexibleDouble()?.toInt()
}
