package com.kazox.autoreps

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<WorkoutDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(WorkoutDatabase.DATABASE_NAME)
    return Room.databaseBuilder<WorkoutDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}