package com.one.utility.core.processing

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

class TactileCalculatorEngine {

    var isDegreeMode: Boolean = true

    fun evaluate(expression: String): Result<Double> {
        return runCatching {
            val sanitized = expression
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("π", Math.PI.toString())
                .replace("e", Math.E.toString())

            val tokens = tokenize(sanitized)
            val postfix = infixToPostfix(tokens)
            evaluatePostfix(postfix)
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++
                c in "0123456789." -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                c in "+-*/^!()" -> {
                    // Check for unary minus: at start or after an operator / open parenthesis
                    if (c == '-' && (tokens.isEmpty() || tokens.last() in "+-*/^(," || tokens.last() == "sin" || tokens.last() == "cos" || tokens.last() == "tan" || tokens.last() == "log" || tokens.last() == "ln" || tokens.last() == "sqrt")) {
                        tokens.add("u-")
                    } else {
                        tokens.add(c.toString())
                    }
                    i++
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < expr.length && expr[i].isLetter()) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                }
                else -> i++
            }
        }
        return tokens
    }

    private fun precedence(op: String): Int {
        return when (op) {
            "u-" -> 5
            "^", "!" -> 4
            "*", "/" -> 3
            "+", "-" -> 2
            else -> 0
        }
    }

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = ArrayDeque<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token in listOf("sin", "cos", "tan", "log", "ln", "sqrt") -> stack.addFirst(token)
                token == "(" -> stack.addFirst(token)
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.first() != "(") {
                        output.add(stack.removeFirst())
                    }
                    if (stack.isNotEmpty() && stack.first() == "(") {
                        stack.removeFirst()
                    }
                    if (stack.isNotEmpty() && stack.first() in listOf("sin", "cos", "tan", "log", "ln", "sqrt")) {
                        output.add(stack.removeFirst())
                    }
                }
                token == "!" -> output.add(token) // Postfix factorial
                else -> {
                    while (stack.isNotEmpty() && stack.first() != "(" &&
                        precedence(stack.first()) >= precedence(token)) {
                        output.add(stack.removeFirst())
                    }
                    stack.addFirst(token)
                }
            }
        }

        while (stack.isNotEmpty()) {
            output.add(stack.removeFirst())
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = ArrayDeque<Double>()

        for (token in postfix) {
            val num = token.toDoubleOrNull()
            if (num != null) {
                stack.addFirst(num)
                continue
            }

            when (token) {
                "u-" -> {
                    val a = stack.removeFirst()
                    stack.addFirst(-a)
                }
                "!" -> {
                    val a = stack.removeFirst().toInt()
                    stack.addFirst(factorial(a).toDouble())
                }
                "sin" -> {
                    val a = stack.removeFirst()
                    val rad = if (isDegreeMode) Math.toRadians(a) else a
                    stack.addFirst(sin(rad))
                }
                "cos" -> {
                    val a = stack.removeFirst()
                    val rad = if (isDegreeMode) Math.toRadians(a) else a
                    stack.addFirst(cos(rad))
                }
                "tan" -> {
                    val a = stack.removeFirst()
                    val rad = if (isDegreeMode) Math.toRadians(a) else a
                    stack.addFirst(tan(rad))
                }
                "log" -> {
                    val a = stack.removeFirst()
                    stack.addFirst(log10(a))
                }
                "ln" -> {
                    val a = stack.removeFirst()
                    stack.addFirst(ln(a))
                }
                "sqrt" -> {
                    val a = stack.removeFirst()
                    stack.addFirst(sqrt(a))
                }
                "+", "-", "*", "/", "^" -> {
                    val b = stack.removeFirst()
                    val a = stack.removeFirst()
                    val res = when (token) {
                        "+" -> a + b
                        "-" -> a - b
                        "*" -> a * b
                        "/" -> if (b != 0.0) a / b else throw ArithmeticException("Division by zero")
                        "^" -> a.pow(b)
                        else -> 0.0
                    }
                    stack.addFirst(res)
                }
            }
        }

        return stack.firstOrNull() ?: 0.0
    }

    private fun factorial(n: Int): Long {
        if (n < 0) throw IllegalArgumentException("Negative factorial")
        var result = 1L
        for (i in 2..min(n, 20)) {
            result *= i
        }
        return result
    }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return try {
            val bd = BigDecimal(value, MathContext(10, RoundingMode.HALF_UP)).stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            String.format("%.6f", value).trimEnd('0').trimEnd('.')
        }
    }
}
