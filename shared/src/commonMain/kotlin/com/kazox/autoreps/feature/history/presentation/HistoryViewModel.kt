package com.kazox.autoreps.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HistoryState())
    val state = _state.asStateFlow()

    init {
        observeWorkouts()
    }

    fun onAction(action: HistoryAction) {
        when (action) {
            is HistoryAction.DeleteWorkout ->
                viewModelScope.launch {
                    workoutRepository.deleteWorkout(action.workout)
                }
        }
    }

    private fun observeWorkouts() {
        viewModelScope.launch {
            workoutRepository.getWorkouts().collect { workouts ->
                _state.update { it.copy(workouts = workouts, isLoading = false) }
            }
        }
    }
}
