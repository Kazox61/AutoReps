package com.kazox.autoreps.core.data.repository

import com.kazox.autoreps.core.domain.model.AppSettings
import com.kazox.autoreps.core.domain.model.ThemeChoice
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferences over a key-value store, cached in memory.
 *
 * The store is read once at construction and the in-memory [StateFlow] is authoritative from then
 * on — this class is the only writer, so it cannot go stale. That keeps reads synchronous, which
 * is what lets [SettingsRepository.settings] be a StateFlow with no loading state: a screen asking
 * for the daily goal on its first frame gets the real one, not a default it has to replace.
 *
 * Writes are whole-value and go to the store immediately. Ten small keys is not enough work to be
 * worth batching, and a write that survives being killed mid-session is worth more than the
 * microseconds saved.
 */
class StoredSettingsRepository(
    private val store: Settings,
) : SettingsRepository {
    private val _settings = MutableStateFlow(read())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    override fun update(settings: AppSettings) {
        write(settings)
        _settings.value = settings
    }

    override fun reset() {
        // clear() rather than writing the defaults: a key that is absent reads as its default, so
        // this also drops anything a previous version of the app wrote and no longer knows about.
        store.clear()
        _settings.value = AppSettings()
    }

    private fun read(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            dailyGoal = store.getInt(DAILY_GOAL, defaults.dailyGoal),
            restSeconds = store.getInt(REST_SECONDS, defaults.restSeconds),
            emomEnabled = store.getBoolean(EMOM_ENABLED, defaults.emomEnabled),
            emomIntervalSeconds = store.getInt(EMOM_INTERVAL_SECONDS, defaults.emomIntervalSeconds),
            emomWarningSeconds = store.getInt(EMOM_WARNING_SECONDS, defaults.emomWarningSeconds),
            soundPerRep = store.getBoolean(SOUND_PER_REP, defaults.soundPerRep),
            theme = readTheme(defaults.theme),
        )
    }

    private fun write(settings: AppSettings) {
        store.putInt(DAILY_GOAL, settings.dailyGoal)
        store.putInt(REST_SECONDS, settings.restSeconds)
        store.putBoolean(EMOM_ENABLED, settings.emomEnabled)
        store.putInt(EMOM_INTERVAL_SECONDS, settings.emomIntervalSeconds)
        store.putInt(EMOM_WARNING_SECONDS, settings.emomWarningSeconds)
        store.putBoolean(SOUND_PER_REP, settings.soundPerRep)
        store.putString(THEME, settings.theme.name)
    }

    /**
     * The theme is stored by name, not by ordinal: reordering the enum would silently repoint
     * every stored ordinal at a different choice. An unrecognised name falls back to the default,
     * which is what a downgrade or a hand-edited store looks like.
     */
    private fun readTheme(default: ThemeChoice): ThemeChoice {
        val stored = store.getStringOrNull(THEME) ?: return default
        return ThemeChoice.entries.firstOrNull { it.name == stored } ?: default
    }

    private companion object {
        const val DAILY_GOAL = "daily_goal"
        const val REST_SECONDS = "rest_seconds"
        const val EMOM_ENABLED = "emom_enabled"
        const val EMOM_INTERVAL_SECONDS = "emom_interval_seconds"
        const val EMOM_WARNING_SECONDS = "emom_warning_seconds"
        const val SOUND_PER_REP = "sound_per_rep"
        const val THEME = "theme"
    }
}
