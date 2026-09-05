package com.kazox.autoreps.feature.workout.presentation

import androidx.compose.runtime.Stable
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout

@Stable
data class AddEditWorkoutState(
    /** The loaded row, or null for a `workoutId == null` route or while loading. */
    val workout: Workout? = null,
    val reps: List<Rep> = emptyList(),
    /** The name field; seeded from [Workout.name] once the row arrives, never clobbered after. */
    val name: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    /** Set when a save fails, so the screen can say so instead of silently doing nothing. */
    val saveError: String? = null,
    /**
     * The last workout with the same name before this one, or null when this is the first of
     * its kind. Drives the session-over-session comparison.
     */
    val previousWorkout: Workout? = null,
)
