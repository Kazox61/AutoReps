package com.kazox.autoreps.feature.workout.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Rep(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    var workoutId: Int = 0,
    val timestamp: Int,
    val setId: Int,
)