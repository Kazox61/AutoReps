package com.kazox.autoreps.feature.workout.domain.useCase

data class WorkoutUseCases(
    val addWorkout: AddWorkout,
    val deleteWorkout: DeleteWorkout,
    val addReps: AddReps,
    val getWorkouts: GetWorkouts,
    val getTotalReps: GetTotalReps,
    val getTodayReps: GetTodayReps,
    val getCurrentStreak: GetCurrentStreak,
    val getRepsForWorkout: GetRepsForWorkout
)