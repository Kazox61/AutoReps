package com.kazox.autoreps

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    fun addWorkout() {
        viewModelScope.launch {

            // Sample insert
            val workout = Workout(
                reps = 20,
                startedAt = "2024-10-01T10:00:00Z",
                duration = 20
            )

            workoutRepository.insertWorkout(workout)
        }
    }

    private val _workouts = mutableStateOf<List<Workout>>(emptyList())
    val workouts: MutableState<List<Workout>> = _workouts

    init {
        workoutRepository.getWorkouts()
            .onEach {
                _workouts.value = it
                println("Workouts: ${it.size} updated")
            }
            .launchIn(viewModelScope)
    }
}