package com.kazox.autoreps.core.data.data_source

import androidx.room.Room
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class DatabaseFactory {
    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }

    actual fun createBuilder(): androidx.room.RoomDatabase.Builder<WorkoutDatabase> {
        val dbFilePath = documentDirectory() + "/" + WorkoutDatabase.DATABASE_NAME
        return Room.databaseBuilder<WorkoutDatabase>(
            name = dbFilePath,
        )
    }
}