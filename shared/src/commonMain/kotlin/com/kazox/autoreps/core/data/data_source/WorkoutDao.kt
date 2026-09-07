package com.kazox.autoreps.core.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    /**
     * Inserts a new workout and returns its generated id.
     *
     * Deliberately NOT OnConflictStrategy.REPLACE: SQLite implements REPLACE as delete +
     * insert, and Rep has ON DELETE CASCADE, so re-inserting an existing workout to change a
     * field silently destroys every rep belonging to it. Use [updateWorkout] to edit.
     */
    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    /** Updates an existing workout in place, leaving its reps alone. */
    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert()
    suspend fun insertReps(reps: List<Rep>)

    /**
     * Wipes every workout. Reps go with them through the ON DELETE CASCADE on [Rep], so there is
     * no second statement to keep in step with this one.
     */
    @Query("DELETE FROM Workout")
    suspend fun deleteAllWorkouts()

    @Query("""
        SELECT *
        FROM Workout
        ORDER BY startedAt DESC
    """)
    fun getWorkouts(): Flow<List<Workout>>

    @Query("""
        SELECT *
        FROM Workout
        WHERE id = :id
    """)
    fun getWorkoutById(id: Int): Flow<Workout?>

    /**
     * The workout immediately before this one, or null if it is the first ever.
     *
     * Matched purely on time, not on [Workout.name]: every workout is the same exercise
     * (push-ups), so the name is a label for the session, not an identifier for what was done.
     * Comparing by name meant renaming a session detached it from its own history.
     *
     * When a second exercise is added this needs an exerciseId to filter on — a name is not a
     * safe stand-in for one.
     */
    @Query("""
        SELECT *
        FROM Workout
        WHERE startedAt < :startedAt
        ORDER BY startedAt DESC
        LIMIT 1
    """)
    fun getPreviousWorkout(startedAt: String): Flow<Workout?>

    @Query(
        """
            SELECT *
            FROM Rep
            WHERE workoutId = :workoutId
            ORDER BY setId, timestamp
        """
    )
    fun getRepsForWorkout(workoutId: Int): Flow<List<Rep>>
}