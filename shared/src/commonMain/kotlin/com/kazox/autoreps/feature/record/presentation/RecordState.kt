package com.kazox.autoreps.feature.record.presentation

import androidx.compose.runtime.Stable
import com.kazox.autoreps.feature.record.domain.RepPhase

/** Where the screen is in the start → count → finish cycle. */
enum class RecordStatus {
    /** Nothing running. The button says "Starten". */
    Idle,

    /** Frames are being counted. */
    Recording,
}

/** The EMOM in flight, or absent for a free-form session. */
@Stable
data class EmomState(
    val round: Int = 1,
)

@Stable
data class RecordState(
    val repCount: Int = 0,
    /** What the detector believes the body is doing, for live feedback. */
    val phase: RepPhase = RepPhase.ABSENT,
    /**
     * Whether the camera can see everything the exercise needs — not merely "a person".
     *
     * MediaPipe reports a pose for a face alone, so this cannot be `pose != null`: that would
     * report "ready" while only a face was in shot.
     *
     * Separate from [phase] on purpose: the detector only runs while recording, so phase stays
     * ABSENT before you press Start — but you still need to know whether the camera can see you
     * while getting into position.
     */
    val isPersonVisible: Boolean = false,
    /** Whether the camera preview is on screen, toggled by the eye in the corner. */
    val showPreview: Boolean = false,
    val elapsedMillis: Long = 0,
    val status: RecordStatus = RecordStatus.Idle,
    /** Non-null only while an EMOM is running, so free-form sessions carry no round state at all. */
    val emom: EmomState? = null,
    /** True while the finished session is being written. */
    val isSaving: Boolean = false,
    /**
     * Set when the write failed, so the set can be retried instead of vanishing.
     *
     * The reps stay in the session while this is non-null — leaving the screen is the only way
     * to lose them, and the button says so rather than pretending the workout was saved.
     */
    val saveError: String? = null,
    val hasCameraPermission: Boolean = false,
    /** True once permission was asked for and refused, so the screen can explain itself. */
    val permissionDenied: Boolean = false,
) {
    /** Reps are being counted right now. */
    val isRecording: Boolean get() = status == RecordStatus.Recording

    /** Either half of a run in progress, which is what the "Starten" button hides behind. */
    val isActive: Boolean get() = status != RecordStatus.Idle
}
