package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class DeleteWorkout(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(workout: Workout) = repository.deleteWorkout(workout)
}