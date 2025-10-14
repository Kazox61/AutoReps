package com.kazox.autoreps.feature.workout.domain.util

import com.kazox.autoreps.feature.workout.domain.model.Workout
import kotlinx.datetime.LocalDateTime

fun Workout.getStartHour(): Int {
    val localDateTime = LocalDateTime.parse(startedAt)
    return localDateTime.hour
}
