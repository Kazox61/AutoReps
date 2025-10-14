package com.kazox.autoreps.core.domain.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char

val dateFormat = LocalDate.Format {
    monthNumber(padding = Padding.SPACE)
    char('/')
    day()
    char(' ')
    year()
}