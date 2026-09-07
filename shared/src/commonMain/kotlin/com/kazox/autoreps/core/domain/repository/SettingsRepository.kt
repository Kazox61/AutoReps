package com.kazox.autoreps.core.domain.repository

import com.kazox.autoreps.core.domain.model.AppSettings
import kotlinx.coroutines.flow.StateFlow

/**
 * The app's preferences, kept as one always-readable value.
 *
 * A [StateFlow] rather than a plain Flow: settings have no "not loaded yet" state worth showing —
 * every screen that reads them needs a value on its first frame, and a spinner over a daily goal
 * would be worse than briefly showing the default.
 */
interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    /** Replaces the stored settings. Whole-value, so callers `copy` the field they mean. */
    fun update(settings: AppSettings)

    /** Puts every preference back to its default. Part of "delete all data". */
    fun reset()
}
