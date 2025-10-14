package com.kazox.autoreps.core.domain.util

import kotlin.math.pow
import kotlin.math.round

fun Double.format(decimals: Int = 0): String {
    val factor = 10.0.pow(decimals)
    val rounded = round(this * factor) / factor
    return buildString {
        append(rounded)
        if (decimals > 0) {
            val parts = rounded.toString().split('.')
            val fraction = parts.getOrNull(1)?.padEnd(decimals, '0') ?: "0".repeat(decimals)
            append(parts[0])
            append('.')
            append(fraction.take(decimals))
        }
    }
}
