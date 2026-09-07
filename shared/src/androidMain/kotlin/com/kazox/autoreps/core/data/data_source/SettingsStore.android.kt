package com.kazox.autoreps.core.data.data_source

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

actual class SettingsFactory(private val context: Context) {
    actual fun create(): Settings =
        SharedPreferencesSettings(
            context.applicationContext.getSharedPreferences(SETTINGS_STORE_NAME, Context.MODE_PRIVATE),
        )
}
