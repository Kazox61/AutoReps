package com.kazox.autoreps.app

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSettingsApi::class)
expect val settings: FlowSettings

class PreferencesManager() {

    private val SETUP_FINISHED_KEY = "setup_finished"
    private val DAILY_GOAL_KEY = "daily_goal"
    val DEFAULT_DAILY_GOAL = 50

    @OptIn(ExperimentalSettingsApi::class)
    val setupFinished = settings.getIntOrNullFlow(SETUP_FINISHED_KEY)
        .map { it?.let { it > 0 } }

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun updateSetupFinished(finished: Boolean) {
        settings.putInt(SETUP_FINISHED_KEY, if (finished) 1 else 0)
    }

    @OptIn(ExperimentalSettingsApi::class)
    val dailyGoal = settings.getIntFlow(DAILY_GOAL_KEY, DEFAULT_DAILY_GOAL)

    @OptIn(ExperimentalSettingsApi::class)
    suspend fun updateDailyGoal(goal: Int) {
        settings.putInt(DAILY_GOAL_KEY, goal)
    }
}
