package com.kazox.autoreps.core.data.repository

import com.kazox.autoreps.core.data.data_source.WorkoutDatabase
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.domain.repository.WorkoutRepository

class RoomWorkoutRepository(
    private val database: WorkoutDatabase
) : WorkoutRepository {
    override suspend fun insertWorkout(workout: Workout): Int = database.getDao().insertWorkout(workout).toInt()

    override suspend fun updateWorkout(workout: Workout) = database.getDao().updateWorkout(workout)

    override suspend fun deleteWorkout(workout: Workout) = database.getDao().deleteWorkout(workout)

    override suspend fun deleteAllWorkouts() = database.getDao().deleteAllWorkouts()

    override suspend fun insertReps(reps: List<Rep>) = database.getDao().insertReps(reps)

    override fun getWorkouts() = database.getDao().getWorkouts()

    override fun getWorkoutById(id: Int) = database.getDao().getWorkoutById(id)

    override fun getPreviousWorkout(startedAt: String) = database.getDao().getPreviousWorkout(startedAt)

    override fun getRepsForWorkout(workoutId: Int) = database.getDao().getRepsForWorkout(workoutId)
}