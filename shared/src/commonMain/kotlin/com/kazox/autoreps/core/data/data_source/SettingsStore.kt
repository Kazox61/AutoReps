package com.kazox.autoreps.core.data.data_source

import com.russhwolf.settings.Settings

/** Namespaces the stored keys, so the app's preferences cannot collide with anyone else's. */
const val SETTINGS_STORE_NAME: String = "autoreps_settings"

/**
 * Builds the platform's key-value store: SharedPreferences, NSUserDefaults, or java.util.prefs.
 *
 * Mirrors [DatabaseFactory] — the platform seam is the factory, so the repository above it stays
 * in commonMain and knows only [Settings].
 */
expect class SettingsFactory {
    fun create(): Settings
}
