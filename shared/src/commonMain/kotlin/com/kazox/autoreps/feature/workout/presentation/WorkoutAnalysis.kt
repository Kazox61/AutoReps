package com.kazox.autoreps.feature.workout.presentation

import com.kazox.autoreps.core.domain.model.Rep

/*
 * Everything derivable from a workout's reps without any schema change.
 *
 * [Rep.timestamp] is seconds since the workout started, so the rep list carries the session's
 * whole temporal shape — when work happened and when it didn't. Counting reps per set throws
 * that away; these functions keep it.
 *
 * All of them assume reps arrive ordered by set then timestamp, which the DAO query guarantees.
 */

/** Reps grouped into sets, in set order. */
private fun sets(reps: List<Rep>): List<List<Rep>> =
    reps.groupBy { it.setId }.toList().sortedBy { it.first }.map { it.second }

/** Reps per set, in set order. */
internal fun repsPerSet(reps: List<Rep>): List<Int> = sets(reps).map { it.size }

/**
 * Seconds of rest after each set — last rep of one set to first rep of the next. One shorter
 * than the set count, and empty for a single-set workout.
 */
internal fun restAfterEachSet(reps: List<Rep>): List<Int> {
    val sets = sets(reps)
    if (sets.size < 2) return emptyList()
    return sets.zipWithNext { current, next ->
        (next.first().timestamp - current.last().timestamp).coerceAtLeast(0)
    }
}

/**
 * Seconds spent inside sets — each set's first-to-last rep span, summed.
 *
 * The analogue of Strava's moving time. Note it under-counts by one rep's worth per set: the
 * final rep of a set has no successor to measure against.
 */
internal fun workSeconds(reps: List<Rep>): Int =
    sets(reps).sumOf { set -> set.last().timestamp - set.first().timestamp }

/** Seconds spent resting between sets. The counterpart to [workSeconds]. */
internal fun restSeconds(reps: List<Rep>): Int = restAfterEachSet(reps).sum()

/**
 * How far the last set fell below the best one, as a fraction of the best. `0.4` means the
 * closing set managed 60% of the peak — the single number that says how hard the session bit.
 *
 * Null when there are fewer than two sets, or the best set is empty.
 */
internal fun dropOff(reps: List<Rep>): Double? {
    val perSet = repsPerSet(reps)
    if (perSet.size < 2) return null
    val best = perSet.max()
    if (best == 0) return null
    return (best - perSet.last()).toDouble() / best
}

/**
 * Seconds each rep took, in workout order — the gap from the previous rep in the same set.
 *
 * The first rep of every set is dropped: its predecessor is on the far side of a rest, so the
 * gap would measure recovery, not effort. The result therefore has one entry fewer per set than
 * there are reps, and reads as a sawtooth — quick at each set's start, dragging by its end.
 *
 * This is the view no manual logger can draw, and at the current one-second resolution it is
 * also the view that would gain the most from storing [Rep.timestamp] in milliseconds.
 */
internal fun repCadence(reps: List<Rep>): List<Int> =
    sets(reps).flatMap { set ->
        set.zipWithNext { previous, current ->
            (current.timestamp - previous.timestamp).coerceAtLeast(0)
        }
    }

/** Mean seconds per rep, ignoring rest. Null when no set has two reps to measure between. */
internal fun averageCadence(reps: List<Rep>): Double? =
    repCadence(reps).takeIf { it.isNotEmpty() }?.average()
