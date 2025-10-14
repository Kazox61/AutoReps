package com.kazox.autoreps.feature.workout.presentation.workouts

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.useCase.WorkoutUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

class WorkoutsViewModel(
    private val workoutUseCases: WorkoutUseCases
) : ViewModel() {

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: MutableState<List<Workout>> = _workouts

    private val _repsMap = mutableMapOf<Int, StateFlow<List<Rep>>>()

    init {
        workoutUseCases.getWorkouts()
            .onEach {
                _workouts.value = it
            }
            .launchIn(viewModelScope)
    }

    fun repsForWorkout(workoutId: Int): StateFlow<List<Rep>> {
        return _repsMap.getOrPut(workoutId) {
            workoutUseCases.getRepsForWorkout(workoutId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }
}