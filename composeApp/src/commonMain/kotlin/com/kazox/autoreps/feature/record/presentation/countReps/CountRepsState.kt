package com.kazox.autoreps.feature.record.presentation.countReps

import com.kazox.autoreps.feature.workout.domain.model.Rep
import kotlinx.datetime.LocalDateTime

data class CountRepsState (
    val startedDateTime: LocalDateTime? = null,
    val repCount: Int = 0,
    val reps: List<Rep> = emptyList(),
    val duration: Int = 0,
    val setId: Int = 0,
    val lastRepTimeStamp: Int = 0,
    val formattedTime: String = "00:00"
)