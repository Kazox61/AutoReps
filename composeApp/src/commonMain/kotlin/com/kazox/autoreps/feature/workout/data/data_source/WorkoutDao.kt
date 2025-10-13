package com.kazox.autoreps.feature.workout.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout): Long

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Insert()
    suspend fun insertReps(reps: List<Rep>)

    @Query("""
        SELECT * 
        FROM Workout
        ORDER BY startedAt DESC
    """)
    fun getWorkouts(): Flow<List<Workout>>

    @Query("""
        SELECT SUM(reps)
        FROM Workout
        WHERE startedAt >= datetime('now', 'localtime', 'start of day')
    """)
    fun getTotalReps(): Flow<Int>

    @Query("""
        SELECT SUM(reps) 
        FROM Workout 
        WHERE startedAt >= datetime('now', 'localtime', 'start of day')
    """)
    fun getTodayReps(): Flow<Int>

    @Query("""
        WITH DistinctDates AS (
            SELECT DISTINCT DATE(startedAt) AS workoutDate
            FROM Workout
        ),
        NumberedDates AS (
            SELECT
                workoutDate,
                ROW_NUMBER() OVER (ORDER BY workoutDate) AS rn
            FROM DistinctDates
        ),
        StreakGroups AS (
            SELECT
                workoutDate,
                rn,
                DATE(workoutDate, '-' || rn || ' days') AS grp
            FROM NumberedDates
        )
        SELECT COUNT(*) AS current_streak
        FROM StreakGroups
        WHERE grp IN (
            SELECT grp FROM StreakGroups WHERE workoutDate = DATE('now', 'localtime')
        )
    """)
    fun getCurrentStreak(): Flow<Int>

    @Query(
        """
            SELECT *
            FROM Rep
            WHERE workoutId = :workoutId
        """
    )
    fun getRepsForWorkout(workoutId: Int): Flow<List<Rep>>
}