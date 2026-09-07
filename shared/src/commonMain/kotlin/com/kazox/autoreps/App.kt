package com.kazox.autoreps

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kazox.autoreps.app.navigation.NavigationRoot
import com.kazox.autoreps.core.domain.model.ThemeChoice
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import org.koin.compose.koinInject
import com.kazox.ui.foundation.KazAccentPreset
import com.kazox.ui.foundation.KazPalette
import com.kazox.ui.foundation.KazStylePreset
import com.kazox.ui.foundation.KazTheme

@Composable
@Preview
fun App(settingsRepository: SettingsRepository = koinInject()) {
    val isDark = rememberEffectiveDarkTheme(settingsRepository)

    KazTheme(
        palette = KazPalette.Zinc,
        accent = KazAccentPreset.Default,
        isDark = isDark,
        preset = KazStylePreset.Default,
    ) {
        // No safeDrawingPadding here: the Scaffold owns the system insets (its bars lift
        // clear of the status bar / home indicator while content flows edge to edge behind
        // them). Padding here would shrink the whole scaffold above the safe area instead.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KazTheme.colors.background),
        ) {
            NavigationRoot()
        }
    }
}

/**
 * The theme actually rendered: the user's choice resolved against the system setting.
 *
 * isSystemInDarkTheme is read unconditionally rather than inside the when so the theme keeps
 * reacting to the device switching over at dusk even while the user has picked a fixed one.
 *
 * Android also feeds this into enableEdgeToEdge — whose system-bar icon colors must match what
 * is actually on screen, not the system's own mode.
 */
@Composable
fun rememberEffectiveDarkTheme(
    settingsRepository: SettingsRepository = koinInject(),
): Boolean {
    val settings by settingsRepository.settings.collectAsState()
    val systemIsDark = isSystemInDarkTheme()
    return when (settings.theme) {
        ThemeChoice.Light -> false
        ThemeChoice.Dark -> true
        ThemeChoice.System -> systemIsDark
    }
}