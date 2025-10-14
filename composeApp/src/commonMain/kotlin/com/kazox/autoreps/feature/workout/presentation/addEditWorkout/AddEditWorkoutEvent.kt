package com.kazox.autoreps.feature.workout.presentation.addEditWorkout

sealed class AddEditWorkoutEvent {
    data object SaveWorkout : AddEditWorkoutEvent()
    data class EnteredName(val name: String) : AddEditWorkoutEvent()
}