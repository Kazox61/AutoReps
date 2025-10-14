package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository

class GetTodayReps(
    private val repository: WorkoutRepository
) {
    operator fun invoke() = repository.getTodayReps()
}