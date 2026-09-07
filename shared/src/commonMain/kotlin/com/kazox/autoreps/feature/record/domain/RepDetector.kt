package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.Pose

/** One position's live constraint readings, for on-device tuning. */
data class PositionDiagnostics(
    val matches: Boolean,
    val readings: List<ConstraintReading>,
)

/** Where the body is within a repetition, as far as the detector can currently tell. */
enum class RepPhase {
    /** Matching the exercise's start position — arms extended, for a push-up. */
    START,

    /** Matching the end position — chest down. */
    END,

    /** A recognisable body, between the two positions. */
    TRANSITION,

    /** No pose, or not enough visible landmarks to judge. */
    ABSENT,
}

/**
 * @param windowFrames how many recent frames vote on what the body is doing. Raw classification
 *   flickers exactly where reps happen — the constraint band edges — and a detection gap drops a
 *   null pose in; a strict majority over this window makes a stray frame weight nothing.
 * @param minPhaseHoldMillis how long the voted phase must persist before it counts as real.
 *   Guards against landmark jitter flickering across a constraint boundary and manufacturing
 *   reps out of noise. Roughly four frames at 30fps.
 * @param minRepIntervalMillis floor on the time between two reps. A push-up takes about a
 *   second; anything faster is the detector being fooled, not an athlete.
 */
data class RepDetectorConfig(
    val windowFrames: Int = 6,
    val minPhaseHoldMillis: Long = 120,
    val minRepIntervalMillis: Long = 600,
) {
    init {
        // Zero votes would make effectivePhase() ask for the max of nothing, mid-workout.
        require(windowFrames >= 1) { "windowFrames must vote on at least one frame, was $windowFrames" }
    }
}

/**
 * Counts repetitions from a stream of poses.
 *
 * A rep is a full cycle: settle in the start position, reach the end position, come back. Both
 * ends have to *hold* — see [RepDetectorConfig.minPhaseHoldMillis] — so a single noisy frame
 * cannot advance the cycle.
 *
 * Raw classifications are majority-voted over [RepDetectorConfig.windowFrames] before the state
 * machine sees them. Requiring the hold on the *raw* signal — as an earlier version did — meant
 * one dropped detection or one frame of band-edge jitter reset the hold timer, and a phase that
 * flickered every two or three frames never confirmed at all: the readout showed green while the
 * count stayed at zero.
 *
 * Losing the athlete resets the cycle rather than leaving it half-armed, so walking out of frame
 * mid-push-up and returning later cannot complete the rep that was abandoned.
 *
 * Not thread-safe: feed it from one thread. Detection callbacks are already marshalled to the
 * main thread on both platforms.
 */
class RepDetector(
    private val startPosition: ExercisePosition,
    private val endPosition: ExercisePosition,
    private val config: RepDetectorConfig = RepDetectorConfig(),
) {
    /** The last phase that held long enough to be believed. */
    var phase: RepPhase = RepPhase.ABSENT
        private set

    var repCount: Int = 0
        private set

    /** The last [RepDetectorConfig.windowFrames] raw classifications, oldest first. */
    private val window = ArrayDeque<RepPhase>()

    private var candidate: RepPhase = RepPhase.ABSENT

    // Nullable rather than a Long.MIN_VALUE sentinel: `now - Long.MIN_VALUE` overflows to a
    // negative gap, which reads as "no time has passed" and suppressed every rep.
    private var candidateSinceMillis: Long? = null

    /** Last confirmed START or END. TRANSITION does not disturb it — passing through is normal. */
    private var lastAnchor: RepPhase = RepPhase.ABSENT

    /** True once a confirmed END has followed a confirmed START: the descent is done. */
    private var descended: Boolean = false

    private var lastRepAtMillis: Long? = null

    /**
     * Feeds one frame in.
     *
     * @param pose the frame's pose, or null when nothing was detected.
     * @param timestampMillis the frame's capture time. Must be monotonically increasing; the
     *   camera layer supplies frame timestamps rather than wall-clock time so it cannot go
     *   backwards.
     * @return the timestamp of a completed rep, or null. Returning the rep instead of firing a
     *   listener keeps the caller in control of ordering and makes this testable without
     *   subscribing to anything.
     */
    fun onFrame(
        pose: Pose?,
        timestampMillis: Long,
    ): Long? {
        window.addLast(classify(pose))
        while (window.size > config.windowFrames) window.removeFirst()

        val observed = effectivePhase()

        val since = candidateSinceMillis
        if (observed != candidate || since == null) {
            candidate = observed
            candidateSinceMillis = timestampMillis
            return null
        }
        // Not held long enough yet — say nothing rather than act on a flicker.
        if (timestampMillis - since < config.minPhaseHoldMillis) return null
        if (observed == phase) return null

        phase = observed
        return when (observed) {
            RepPhase.END -> {
                if (lastAnchor == RepPhase.START) descended = true
                lastAnchor = RepPhase.END
                null
            }

            RepPhase.START -> {
                val completed = descended && lastAnchor == RepPhase.END
                lastAnchor = RepPhase.START
                if (!completed) return null
                descended = false
                // A cycle this fast is jitter that happened to hold, not a repetition.
                val previousRep = lastRepAtMillis
                if (previousRep != null && timestampMillis - previousRep < config.minRepIntervalMillis) {
                    return null
                }
                lastRepAtMillis = timestampMillis
                repCount++
                timestampMillis
            }

            RepPhase.ABSENT -> {
                // Tracking held as lost. Abandon the cycle: whatever happens next is a new one.
                descended = false
                lastAnchor = RepPhase.ABSENT
                null
            }

            RepPhase.TRANSITION -> null
        }
    }

    /**
     * Whether this pose carries enough visible landmarks for *both* positions to be judged.
     *
     * Both, not either: a rep needs the top and the bottom. If the camera can see you at the
     * bottom but loses your feet at the top, no repetition can ever complete — so reporting
     * "tracked" in that state would be a lie that costs the user their whole set.
     */
    fun canTrack(pose: Pose?): Boolean {
        if (pose == null) return false
        return startPosition.isMeasurable(pose) && endPosition.isMeasurable(pose)
    }

    /**
     * Live readings for **both** positions, ordered `[start, end]`; the caller labels them for
     * display.
     *
     * Both, not the current one: while moving between them the phase is TRANSITION, and an
     * earlier version reported the start position in that case — so the readout could never
     * show why the *bottom* failed to match, which is the question anyone debugging a
     * miscounting rep is actually asking.
     */
    fun diagnose(pose: Pose?): List<PositionDiagnostics> {
        if (pose == null) return emptyList()
        return listOf(
            PositionDiagnostics(
                matches = startPosition.matches(pose),
                readings = startPosition.diagnose(pose),
            ),
            PositionDiagnostics(
                matches = endPosition.matches(pose),
                readings = endPosition.diagnose(pose),
            ),
        )
    }

    /** Forgets the cycle in progress, keeping [repCount]. Use when a set is deliberately ended. */
    fun resetCycle() {
        window.clear()
        candidate = RepPhase.ABSENT
        candidateSinceMillis = null
        phase = RepPhase.ABSENT
        lastAnchor = RepPhase.ABSENT
        descended = false
    }

    /**
     * The phase the recent window actually supports: a strict majority of the raw votes.
     *
     * Ties are broken away from [RepPhase.TRANSITION] first — an even split between "bottom" and
     * "somewhere in between" is the elbow sitting on the band edge, and the position is the more
     * useful answer — then towards the current candidate, so an even split never flip-flops.
     */
    private fun effectivePhase(): RepPhase {
        val counts = window.groupingBy { it }.eachCount()
        val best = counts.values.max()
        val leaders = counts.filterValues { it == best }.keys
        if (leaders.size == 1) return leaders.single()

        val positional = leaders.filterNot { it == RepPhase.TRANSITION }
        if (positional.size == 1) return positional.single()
        return if (candidate in leaders) candidate else leaders.first()
    }

    private fun classify(pose: Pose?): RepPhase {
        if (pose == null) return RepPhase.ABSENT
        return when {
            startPosition.matches(pose) -> RepPhase.START
            endPosition.matches(pose) -> RepPhase.END
            else -> RepPhase.TRANSITION
        }
    }
}
