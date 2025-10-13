package com.kazox.autoreps.feature.workout.data.repository

import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabase
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class WorkoutRepositoryImpl(
    private val database: WorkoutDatabase
) : WorkoutRepository {
    override suspend fun insertWorkout(workout: Workout): Int = database.getDao().insertWorkout(workout).toInt()

    override suspend fun deleteWorkout(workout: Workout) = database.getDao().deleteWorkout(workout)

    override suspend fun insertReps(reps: List<Rep>) = database.getDao().insertReps(reps)

    override fun getWorkouts() = database.getDao().getWorkouts()

    override fun getTotalReps() = database.getDao().getTotalReps()

    override fun getTodayReps() = database.getDao().getTodayReps()

    override fun getCurrentStreak() = database.getDao().getCurrentStreak()

    override fun getRepsForWorkout(workoutId: Int) = database.getDao().getRepsForWorkout(workoutId)
}