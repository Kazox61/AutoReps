package com.kazox.autoreps.feature.record.domain

/**
 * An EMOM — "every minute on the minute".
 *
 * The clock, not the athlete, decides when a set starts: every [intervalSeconds] a new round
 * begins, and whatever is left of the interval is your rest. Working faster buys more rest, not
 * an earlier start — which is the whole point, and why the interval is fixed rather than
 * triggered by finishing.
 *
 * The plan is open-ended: it never ends by itself and only [RecordAction.FinishRecording] stops
 * it.
 *
 * @param warningSeconds how many seconds of warning tones precede each round. 0 disables them.
 */
data class EmomPlan(
    val intervalSeconds: Int,
    val warningSeconds: Int,
) {
    init {
        require(intervalSeconds > 0) { "an EMOM round cannot be $intervalSeconds seconds long" }
    }

    /**
     * Where the workout is [elapsedMillis] after the first round began.
     *
     * Derived from elapsed time rather than accumulated per round: a timer that adds up one-second
     * delays drifts, and by round ten of a five-minute EMOM the drift is what you would hear.
     */
    fun progressAt(elapsedMillis: Long): EmomProgress {
        val elapsed = elapsedMillis.coerceAtLeast(0)
        val intervalMillis = intervalSeconds * 1000L
        val completedRounds = elapsed / intervalMillis
        val millisLeft = intervalMillis - (elapsed % intervalMillis)

        return EmomProgress(
            round = (completedRounds + 1).toInt(),
            // Rounded up, so the last whole second of a round reads "1" rather than "0": zero is
            // the moment the next round starts, and it belongs to that round's display.
            secondsLeftInRound = ((millisLeft + 999) / 1000).toInt(),
        )
    }
}

/**
 * A snapshot of an EMOM in flight.
 *
 * @param round 1-based, counting up for as long as the session runs.
 * @param secondsLeftInRound counts down [EmomPlan.intervalSeconds] to 1. Not shown — the screen
 *   keeps counting total elapsed time up — but it drives the round's warning tones.
 */
data class EmomProgress(
    val round: Int,
    val secondsLeftInRound: Int,
)
