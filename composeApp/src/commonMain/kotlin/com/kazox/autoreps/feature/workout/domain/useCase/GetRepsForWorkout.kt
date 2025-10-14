package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class GetRepsForWorkout(
    private val repository: WorkoutRepository
) {
    operator fun invoke(workoutId: Int) = repository.getRepsForWorkout(workoutId)
}