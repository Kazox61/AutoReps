package com.kazox.autoreps.core.data.data_source

import androidx.room.Room
import java.io.File

actual class DatabaseFactory {
    actual fun createBuilder(): androidx.room.RoomDatabase.Builder<WorkoutDatabase> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), WorkoutDatabase.DATABASE_NAME)
        return Room.databaseBuilder<WorkoutDatabase>(
            name = dbFile.absolutePath,
        )
    }
}