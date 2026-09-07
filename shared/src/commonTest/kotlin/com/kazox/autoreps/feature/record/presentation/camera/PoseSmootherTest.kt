package com.kazox.autoreps.feature.record.presentation.camera

import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PoseSmootherTest {
    private val smoother = PoseSmoother()

    /** ~30 fps, the rate the camera actually delivers. */
    private val frameMillis = 33L

    private fun Pose.wristX(): Float = this[LandmarkType.LEFT_WRIST]!!.x

    private fun poseAt(
        x: Float,
        y: Float = 0.5f,
    ): Pose =
        Pose(
            listOf(
                Pose.Landmark(
                    type = LandmarkType.LEFT_WRIST,
                    x = x,
                    y = y,
                    wx = 0f,
                    wy = 0f,
                    wz = 0f,
                    visibility = 1f,
                ),
            ),
        )

    @Test
    fun stillJitterIsDamped() {
        // A still body as the model reports it: wobbling a little each frame around 0.5.
        val inputs = List(40) { poseAt(if (it % 2 == 0) 0.49f else 0.51f) }
        var t = 0L
        val outputs = inputs.map { smoother.smooth(it, t.also { t += frameMillis })!! }

        val inputPeakToPeak = 0.02f
        val outputPeakToPeak = outputs.takeLast(20).maxOf { it.wristX() } - outputs.takeLast(20).minOf { it.wristX() }
        assertTrue(outputPeakToPeak < inputPeakToPeak / 3, "expected jitter damped, was $outputPeakToPeak")
    }

    @Test
    fun fastMovementIsFollowed() {
        // A wrist moving across a third of the frame in a second — the ramp's own speed.
        val inputs = List(30) { poseAt(0.5f + it * 0.033f) }
        var t = 0L
        val outputs = inputs.map { smoother.smooth(it, t.also { t += frameMillis })!! }

        val error = outputs.last().wristX() - inputs.last().wristX()
        assertTrue(kotlin.math.abs(error) < 0.1f, "expected to track within 0.1, lagged by $error")
    }

    @Test
    fun slowMovementIsFollowed() {
        val inputs = List(30) { poseAt(0.5f + it * 0.008f) }
        var t = 0L
        val outputs = inputs.map { smoother.smooth(it, t.also { t += frameMillis })!! }

        val error = outputs.last().wristX() - inputs.last().wristX()
        assertTrue(kotlin.math.abs(error) < 0.05f, "expected to track within 0.05, lagged by $error")
    }

    @Test
    fun lostPoseResetsTheFilter() {
        var t = 0L
        repeat(10) {
            smoother.smooth(poseAt(0.5f), t.also { t += frameMillis })
        }
        assertNull(smoother.smooth(null, t.also { t += frameMillis }))

        // The next detection must be adopted as-is, not dragged in from the old position.
        val adopted = smoother.smooth(poseAt(0.9f), t.also { t += frameMillis })!!
        assertEquals(0.9f, adopted.wristX())
    }

    @Test
    fun nonIncreasingTimestampPassesThePoseThrough() {
        smoother.smooth(poseAt(0.2f), 100)
        val out = smoother.smooth(poseAt(0.9f), 100)!!
        assertEquals(0.9f, out.wristX())
    }
}
