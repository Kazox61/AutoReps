package com.kazox.autoreps.feature.record.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import com.kazox.autoreps.core.sound.SoundPlayer
import com.kazox.autoreps.core.sound.Tones
import com.kazox.autoreps.feature.record.domain.EmomPlan
import com.kazox.autoreps.feature.record.domain.PushupExercise
import com.kazox.autoreps.feature.record.domain.RepPhase
import com.kazox.autoreps.feature.record.domain.WorkoutSession
import com.kazox.autoreps.feature.record.presentation.camera.PoseFrame
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * Drives push-up counting from the camera, and writes the finished session.
 *
 * The workout row is created here rather than on the naming screen that follows: that screen is
 * reached by id, so the rows have to exist before it can load them — and creating them here means
 * a set is never lost by backing out of naming it.
 */
class RecordViewModel(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
    private val soundPlayer: SoundPlayer,
) : ViewModel() {
    private val _state = MutableStateFlow(RecordState())
    val state = _state.asStateFlow()

    /** Emitted once the finished session has been dealt with, so the screen knows where to go. */
    private val _events = Channel<RecordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var detector = PushupExercise.detector()
    private var session = newSession(plan = null)

    /** The round clock, running only while an EMOM is in progress. */
    private var emomJob: Job? = null

    fun onAction(action: RecordAction) {
        when (action) {
            is RecordAction.FrameAnalysed -> onFrame(action.frame)

            RecordAction.StartRecording -> beginRecording()

            RecordAction.FinishRecording -> finish()

            RecordAction.TogglePreview ->
                _state.update { it.copy(showPreview = !it.showPreview) }

            is RecordAction.CameraPermissionResult ->
                _state.update {
                    it.copy(
                        hasCameraPermission = action.granted,
                        permissionDenied = !action.granted,
                    )
                }

            RecordAction.Discard -> discard()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.release()
    }

    private fun onFrame(frame: PoseFrame) {
        // Tracked whether or not we are recording: it drives the framing preview, which matters
        // most *before* the set starts. canTrack, not `pose != null` — MediaPipe reports a pose
        // for a bare face, and a rep cannot be counted from one.
        val trackable = detector.canTrack(frame.pose)

        // Frames keep arriving while paused — the preview stays live — but they must not count.
        if (!_state.value.isRecording) {
            _state.update { it.copy(isPersonVisible = trackable) }
            return
        }

        session.startIfNeeded(frame.timestampMillis)
        val repAt = detector.onFrame(frame.pose, frame.timestampMillis)
        if (repAt != null) {
            session.record(repAt)
            // The whole point of the setting: face down at the bottom of a push-up, the counter
            // on screen is the one thing you cannot check.
            if (settingsRepository.settings.value.soundPerRep) soundPlayer.play(Tones.Rep)
        }

        _state.update {
            it.copy(
                repCount = session.repCount,
                phase = detector.phase,
                isPersonVisible = trackable,
                elapsedMillis = session.elapsedMillis(frame.timestampMillis),
            )
        }
    }

    /**
     * Switches to counting frames.
     *
     * The detector and session are built here rather than in the constructor so a previous run
     * can never leave a half-finished rep behind in the detector.
     */
    private fun beginRecording() {
        val plan = emomPlan()
        detector = PushupExercise.detector()
        session = newSession(plan)
        // An octave above the round ticks, it is the one unambiguous signal that reps are being
        // counted from now on. It doubles as round 1's start tone.
        soundPlayer.play(Tones.CountdownGo)
        _state.update {
            it.copy(
                status = RecordStatus.Recording,
                repCount = 0,
                elapsedMillis = 0,
                phase = RepPhase.ABSENT,
                saveError = null,
                emom = plan?.let { EmomState(round = 1) },
            )
        }
        if (plan != null) runEmom(plan)
    }

    /** The EMOM configured in Settings, or null when this is a free-form session. */
    private fun emomPlan(): EmomPlan? {
        val settings = settingsRepository.settings.value
        if (!settings.emomEnabled) return null

        return EmomPlan(
            intervalSeconds = settings.emomIntervalSeconds,
            warningSeconds = settings.emomWarningSeconds,
        )
    }

    /**
     * Drives the round clock until the athlete stops.
     *
     * Polls a monotonic mark rather than sleeping one round at a time: `delay` guarantees *at
     * least* its duration, so a per-round sleep accumulates error, and by round ten of a
     * long EMOM the tone no longer lands on the minute. Everything here is derived from total
     * elapsed time, which cannot drift.
     */
    private fun runEmom(plan: EmomPlan) {
        emomJob?.cancel()
        emomJob =
            viewModelScope.launch {
                val startedAt = TimeSource.Monotonic.markNow()
                var currentRound = 1
                var lastWarnedSecond = 0

                while (true) {
                    val progress = plan.progressAt(startedAt.elapsedNow().inWholeMilliseconds)

                    if (progress.round != currentRound) {
                        currentRound = progress.round
                        // The clock declares the boundary, so the session is told rather than
                        // left to infer one from the rest that just happened.
                        session.startNewSet()
                        soundPlayer.play(Tones.CountdownGo)
                    }

                    val left = progress.secondsLeftInRound
                    // One tone per second, and only inside the warning window. Tracking the last
                    // second warned is what keeps the poll loop from retriggering the same one.
                    if (left in 1..plan.warningSeconds && left != lastWarnedSecond) {
                        soundPlayer.play(Tones.CountdownTick)
                    }
                    lastWarnedSecond = left

                    if (progress.round != _state.value.emom?.round) {
                        _state.update { it.copy(emom = it.emom?.copy(round = progress.round)) }
                    }

                    delay(EMOM_POLL_MILLIS)
                }
            }
    }

    /**
     * A session that splits sets the way this run needs.
     *
     * An EMOM passes no rest threshold at all — its rounds are the sets, and inferring extra ones
     * from the rest inside a round would cut every round in two.
     *
     * The free-form threshold is read per session rather than once per view model: it only
     * matters while a set is running, so picking it up at Start means a change made in Settings
     * applies to the next set rather than the next launch.
     */
    private fun newSession(plan: EmomPlan?) =
        WorkoutSession(
            restThresholdMillis =
                if (plan != null) null else settingsRepository.settings.value.restSeconds * 1000L,
        )

    /**
     * Leaves without a write, stopping the round clock with it. Nothing here is kept on
     * purpose: this is the discard dialog's destructive half, not a pause.
     */
    private fun discard() {
        emomJob?.cancel()
        emomJob = null
        _state.update { it.copy(status = RecordStatus.Idle, emom = null) }
        viewModelScope.launch { _events.send(RecordEvent.Discarded) }
    }

    /**
     * Ends the set and persists it.
     *
     * The session is deliberately left intact until the write succeeds, so a failed save can be
     * retried from the same reps rather than being counted twice or lost.
     */
    private fun finish() {
        if (_state.value.isSaving) return

        val reps = session.reps.toList()
        val elapsedMillis = _state.value.elapsedMillis

        if (reps.isEmpty()) {
            // No row for a set that never happened: an empty workout would still land in
            // History and count towards the streak. Same exit as an explicit discard.
            discard()
            return
        }

        emomJob?.cancel()
        emomJob = null
        _state.update { it.copy(status = RecordStatus.Idle, emom = null) }

        // Set before launching, not inside: a second tap on Fertig must be rejected by the
        // guard above on the very next call, without depending on when the coroutine is run.
        _state.update { it.copy(isSaving = true, saveError = null) }

        viewModelScope.launch {
            try {
                val workoutId =
                    workoutRepository.insertWorkout(
                        Workout(
                            // Unnamed on purpose — the screen that follows asks for the name and
                            // updates this row in place.
                            name = null,
                            reps = reps.size,
                            startedAt = startedAt(elapsedMillis),
                            duration = (elapsedMillis / 1000).toInt(),
                        ),
                    )
                // Reps second: the foreign key needs the workout's generated id, which only
                // exists once the insert above has returned.
                workoutRepository.insertReps(
                    reps.map { rep ->
                        Rep(
                            workoutId = workoutId,
                            timestamp = (rep.elapsedMillis / 1000).toInt(),
                            setId = rep.setIndex,
                        )
                    },
                )
                _state.update { it.copy(isSaving = false) }
                _events.send(RecordEvent.Saved(workoutId))
            } catch (e: Exception) {
                // Raw cause only — the screen composes the localized message around it.
                _state.update {
                    it.copy(isSaving = false, saveError = e.message ?: "")
                }
            }
        }
    }

    /**
     * When the session began, in the `LocalDateTime.toString()` shape the rest of the app parses
     * [Workout.startedAt] as.
     *
     * Derived by subtracting the elapsed time instead of being remembered at Start: frames are
     * stamped with the camera's clock, not the wall clock, so only the span between them is
     * comparable. Seconds are always written out — the DAO's date queries and History's parser
     * both read this string, so its shape must not vary with the value.
     */
    private fun startedAt(elapsedMillis: Long): String {
        val startedAt =
            (Clock.System.now() - elapsedMillis.milliseconds)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        return "${startedAt.date}T${startedAt.hour.padded()}:${startedAt.minute.padded()}:${startedAt.second.padded()}"
    }

    private fun Int.padded(): String = toString().padStart(2, '0')

    private companion object {
        /**
         * How often the round clock is sampled. Well under a second so a warning tone lands on
         * its second rather than up to a second late, and far too little work to matter.
         */
        const val EMOM_POLL_MILLIS = 100L
    }
}
