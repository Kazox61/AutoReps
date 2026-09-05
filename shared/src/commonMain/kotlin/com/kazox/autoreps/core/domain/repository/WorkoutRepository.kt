package com.kazox.autoreps.core.domain.repository

import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun insertWorkout(workout: Workout): Int

    /** Edits an existing workout. Never re-insert to update — that destroys its reps. */
    suspend fun updateWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)

    /** Wipes every workout and, by cascade, every rep. */
    suspend fun deleteAllWorkouts()
    suspend fun insertReps(reps: List<Rep>)
    fun getWorkouts(): Flow<List<Workout>>
    fun getWorkoutById(id: Int): Flow<Workout?>

    /** The workout immediately before this one, for session-over-session comparison. */
    fun getPreviousWorkout(startedAt: String): Flow<Workout?>
    fun getRepsForWorkout(workoutId: Int): Flow<List<Rep>>
}