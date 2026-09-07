package com.kazox.autoreps.core.data.data_source

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(private val context: Context) {
    actual fun createBuilder(): RoomDatabase.Builder<WorkoutDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(WorkoutDatabase.DATABASE_NAME)
        return Room.databaseBuilder<WorkoutDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}