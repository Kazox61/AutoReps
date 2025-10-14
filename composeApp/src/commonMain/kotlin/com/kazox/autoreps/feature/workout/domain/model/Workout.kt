package com.kazox.autoreps.feature.workout.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Workout(
    var name: String? = null,
    val reps: Int,
    val startedAt: String,
    val duration: Int,
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
)

class InvalidWorkoutException(message: String) : Exception(message)