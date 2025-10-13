package com.kazox.autoreps

import androidx.room.Room
import androidx.room.RoomDatabase
import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<WorkoutDatabase> {
    val dbFilePath = documentDirectory() + "/" + WorkoutDatabase.DATABASE_NAME
    return Room.databaseBuilder<WorkoutDatabase>(
        name = dbFilePath,
    )
}

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