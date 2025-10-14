package com.kazox.autoreps.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.app.PreferencesManager
import com.kazox.autoreps.feature.workout.domain.useCase.WorkoutUseCases
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val workoutUseCases: WorkoutUseCases,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    val dailyGoal = preferencesManager.dailyGoal
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 50
        )

    val todayReps = workoutUseCases.getTodayReps()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val workouts = workoutUseCases.getWorkouts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}