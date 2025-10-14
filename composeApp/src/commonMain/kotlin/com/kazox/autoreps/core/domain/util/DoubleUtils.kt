package com.kazox.autoreps.core.domain.util

import kotlin.math.pow
import kotlin.math.round

fun Double.format(decimals: Int = 0): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor

    return if (decimals == 0) {
        rounded.toInt().toString()
    } else {
        val parts = rounded.toString().split('.')
        val integerPart = parts[0]
        val fractionPart = parts.getOrNull(1)?.padEnd(decimals, '0') ?: "0".repeat(decimals)
        "$integerPart.${fractionPart.take(decimals)}"
    }
}
