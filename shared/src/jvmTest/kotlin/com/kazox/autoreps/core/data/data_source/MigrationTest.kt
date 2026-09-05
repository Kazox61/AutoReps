package com.kazox.autoreps.core.data.data_source

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Opens a real version-1 file with the version-2 database.
 *
 * The point is not that the CREATE INDEX statement is valid SQL — it is that Room *accepts* the
 * schema the migration leaves behind. Room compares the opened database against the entities and
 * throws if anything differs, right down to the index's name, and that check only ever runs on a
 * device that has an old file on it. This is the one place it can run before then.
 */
class MigrationTest {
    private val dbFile = File.createTempFile("migration-test", ".db").also { it.delete() }

    @AfterTest
    fun cleanUp() {
        listOf("", "-wal", "-shm").forEach { File(dbFile.path + it).delete() }
    }

    @Test
    fun `version 1 data survives the upgrade and gains the index`() {
        createVersion1DatabaseWithOneWorkout()

        val database =
            getWorkoutDatabase(Room.databaseBuilder<WorkoutDatabase>(name = dbFile.absolutePath))

        try {
            val workouts = runBlocking { database.getDao().getWorkouts().first() }
            assertEquals(1, workouts.size, "the migration must not drop existing workouts")
            assertEquals("Vorher", workouts.single().name)

            val reps = runBlocking { database.getDao().getRepsForWorkout(workouts.single().id).first() }
            assertEquals(2, reps.size, "reps belonging to the workout must survive too")

        } finally {
            database.close()
        }

        // Read back through the raw driver rather than through Room: this is asking what is
        // actually on disk after the migration, which is the thing a device would be left with.
        val indices = repIndexNames()
        assertTrue("index_Rep_workoutId" in indices, "expected the new index, found $indices")
    }

    /** Builds the file exactly as version 1 of the app left it, straight from `schemas/1.json`. */
    private fun createVersion1DatabaseWithOneWorkout() {
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            connection.execSQL(V1_WORKOUT_TABLE)
            connection.execSQL(V1_REP_TABLE)
            // Room refuses to open a database whose recorded identity does not match a schema it
            // knows, so version 1's bookkeeping has to be here as well as its tables.
            connection.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
            )
            connection.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '$V1_IDENTITY_HASH')",
            )
            connection.execSQL("PRAGMA user_version = 1")

            connection.execSQL(
                "INSERT INTO Workout (id, name, reps, startedAt, duration) " +
                    "VALUES (1, 'Vorher', 2, '2026-08-20T18:30:00', 90)",
            )
            connection.execSQL("INSERT INTO Rep (id, workoutId, timestamp, setId) VALUES (1, 1, 0, 1)")
            connection.execSQL("INSERT INTO Rep (id, workoutId, timestamp, setId) VALUES (2, 1, 3, 1)")
        } finally {
            connection.close()
        }
    }

    private fun repIndexNames(): List<String> {
        val connection = BundledSQLiteDriver().open(dbFile.absolutePath)
        try {
            val statement =
                connection.prepare("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'Rep'")
            try {
                return buildList { while (statement.step()) add(statement.getText(0)) }
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }

    private companion object {
        const val V1_IDENTITY_HASH = "a9d8d861a9fb3b20ff3161969e4de1e8"

        const val V1_WORKOUT_TABLE =
            "CREATE TABLE IF NOT EXISTS `Workout` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT, `reps` INTEGER NOT NULL, `startedAt` TEXT NOT NULL, `duration` INTEGER NOT NULL)"

        /** Version 1's Rep table: same columns, no index on workoutId. That is what changed. */
        const val V1_REP_TABLE =
            "CREATE TABLE IF NOT EXISTS `Rep` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`workoutId` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `setId` INTEGER NOT NULL, " +
                "FOREIGN KEY(`workoutId`) REFERENCES `Workout`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
    }
}
