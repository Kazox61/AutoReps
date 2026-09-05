package com.kazox.autoreps.feature.settings.presentation

import com.kazox.autoreps.core.domain.model.ThemeChoice

sealed interface SettingsAction {
    data class DailyGoalChanged(val reps: Int) : SettingsAction

    data class RestSecondsChanged(val seconds: Int) : SettingsAction

    data class EmomEnabled(val enabled: Boolean) : SettingsAction

    data class EmomIntervalChanged(val seconds: Int) : SettingsAction

    data class EmomWarningChanged(val seconds: Int) : SettingsAction

    data class SoundPerRepChanged(val enabled: Boolean) : SettingsAction

    data class ThemeChanged(val theme: ThemeChoice) : SettingsAction

    /** Opens the confirmation. Nothing is deleted until [ConfirmDeleteAllData]. */
    data object DeleteAllData : SettingsAction

    data object ConfirmDeleteAllData : SettingsAction

    data object DismissDeleteAllData : SettingsAction
}
