package com.kazox.autoreps.feature.record.presentation

import com.kazox.autoreps.feature.record.presentation.camera.PoseFrame

sealed interface RecordAction {
    /** A camera frame arrived. The only input the rep counter needs. */
    data class FrameAnalysed(val frame: PoseFrame) : RecordAction

    /** Begins a run, counting straight away. */
    data object StartRecording : RecordAction

    /**
     * Ends the set and writes it: a [RecordEvent] follows once the workout exists.
     *
     * Also the retry when a previous save failed — the reps stay in memory until one succeeds.
     */
    data object FinishRecording : RecordAction

    data class CameraPermissionResult(val granted: Boolean) : RecordAction

    /** Shows or hides the camera preview, which is off while you are busy doing reps. */
    data object TogglePreview : RecordAction

    /**
     * Leaves without saving, on the confirm dialog's say-so. Whatever the session counted is
     * dropped on purpose — the dialog was explicit that it would be.
     */
    data object Discard : RecordAction
}
