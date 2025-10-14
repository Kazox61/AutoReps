package com.kazox.autoreps.feature.setup.presentation.dailyGoal

sealed class DailyGoalEvent {
    data class UpdateDailyGoal(val goal: Int) : DailyGoalEvent()
}