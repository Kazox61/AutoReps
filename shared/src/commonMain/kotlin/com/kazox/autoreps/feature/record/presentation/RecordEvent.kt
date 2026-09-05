package com.kazox.autoreps.feature.record.presentation

/** The outcome of pressing "Fertig", which decides where the record screen leaves to. */
sealed interface RecordEvent {
    /**
     * The session was written. [workoutId] is the new row — unnamed, so the next screen can
     * ask for a name and update it in place.
     */
    data class Saved(val workoutId: Int) : RecordEvent

    /** Nothing was counted, so nothing was written and there is nothing to name. */
    data object Discarded : RecordEvent
}
