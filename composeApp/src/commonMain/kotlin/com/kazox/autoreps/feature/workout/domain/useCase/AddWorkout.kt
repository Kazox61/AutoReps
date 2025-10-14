package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.model.InvalidWorkoutException
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class AddWorkout(
    private val repository: WorkoutRepository
) {

    @Throws(InvalidWorkoutException::class)
    suspend operator fun invoke(workout: Workout) : Int {
        if (workout.name.isNullOrBlank()) {
            throw InvalidWorkoutException("The name of the workout can't be empty.")
        }

        return repository.insertWorkout(workout)
    }
}