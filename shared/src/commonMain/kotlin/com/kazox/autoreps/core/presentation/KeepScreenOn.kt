package com.kazox.autoreps.core.presentation

import androidx.compose.runtime.Composable

/**
 * Holds the display awake for as long as this composable stays in the composition.
 *
 * For screens the user watches without touching — counting reps from across the room is exactly
 * the case the idle timer gets wrong, since "no input" there means the workout is going well.
 *
 * Scoped to composition, not to a global toggle: leaving the screen releases the hold on every
 * platform, so no path off it can leave the display pinned awake.
 *
 * @param enabled false releases the hold without removing the call, for screens that only
 *   sometimes need it.
 */
@Composable
expect fun KeepScreenOn(enabled: Boolean = true)
