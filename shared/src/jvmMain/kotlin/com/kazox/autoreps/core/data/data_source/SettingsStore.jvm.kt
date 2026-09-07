package com.kazox.autoreps.core.data.data_source

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import java.util.prefs.Preferences

actual class SettingsFactory {
    actual fun create(): Settings =
        PreferencesSettings(Preferences.userRoot().node(SETTINGS_STORE_NAME))
}
