package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class GetTotalReps(
    private val repository: WorkoutRepository
) {
    operator fun invoke() = repository.getTotalReps()
}