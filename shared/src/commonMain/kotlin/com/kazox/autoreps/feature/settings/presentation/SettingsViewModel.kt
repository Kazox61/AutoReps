package com.kazox.autoreps.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.domain.model.AppSettings
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState(settings = settingsRepository.settings.value))
    val state = _state.asStateFlow()

    init {
        observeSettings()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.DailyGoalChanged -> edit { it.copy(dailyGoal = action.reps) }
            is SettingsAction.RestSecondsChanged -> edit { it.copy(restSeconds = action.seconds) }
            is SettingsAction.EmomEnabled -> edit { it.copy(emomEnabled = action.enabled) }
            is SettingsAction.EmomIntervalChanged -> edit { it.copy(emomIntervalSeconds = action.seconds) }
            is SettingsAction.EmomWarningChanged -> edit { it.copy(emomWarningSeconds = action.seconds) }
            is SettingsAction.SoundPerRepChanged -> edit { it.copy(soundPerRep = action.enabled) }
            is SettingsAction.ThemeChanged -> edit { it.copy(theme = action.theme) }

            SettingsAction.DeleteAllData ->
                _state.update { it.copy(confirmingDelete = true) }

            SettingsAction.DismissDeleteAllData ->
                _state.update { it.copy(confirmingDelete = false) }

            SettingsAction.ConfirmDeleteAllData -> deleteAllData()
        }
    }

    /**
     * Mirrors the store back into the state, so a change made anywhere reaches this screen.
     *
     * Nothing else writes settings today, but the screen reading the repository rather than its
     * own copy is what keeps that true — the alternative silently drifts the moment something does.
     */
    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings) }
            }
        }
    }

    /**
     * Applies one field and persists the whole value.
     *
     * Read through the repository, not through [state]: the repository's value is updated
     * synchronously by `update`, while the state catches up through the flow above. Two taps on a
     * stepper inside one frame would otherwise both start from the same old number, and the second
     * would undo the first.
     */
    private fun edit(transform: (AppSettings) -> AppSettings) {
        settingsRepository.update(transform(settingsRepository.settings.value))
    }

    private fun deleteAllData() {
        if (_state.value.isDeleting) return

        _state.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            try {
                // Workouts first: the settings reset cannot fail, so doing it last means a failed
                // wipe leaves everything as it was rather than half-cleared.
                workoutRepository.deleteAllWorkouts()
                settingsRepository.reset()
            } finally {
                _state.update { it.copy(isDeleting = false, confirmingDelete = false) }
            }
        }
    }
}
