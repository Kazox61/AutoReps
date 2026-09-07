package com.kazox.autoreps.feature.record.domain

/**
 * A repetition, positioned within the session that produced it.
 *
 * @param elapsedMillis time since the session started. Milliseconds here even though the stored
 *   model keeps whole seconds — rounding is the storage layer's business, and rep-to-rep cadence
 *   is not measurable once it has happened.
 * @param setIndex 1-based. Advanced either by a long enough gap since the previous rep or,
 *   during an EMOM, by the round clock declaring a new set.
 */
data class RecordedRep(
    val elapsedMillis: Long,
    val setIndex: Int,
)

/**
 * Accumulates the reps of one recording session and splits them into sets.
 *
 * Sets are normally inferred from rest, not declared: nobody presses a button between sets, so a
 * gap of [restThresholdMillis] since the last rep starts a new one. That is the only signal
 * available without asking the athlete to interact mid-workout.
 *
 * An EMOM has a better signal — the round clock — so it passes a null threshold and calls
 * [startNewSet] on each boundary instead of letting rest be guessed at.
 *
 * Holds no dependency on storage — a finished session is handed to whatever wants to persist it.
 */
class WorkoutSession(
    /** Gap that ends a set, or null when the caller declares the boundaries itself. */
    private val restThresholdMillis: Long? = DEFAULT_REST_THRESHOLD_MILLIS,
) {
    private val _reps = mutableListOf<RecordedRep>()
    val reps: List<RecordedRep> get() = _reps

    /** Session start, in the same clock the frames are timestamped with. Null until the first frame. */
    private var startedAtMillis: Long? = null
    private var lastRepAtMillis: Long? = null
    private var currentSet: Int = 0

    /** True while the next rep should open a set. Starts true: the first rep always opens one. */
    private var startsNewSet: Boolean = true

    val repCount: Int get() = _reps.size
    val setCount: Int get() = currentSet

    /** Reps in the set currently being performed — the number the record screen counts up. */
    val repsInCurrentSet: Int get() = _reps.count { it.setIndex == currentSet }

    /** Elapsed session time, or 0 before it starts. */
    fun elapsedMillis(nowMillis: Long): Long = startedAtMillis?.let { nowMillis - it } ?: 0L

    /**
     * Marks the session as running from [nowMillis]. Called on the first frame rather than at
     * construction so elapsed times are relative to the camera clock, not to whenever the screen
     * happened to be built.
     */
    fun startIfNeeded(nowMillis: Long) {
        if (startedAtMillis == null) startedAtMillis = nowMillis
    }

    /**
     * Declares that the next rep belongs to a new set, for callers that know where the boundary
     * is instead of inferring it.
     *
     * Deferred rather than incrementing straight away: a round nobody managed a rep in would
     * otherwise leave a hole in the set numbering, and every chart downstream groups by that
     * number. A round with no reps simply does not appear.
     */
    fun startNewSet() {
        startsNewSet = true
    }

    fun record(repAtMillis: Long): RecordedRep {
        startIfNeeded(repAtMillis)
        val start = startedAtMillis ?: repAtMillis

        val previous = lastRepAtMillis
        val rested =
            restThresholdMillis != null &&
                previous != null &&
                repAtMillis - previous >= restThresholdMillis
        if (startsNewSet || rested) {
            currentSet++
            startsNewSet = false
        }
        lastRepAtMillis = repAtMillis

        return RecordedRep(
            elapsedMillis = repAtMillis - start,
            setIndex = currentSet,
        ).also { _reps += it }
    }

    /**
     * Reps per set, in set order.
     *
     * The workout screen builds its charts from the stored rows via `WorkoutAnalysis.repsPerSet`,
     * not from here — this is the in-memory view of the same split, and is what the tests assert
     * set-detection through.
     */
    fun repsPerSet(): List<Int> = _reps.groupBy { it.setIndex }.toList().sortedBy { it.first }.map { it.second.size }

    companion object {
        /**
         * A pause this long ends a set. Long enough to survive a slow rep or a lost-tracking
         * blip, short enough that a real breather between sets registers.
         *
         * Only a fallback now: the app always passes the value from Settings, whose own default
         * matches this one. Tests are the remaining caller.
         */
        const val DEFAULT_REST_THRESHOLD_MILLIS: Long = 10_000
    }
}
