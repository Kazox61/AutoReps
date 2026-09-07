package com.kazox.autoreps.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

/**
 * `idleTimerDisabled` is a single app-wide flag, not a counted hold, so clearing it on dispose
 * is only correct while one screen at a time asks for this. Should a second ever want it, this
 * needs a counter — the last screen to leave would otherwise switch the timer back on underneath
 * the first.
 */
@Composable
actual fun KeepScreenOn(enabled: Boolean) {
    DisposableEffect(enabled) {
        if (!enabled) return@DisposableEffect onDispose { }

        UIApplication.sharedApplication.idleTimerDisabled = true
        onDispose { UIApplication.sharedApplication.idleTimerDisabled = false }
    }
}
