package com.kazox.autoreps.core.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    // Every read of a workout's reps filters on this column, and the CASCADE above walks it on
    // every delete. Without the index both are full scans of every rep ever recorded.
    indices = [Index("workoutId")]
)
data class Rep(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    var workoutId: Int = 0,
    val timestamp: Int,
    val setId: Int,
)