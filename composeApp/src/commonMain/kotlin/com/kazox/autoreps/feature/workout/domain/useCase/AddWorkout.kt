package com.kazox.autoreps.feature.workout.domain.useCase

import com.kazox.autoreps.feature.workout.domain.model.InvalidWorkoutException
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository
import kotlin.coroutines.cancellation.CancellationException

class AddWorkout(
    private val repository: WorkoutRepository
) {

    @Throws(InvalidWorkoutException::class, CancellationException::class)
    suspend operator fun invoke(workout: Workout) : Int {
        return repository.insertWorkout(workout)
    }
}