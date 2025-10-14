package com.kazox.autoreps.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.app.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NavigationViewModel(
    private val preferencesManager: PreferencesManager
): ViewModel() {
    val setupFinished = preferencesManager.setupFinished
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 50
        )

    fun updateSetupFinished() {
        viewModelScope.launch {
            preferencesManager.updateSetupFinished(true)
        }
    }
}