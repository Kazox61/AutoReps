package com.kazox.autoreps.feature.workout.presentation

sealed interface AddEditWorkoutAction {
    data class EnteredName(val name: String) : AddEditWorkoutAction

    /** Persists the edited name and emits [AddEditWorkoutViewModel.saved] once done. */
    data object SaveWorkout : AddEditWorkoutAction
}
