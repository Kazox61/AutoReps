package com.kazox.autoreps.core.data.data_source

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

actual class SettingsFactory {
    // A named suite rather than standardUserDefaults: the app's own preferences stay separate
    // from the system-managed keys that live in the standard domain.
    actual fun create(): Settings =
        NSUserDefaultsSettings(NSUserDefaults(suiteName = SETTINGS_STORE_NAME))
}
