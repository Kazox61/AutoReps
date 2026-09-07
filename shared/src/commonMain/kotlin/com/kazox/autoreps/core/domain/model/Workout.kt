package com.kazox.autoreps.core.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    var name: String? = null,
    val reps: Int,
    val startedAt: String,
    val duration: Int,
)