package com.kazox.autoreps.feature.workout.domain.util

import com.kazox.autoreps.feature.workout.domain.model.Workout
import kotlinx.datetime.LocalDateTime


val Workout.startedDateTime: LocalDateTime
    get() = LocalDateTime.parse(startedAt)
