package com.kazox.autoreps.feature.workout.presentation.addEditWorkout

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.useCase.WorkoutUseCases
import com.kazox.autoreps.feature.workout.domain.util.getStartHour
import kotlinx.coroutines.launch

class AddEditViewModel(
    private val workoutUseCases: WorkoutUseCases
): ViewModel() {
    var workout: Workout? = null
    var reps: List<Rep>? = null

    private val _workoutName = mutableStateOf(
        when (workout?.name) {
            null ->
                when (workout?.getStartHour()) {
                    in 5..11 -> "Morning Workout"
                    in 12..17 -> "Afternoon Workout"
                    in 18 .. 22 -> "Evening Workout"
                    else -> "Night Workout"
                }
            else -> workout?.name ?: "New Workout"
        }
    )
    val workoutName: MutableState<String> = _workoutName

    fun onEvent(event: AddEditWorkoutEvent) {
        when (event) {
            is AddEditWorkoutEvent.EnteredName -> {
                _workoutName.value = event.name
                workout?.name = event.name
            }
            is AddEditWorkoutEvent.SaveWorkout -> {
                viewModelScope.launch {
                    workout?.name = workoutName.value
                    val workoutId = workoutUseCases.addWorkout(
                        workout!!
                    )

                    reps?.forEach {
                        it.workoutId = workoutId
                    }

                    workoutUseCases.addReps(
                        reps ?: emptyList()
                    )
                }
            }
        }
    }
}