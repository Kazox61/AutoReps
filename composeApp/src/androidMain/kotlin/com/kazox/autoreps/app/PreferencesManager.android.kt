package com.kazox.autoreps.app

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.coroutines.FlowSettings

@OptIn(markerClass = [ExperimentalSettingsApi::class])
actual val settings: FlowSettings
    get() = TODO("Not yet implemented")