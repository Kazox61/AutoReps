package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class AddReps(
    private val repository: WorkoutRepository
) {
    suspend operator fun invoke(reps: List<Rep>) = repository.insertReps(reps)
}