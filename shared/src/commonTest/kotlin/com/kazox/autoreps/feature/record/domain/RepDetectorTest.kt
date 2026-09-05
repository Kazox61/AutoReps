package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val FRAME_MS = 33L

/** Feeds [pose] for [durationMillis], returning every rep timestamp produced. */
private fun RepDetector.hold(
    pose: Pose?,
    durationMillis: Long,
    fromMillis: Long,
): Pair<List<Long>, Long> {
    val reps = mutableListOf<Long>()
    var t = fromMillis
    val end = fromMillis + durationMillis
    while (t < end) {
        onFrame(pose, t)?.let { reps += it }
        t += FRAME_MS
    }
    return reps to t
}

class RepDetectorTest {
    private fun detector() = PushupExercise.detector()

    /** The pose with the ankles demoted to guesses — feet occluded behind the torso, filmed side-on. */
    private fun Pose.withGuessedAnkles(): Pose =
        Pose(landmarks.map { if (it.type == LandmarkType.LEFT_ANKLE) it.copy(visibility = 0.05f) else it })

    @Test
    fun `fixtures match the positions they claim to`() {
        assertTrue(PushupExercise.topPosition.matches(topPose()), "top")
        assertTrue(PushupExercise.bottomPosition.matches(bottomPose()), "bottom")
        assertTrue(!PushupExercise.topPosition.matches(midPose()), "mid is not top")
        assertTrue(!PushupExercise.bottomPosition.matches(midPose()), "mid is not bottom")
    }

    @Test
    fun `counts one rep for a full down-and-up cycle`() {
        val detector = detector()
        var t = 0L
        val reps = mutableListOf<Long>()

        listOf(topPose(), midPose(), bottomPose(), midPose(), topPose()).forEach { pose ->
            val (found, next) = detector.hold(pose, durationMillis = 500, fromMillis = t)
            reps += found
            t = next
        }

        assertEquals(1, reps.size)
        assertEquals(1, detector.repCount)
    }

    @Test
    fun `counts each of three consecutive reps`() {
        val detector = detector()
        var t = 0L
        val reps = mutableListOf<Long>()

        val (_, afterTop) = detector.hold(topPose(), 500, t)
        t = afterTop
        repeat(3) {
            listOf(midPose(), bottomPose(), midPose(), topPose()).forEach { pose ->
                val (found, next) = detector.hold(pose, 500, t)
                reps += found
                t = next
            }
        }

        assertEquals(3, reps.size)
        assertEquals(3, detector.repCount)
    }

    @Test
    fun `going only halfway down is not a rep`() {
        val detector = detector()
        var t = 0L
        val reps = mutableListOf<Long>()

        listOf(topPose(), midPose(), topPose(), midPose(), topPose()).forEach { pose ->
            val (found, next) = detector.hold(pose, 500, t)
            reps += found
            t = next
        }

        assertEquals(0, reps.size, "never reached the bottom position")
    }

    @Test
    fun `single-frame flicker to the bottom position does not count`() {
        val detector = detector()
        var t = 0L
        val (_, afterTop) = detector.hold(topPose(), 500, t)
        t = afterTop

        // One stray frame, far shorter than minPhaseHoldMillis — exactly the landmark jitter
        // the old detector would have turned into half a rep.
        assertNull(detector.onFrame(bottomPose(), t))
        t += FRAME_MS

        val (reps, _) = detector.hold(topPose(), 500, t)
        assertEquals(0, reps.size)
        assertEquals(0, detector.repCount)
    }

    @Test
    fun `losing tracking mid-rep abandons the cycle`() {
        val detector = detector()
        var t = 0L

        val (_, afterTop) = detector.hold(topPose(), 500, t)
        t = afterTop
        val (_, afterBottom) = detector.hold(bottomPose(), 500, t)
        t = afterBottom

        // Athlete leaves the frame for long enough to be believed, then returns to the top.
        val (_, afterGone) = detector.hold(null, 1_000, t)
        t = afterGone

        val (reps, _) = detector.hold(topPose(), 500, t)
        assertEquals(0, reps.size, "the abandoned descent must not complete on return")
        assertEquals(0, detector.repCount)
    }

    @Test
    fun `feet occluded behind the torso still count`() {
        val detector = detector()
        var t = 0L
        val reps = mutableListOf<Long>()

        // Every frame with the ankles as low-visibility guesses, the way MediaPipe reports a
        // body filmed side-on. The coarse level checks run on the predicted positions; only the
        // elbow angle demands landmarks the model actually sees.
        listOf(topPose(), midPose(), bottomPose(), midPose(), topPose()).forEach { pose ->
            val (found, next) = detector.hold(pose.withGuessedAnkles(), durationMillis = 500, fromMillis = t)
            reps += found
            t = next
        }

        assertEquals(1, reps.size, "occluded feet must not cost the rep")
        assertEquals(1, detector.repCount)
    }

    @Test
    fun `landmarks the model cannot see are not trusted`() {
        val detector = detector()
        // Geometrically a perfect top position, but every landmark is a guess — the elbow angle
        // refuses to measure a joint the model invented.
        val (reps, _) = detector.hold(topPose(visibility = 0.1f), 500, 0)
        assertEquals(0, reps.size)
        assertEquals(RepPhase.TRANSITION, detector.phase, "unusable landmarks are not a position")
    }

    @Test
    fun `rep timestamp is the frame that completed it`() {
        val detector = detector()
        var t = 0L
        listOf(topPose(), bottomPose()).forEach {
            val (_, next) = detector.hold(it, 500, t)
            t = next
        }
        val start = t
        val (reps, _) = detector.hold(topPose(), 500, t)

        assertEquals(1, reps.size)
        val elapsed = reps.single() - start
        // The voting window has to fill with top frames before the hold even starts.
        val windowFill = (RepDetectorConfig().windowFrames - 1) * FRAME_MS
        assertTrue(elapsed in 0..RepDetectorConfig().minPhaseHoldMillis + windowFill + FRAME_MS, "was $elapsed ms")
    }

    @Test
    fun `dropped detections while at the bottom do not reset the hold`() {
        val detector = detector()
        var t = 0L

        val (_, afterTop) = detector.hold(topPose(), 500, t)
        t = afterTop

        // Every third frame fails to detect — inference falling behind, the way a loaded phone
        // delivers it. Under the consecutive-hold rule each gap restarted the clock and the
        // bottom never confirmed: green readout, zero reps.
        repeat(15) {
            detector.onFrame(bottomPose(), t)
            t += FRAME_MS
            detector.onFrame(bottomPose(), t)
            t += FRAME_MS
            detector.onFrame(null, t)
            t += FRAME_MS
        }

        assertEquals(RepPhase.END, detector.phase, "the bottom must be believed despite the gaps")
        assertEquals(0, detector.repCount, "being at the bottom is not yet a rep")
    }

    @Test
    fun `band-edge jitter at the bottom still confirms the bottom`() {
        val detector = detector()
        var t = 0L

        val (_, afterTop) = detector.hold(topPose(), 500, t)
        t = afterTop

        // The elbow oscillating across the 125 degree band edge: bottom, mid, bottom, mid. The
        // diagnostic readout looks solidly green to an eye at 30fps.
        repeat(12) {
            detector.onFrame(bottomPose(), t)
            t += FRAME_MS
            detector.onFrame(midPose(), t)
            t += FRAME_MS
        }

        assertEquals(RepPhase.END, detector.phase, "a 50 percent duty cycle at the band edge is a real bottom")
    }
}

class TrackabilityTest {
    /** A face and nothing else — what MediaPipe reports when you walk up to the camera. */
    private fun faceOnlyPose() =
        com.kazox.autoreps.core.domain.pose.Pose(
            listOf(
                com.kazox.autoreps.core.domain.pose.Pose.Landmark(
                    type = com.kazox.autoreps.core.domain.pose.LandmarkType.NOSE,
                    x = 0.5f,
                    y = 0.3f,
                    wx = 0f,
                    wy = 0f,
                    wz = 0f,
                    visibility = 0.99f,
                ),
            ),
        )

    @Test
    fun `a face alone is not trackable`() {
        val detector = PushupExercise.detector()
        assertTrue(!detector.canTrack(faceOnlyPose()), "a nose is not a push-up")
    }

    @Test
    fun `a full side-on body is trackable`() {
        val detector = PushupExercise.detector()
        assertTrue(detector.canTrack(topPose()))
    }

    @Test
    fun `feet out of frame is reported and not silently uncounted`() {
        val detector = PushupExercise.detector()
        val noAnkles =
            com.kazox.autoreps.core.domain.pose.Pose(
                topPose().landmarks.filterNot {
                    it.type == com.kazox.autoreps.core.domain.pose.LandmarkType.LEFT_ANKLE
                },
            )
        // The feet are needed to tell a push-up from someone standing, so they are required —
        // but the user has to be told, not left watching a counter that never moves.
        assertTrue(!detector.canTrack(noAnkles), "must surface that the camera cannot see enough")
    }

    @Test
    fun `standing and swinging your arms counts nothing`() {
        val detector = PushupExercise.detector()
        var t = 0L
        val reps = mutableListOf<Long>()

        // Upright, feet on the floor, arms straightening and bending — the false positive that
        // dropping the ankle check let through.
        fun standing(elbowAngle: Double) =
            poseOf(
                elbowAngleDegrees = elbowAngle,
                wristY = 0.55f,
                noseY = 0.20f,
                hipY = 0.58f,
                ankleY = 0.95f,
            )

        repeat(3) {
            listOf(standing(165.0), standing(120.0), standing(100.0), standing(120.0)).forEach { pose ->
                val (found, next) = detector.hold(pose, 500, t)
                reps += found
                t = next
            }
        }

        assertEquals(0, reps.size, "arm swings while upright are not push-ups")
    }

    @Test
    fun `standing upright is not the top of a push-up`() {
        // The guard the ankle constraint used to provide: arms straight, but the torso vertical.
        val standing =
            poseOf(
                elbowAngleDegrees = 165.0,
                wristY = 0.72f,
                noseY = 0.12f,
                hipY = 0.85f,
                ankleY = 0.98f,
            )
        assertTrue(
            !PushupExercise.topPosition.matches(standing),
            "a standing body must not read as a plank",
        )
    }
}
