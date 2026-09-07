package com.kazox.autoreps.feature.settings.presentation

import androidx.compose.runtime.Stable
import com.kazox.autoreps.core.domain.model.AppSettings

@Stable
data class SettingsState(
    /** The persisted preferences. Always real — the store is read synchronously at startup. */
    val settings: AppSettings = AppSettings(),
    /** True while the "delete all data" confirmation is up. */
    val confirmingDelete: Boolean = false,
    /** True while the wipe runs, so the dialog's buttons cannot be pressed twice. */
    val isDeleting: Boolean = false,
)
