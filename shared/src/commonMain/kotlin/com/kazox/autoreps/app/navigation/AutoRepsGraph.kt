package com.kazox.autoreps.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AutoRepsGraph : NavKey {

    // ─── Tabs (top-level routes, each with its own back stack) ───

    @Serializable
    data object Home : AutoRepsGraph

    @Serializable
    data object History : AutoRepsGraph

    @Serializable
    data object Settings : AutoRepsGraph

    // ─── Pushed destinations ───

    /** Opened by the bottom bar's "+" action; pushed onto the current tab's stack. */
    @Serializable
    data object Record : AutoRepsGraph

    /**
     * Add or edit a single workout, pushed onto the current tab's stack.
     *
     * @param workoutId id of the workout to edit, or null to create a new one.
     * @param fromRecording true when this is the naming step of a just-finished session, which
     *   ends at Home instead of going back — there is nothing behind it but the camera.
     */
    @Serializable
    data class AddEditWorkout(
        val workoutId: Int? = null,
        val fromRecording: Boolean = false,
    ) : AutoRepsGraph
}
