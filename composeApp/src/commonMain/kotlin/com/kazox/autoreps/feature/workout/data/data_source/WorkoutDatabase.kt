package com.kazox.autoreps.feature.workout.data.data_source

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [Workout::class, Rep::class],
    version = 1,
)
@ConstructedBy(WorkoutDatabaseConstructor::class)
abstract class WorkoutDatabase : RoomDatabase() {
    abstract fun getDao(): WorkoutDao

    companion object {
        const val DATABASE_NAME = "workout.db"
    }
}

@Suppress("KotlinNoActualForExpect")
expect object WorkoutDatabaseConstructor : RoomDatabaseConstructor<WorkoutDatabase> {
    override fun initialize(): WorkoutDatabase
}

fun getWorkoutDatabase(
    builder: RoomDatabase.Builder<WorkoutDatabase>
): WorkoutDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}