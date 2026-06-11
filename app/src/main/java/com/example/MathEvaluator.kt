package com.example

import kotlin.math.*

class MathEvaluator(private val isDegreeMode: Boolean = true) {

    private var pos = -1
    private var ch = 0
    private var str = ""

    private fun nextChar() {
        ch = if (++pos < str.length) str[pos].code else -1
    }

    private fun eat(charToEat: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == charToEat) {
            nextChar()
            return true
        }
        return false
    }

    fun parse(expression: String): Double {
        // Preprocess string
        str = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "3.141592653589793")
            .replace("e", "2.718281828459045")
        
        pos = -1
        nextChar()
        val x = parseExpression()
        if (pos < str.length) {
            throw IllegalArgumentException("Unexpected character downstream: " + ch.toChar())
        }
        return x
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor | term `nCr` factor | term `nPr` factor
    // factor = `+` factor | `-` factor | base `^` factor
    // base = ( expression ) | number | functionName ( expression ) | base `!` | base `%`

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm() // addition
            else if (eat('-'.code)) x -= parseTerm() // subtraction
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor() // multiplication
            else if (eat('/'.code)) {
                val next = parseFactor()
                if (next == 0.0) throw ArithmeticException("Division by Zero")
                x /= next // division
            } else if (eat('C'.code)) {
                val r = parseFactor()
                x = combination(x, r)
            } else if (eat('P'.code)) {
                val r = parseFactor()
                x = permutation(x, r)
            } else return x
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor() // unary plus
        if (eat('-'.code)) return -parseFactor() // unary minus

        var x: Double
        val startPos = this.pos
        if (eat('('.code)) { // parentheses
            x = parseExpression()
            eat(')'.code)
            x = parseSuffixes(x)
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) { // numbers
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            x = str.substring(startPos, this.pos).toDouble()
            x = parseSuffixes(x)
        } else if (ch >= 'a'.code && ch <= 'z'.code || ch >= 'A'.code && ch <= 'Z'.code || ch == '√'.code) { // functions
            while (ch >= 'a'.code && ch <= 'z'.code || ch >= 'A'.code && ch <= 'Z'.code) nextChar()
            var func = str.substring(startPos, this.pos)
            if (ch == '√'.code) {
                func = "sqrt"
                nextChar()
            }
            
            val hasParentheses = eat('('.code)
            var arg = parseExpression()
            if (hasParentheses) {
                eat(')'.code)
            }
            
            x = when (func.lowercase()) {
                "sqrt", "√" -> {
                    if (arg < 0) throw ArithmeticException("Negative Square Root")
                    sqrt(arg)
                }
                "sin" -> sin(if (isDegreeMode) Math.toRadians(arg) else arg)
                "cos" -> cos(if (isDegreeMode) Math.toRadians(arg) else arg)
                "tan" -> {
                    val r = if (isDegreeMode) Math.toRadians(arg) else arg
                    if (abs(cos(r)) < 1e-10) throw ArithmeticException("Tangent Undefined")
                    tan(r)
                }
                "asin" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain Error")
                    val res = asin(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "acos" -> {
                    if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain Error")
                    val res = acos(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "atan" -> {
                    val res = atan(arg)
                    if (isDegreeMode) Math.toDegrees(res) else res
                }
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "log" -> {
                    if (arg <= 0) throw ArithmeticException("Domain Error")
                    log10(arg)
                }
                "ln" -> {
                    if (arg <= 0) throw ArithmeticException("Domain Error")
                    ln(arg)
                }
                else -> throw IllegalArgumentException("Unknown function: $func")
            }
            x = parseSuffixes(x)
        } else {
            throw IllegalArgumentException("Unexpected character: " + ch.toChar())
        }

        if (eat('^'.code)) x = x.pow(parseFactor()) // exponentiation

        return x
    }

    private fun parseSuffixes(baseVal: Double): Double {
        var res = baseVal
        while (true) {
            if (eat('!'.code)) {
                res = factorial(res)
            } else if (eat('%'.code)) {
                res = res / 100.0
            } else {
                break
            }
        }
        return res
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n)) throw ArithmeticException("Factorial needs positive integer")
        if (n > 100) throw ArithmeticException("Factorial Overflow")
        var result = 1.0
        for (i in 1..n.toInt()) {
            result *= i
        }
        return result
    }

    private fun combination(n: Double, r: Double): Double {
        val nInt = n.toInt()
        val rInt = r.toInt()
        if (nInt < 0 || rInt < 0 || rInt > nInt || n != floor(n) || r != floor(r)) {
            throw ArithmeticException("Invalid Combination input")
        }
        return factorial(n) / (factorial(r) * factorial(n - r))
    }

    private fun permutation(n: Double, r: Double): Double {
        val nInt = n.toInt()
        val rInt = r.toInt()
        if (nInt < 0 || rInt < 0 || rInt > nInt || n != floor(n) || r != floor(r)) {
            throw ArithmeticException("Invalid Permutation input")
        }
        return factorial(n) / factorial(n - r)
    }

    companion object {
        // Statistical helper functions helper
        fun calculateMean(numbers: List<Double>): Double {
            if (numbers.isEmpty()) return 0.0
            return numbers.sum() / numbers.size
        }

        fun calculateMedian(numbers: List<Double>): Double {
            if (numbers.isEmpty()) return 0.0
            val sorted = numbers.sorted()
            val size = sorted.size
            return if (size % 2 == 1) {
                sorted[size / 2]
            } else {
                (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
            }
        }

        fun calculateVariance(numbers: List<Double>): Double {
            if (numbers.size <= 1) return 0.0
            val mean = calculateMean(numbers)
            val sumOfSquares = numbers.sumOf { (it - mean).pow(2) }
            return sumOfSquares / (numbers.size - 1) // Sample variance
        }

        fun calculateStdDev(numbers: List<Double>): Double {
            return sqrt(calculateVariance(numbers))
        }

        fun calculateSum(numbers: List<Double>): Double {
            return numbers.sum()
        }
    }
}
