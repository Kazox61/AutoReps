package com.kazox.autoreps.feature.workout.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toLocalDateTime

@Entity
data class Workout(
    var name: String? = null,
    val reps: Int,
    val startedAt: String,
    val duration: Int,
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
) {
    val startedDateTime: LocalDateTime
        get() = LocalDateTime.parse(startedAt)
}

class InvalidWorkoutException(message: String) : Exception(message)