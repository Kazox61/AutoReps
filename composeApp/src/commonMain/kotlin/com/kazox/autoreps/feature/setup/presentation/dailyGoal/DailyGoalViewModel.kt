package com.kazox.autoreps.feature.setup.presentation.dailyGoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.app.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DailyGoalViewModel (
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    val dailyGoal = preferencesManager.dailyGoal
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = preferencesManager.DEFAULT_DAILY_GOAL
        )

    fun onEvent(event: DailyGoalEvent) {
        when (event) {
            is DailyGoalEvent.UpdateDailyGoal -> {
                viewModelScope.launch {
                    preferencesManager.updateDailyGoal(event.goal)
                }
            }
        }
    }
}