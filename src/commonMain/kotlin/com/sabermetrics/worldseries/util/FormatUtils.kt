package com.sabermetrics.worldseries.util

import kotlin.math.abs
import kotlin.math.round

/**
 * Formats a Double value to a specified number of decimal places in Kotlin Common code.
 */
fun Double.formatDecimals(digits: Int): String {
    if (this.isNaN()) return "NaN"
    if (this.isInfinite()) return if (this > 0) "Infinity" else "-Infinity"

    var p = 1.0
    repeat(digits) { p *= 10.0 }
    val rounded = round(abs(this) * p) / p
    val raw = rounded.toString()
    val parts = raw.split('.')
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else ""
    val sign = if (this < 0 && rounded != 0.0) "-" else ""
    val formattedDec = if (digits <= 0) "" else "." + decPart.padEnd(digits, '0').take(digits)
    return "$sign$intPart$formattedDec"
}

/**
 * Cross-platform String.format extension for common Kotlin Multiplatform usage.
 */
fun String.format(vararg args: Any?): String {
    var result = this
    for (arg in args) {
        val regex = Regex("%(?:\\.(\\d+))?f")
        val match = regex.find(result)
        if (match != null && arg is Number) {
            val decimals = match.groupValues[1].toIntOrNull() ?: 6
            val formatted = arg.toDouble().formatDecimals(decimals)
            result = result.replaceFirst(match.value, formatted)
        } else {
            result = result.replaceFirst("%s", arg.toString())
        }
    }
    return result
}
