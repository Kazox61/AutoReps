package com.kazox.autoreps.feature.record.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class WorkoutSessionTest {
    @Test
    fun `reps close together land in one set`() {
        val session = WorkoutSession(restThresholdMillis = 10_000)
        session.startIfNeeded(1_000)
        listOf(1_000L, 3_000L, 5_500L, 8_000L).forEach { session.record(it) }

        assertEquals(4, session.repCount)
        assertEquals(1, session.setCount)
        assertEquals(listOf(4), session.repsPerSet())
    }

    @Test
    fun `a long enough pause starts a new set`() {
        val session = WorkoutSession(restThresholdMillis = 10_000)
        session.startIfNeeded(0)
        listOf(0L, 2_000L, 4_000L).forEach { session.record(it) }
        // 60s breather.
        listOf(64_000L, 66_000L).forEach { session.record(it) }

        assertEquals(5, session.repCount)
        assertEquals(2, session.setCount)
        assertEquals(listOf(3, 2), session.repsPerSet())
    }

    @Test
    fun `a pause just under the threshold stays in the same set`() {
        val session = WorkoutSession(restThresholdMillis = 10_000)
        session.startIfNeeded(0)
        session.record(0)
        session.record(9_999)

        assertEquals(1, session.setCount, "9.999s is a slow rep, not a rest")
    }

    @Test
    fun `elapsed time is measured from the first frame and not from construction`() {
        val session = WorkoutSession()
        // Camera clock starts wherever it likes — frame timestamps are not epoch-based.
        session.startIfNeeded(5_000_000)
        val rep = session.record(5_002_500)

        assertEquals(2_500, rep.elapsedMillis)
    }

    @Test
    fun `the first rep opens set one`() {
        val session = WorkoutSession()
        val rep = session.record(1_234)

        assertEquals(1, rep.setIndex)
        assertEquals(0, rep.elapsedMillis, "the session starts at its first rep if not started earlier")
    }
}

class DeclaredSetsTest {
    /** How an EMOM builds its session: rounds are the sets, so rest must not split them. */
    private fun emomSession() = WorkoutSession(restThresholdMillis = null)

    @Test
    fun `rest does not split a set when the caller declares the boundaries`() {
        val session = emomSession()

        session.record(0)
        // A minute of rest inside one round — far past any inferred threshold.
        session.record(60_000)

        assertEquals(1, session.setCount, "nothing declared a new set, so there is only one")
        assertEquals(listOf(2), session.repsPerSet())
    }

    @Test
    fun `startNewSet moves the following reps into the next set`() {
        val session = emomSession()

        session.record(0)
        session.record(1_000)
        session.startNewSet()
        session.record(2_000)

        assertEquals(2, session.setCount)
        assertEquals(listOf(2, 1), session.repsPerSet())
    }

    @Test
    fun `a round nobody managed a rep in leaves no hole in the numbering`() {
        val session = emomSession()

        session.record(0)
        session.startNewSet() // round two: no reps at all
        session.startNewSet() // round three
        session.record(2_000)

        // Two sets, numbered 1 and 2 — every chart downstream groups by this number, and a gap
        // would read as a set that existed and was empty rather than a round that was skipped.
        assertEquals(2, session.setCount)
        assertEquals(listOf(1, 1), session.repsPerSet())
    }

    @Test
    fun `declaring a set before any rep does not create an empty first set`() {
        val session = emomSession()

        session.startNewSet()
        session.record(0)

        assertEquals(1, session.setCount)
    }
}
