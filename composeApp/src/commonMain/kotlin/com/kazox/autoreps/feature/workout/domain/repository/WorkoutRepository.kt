package com.kazox.autoreps.feature.workout.domain.repository

import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun insertWorkout(workout: Workout): Int
    suspend fun deleteWorkout(workout: Workout)
    suspend fun insertReps(reps: List<Rep>)
    fun getWorkouts(): Flow<List<Workout>>
    fun getTotalReps(): Flow<Int>
    fun getTodayReps(): Flow<Int>
    fun getCurrentStreak(): Flow<Int>
    fun getRepsForWorkout(workoutId: Int): Flow<List<Rep>>
}