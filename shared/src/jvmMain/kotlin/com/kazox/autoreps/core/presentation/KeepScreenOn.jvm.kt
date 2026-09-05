package com.kazox.autoreps.core.presentation

import androidx.compose.runtime.Composable

/**
 * Desktop keeps its own display awake, and there is nothing to record here anyway — pose
 * detection is mobile-only. A no-op rather than a Robot-based mouse nudge, which is what
 * "keep awake" costs on the JVM and is far more than this screen is worth.
 */
@Composable
actual fun KeepScreenOn(enabled: Boolean) = Unit
