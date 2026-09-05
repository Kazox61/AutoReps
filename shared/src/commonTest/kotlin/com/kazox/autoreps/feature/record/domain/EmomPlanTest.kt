package com.kazox.autoreps.feature.record.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EmomPlanTest {
    private val plan = EmomPlan(intervalSeconds = 60, warningSeconds = 3)

    @Test
    fun `a fresh plan is on round one with the whole interval left`() {
        val progress = plan.progressAt(0)

        assertEquals(1, progress.round)
        assertEquals(60, progress.secondsLeftInRound)
    }

    @Test
    fun `the round advances exactly on the interval`() {
        assertEquals(1, plan.progressAt(59_999).round, "still inside round one")
        assertEquals(2, plan.progressAt(60_000).round, "the boundary belongs to the next round")
        assertEquals(2, plan.progressAt(119_999).round)
        assertEquals(3, plan.progressAt(120_000).round)
    }

    @Test
    fun `seconds left counts down to one and never shows zero`() {
        // Zero is the instant the next round starts, and belongs to that round's display — a
        // round that reads 0 is a round you are already late for.
        assertEquals(60, plan.progressAt(0).secondsLeftInRound)
        assertEquals(1, plan.progressAt(59_001).secondsLeftInRound)
        assertEquals(1, plan.progressAt(59_999).secondsLeftInRound)
        assertEquals(60, plan.progressAt(60_000).secondsLeftInRound, "the next round starts over")
    }

    @Test
    fun `each number is displayed for exactly one second`() {
        // Rounding up is what makes this true: "60" covers the first whole second rather than
        // flashing for one millisecond, so no number is ever skipped on screen.
        assertEquals(60, plan.progressAt(999).secondsLeftInRound)
        assertEquals(59, plan.progressAt(1_000).secondsLeftInRound)
        assertEquals(59, plan.progressAt(1_999).secondsLeftInRound)
        assertEquals(58, plan.progressAt(2_000).secondsLeftInRound)
    }

    @Test
    fun `the warning window covers the last seconds of the round and nothing else`() {
        // What the settings hint promises: three short tones, then a different one at zero. The
        // view model plays a tick whenever this lands in 1..warningSeconds.
        val warned = (0..60_000 step 1_000).filter { plan.progressAt(it.toLong()).secondsLeftInRound in 1..plan.warningSeconds }

        assertEquals(listOf(57_000, 58_000, 59_000), warned)
    }

    @Test
    fun `rounds keep counting up for as long as the session runs`() {
        assertEquals(61, plan.progressAt(3_600_000).round)
        assertEquals(25, plan.progressAt(1_440_000).round)
    }

    @Test
    fun `a shorter interval divides the same elapsed time into more rounds`() {
        val sprint = EmomPlan(intervalSeconds = 30, warningSeconds = 0)

        assertEquals(3, sprint.progressAt(60_000).round)
    }

    @Test
    fun `a zero-length round is rejected rather than dividing by zero`() {
        assertFailsWith<IllegalArgumentException> {
            EmomPlan(intervalSeconds = 0, warningSeconds = 3)
        }
    }
}
