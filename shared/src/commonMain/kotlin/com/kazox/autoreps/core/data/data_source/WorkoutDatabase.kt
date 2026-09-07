package com.kazox.autoreps.core.data.data_source

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [Workout::class, Rep::class],
    version = 2,
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

/**
 * Adds the index behind `Rep.workoutId`.
 *
 * A real migration rather than a destructive fallback even though nothing has shipped: creating
 * an index is a single statement that cannot fail halfway, so wiping the database would cost
 * every recorded workout to save three lines. `fallbackToDestructiveMigration` is also the kind
 * of thing that survives into release and quietly deletes real people's data the first time a
 * migration is forgotten.
 *
 * The name has to be exactly Room's own — it verifies the schema against the entity on open, and
 * an index by any other name reads as a mismatch.
 */
val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(connection: SQLiteConnection) {
            connection.execSQL("CREATE INDEX IF NOT EXISTS `index_Rep_workoutId` ON `Rep` (`workoutId`)")
        }
    }

fun getWorkoutDatabase(
    builder: RoomDatabase.Builder<WorkoutDatabase>
): WorkoutDatabase {
    return builder
        .addMigrations(MIGRATION_1_2)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect class DatabaseFactory {
    fun createBuilder(): RoomDatabase.Builder<WorkoutDatabase>
}