package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.BodySide
import com.kazox.autoreps.core.domain.pose.LandmarkType

/**
 * Push-up detection thresholds.
 *
 * Each position is described once per body side and matches if **either** side does, because the
 * camera sees one arm and guesses the other. Angles are world-space degrees at the elbow; heights
 * are fractions of frame height in display space, where a negative delta means the second
 * landmark sits *above* the first on screen.
 */
object PushupExercise {
    /**
     * How level the torso has to be, as a fraction of frame height between shoulder and hip.
     *
     * Shared by both positions so the top and bottom judge the body the same way. Generous on
     * the positive side because hips sag as a set wears on, and a sagging push-up is still a
     * push-up — refusing to count it is worse than counting a sloppy one.
     */
    private const val TORSO_MIN = -0.2f
    private const val TORSO_MAX = 0.3f

    /**
     * How level hands and feet must be, as a fraction of frame height.
     *
     * The only reliable way to tell a push-up from someone standing and swinging their arms.
     * Torso level was tried instead so the legs could stay out of shot, but a standing body's
     * shoulder-to-hip gap is around 0.2–0.3 of the frame — inside any range wide enough to
     * accept a sagging plank — so it does not discriminate.
     *
     * Applied to **both** positions on purpose. Requiring a landmark in only one of them means a
     * body the camera can half-see matches one half of the cycle forever and completes none of
     * it, counting nothing and saying nothing. Symmetric requirements make
     * [RepDetector.canTrack] able to report the real problem: move the camera back.
     */
    private const val LIMBS_LEVEL = 0.2f

    private fun elbow(side: BodySide) =
        when (side) {
            BodySide.LEFT -> Triple(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST)
            BodySide.RIGHT -> Triple(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST)
        }

    private fun wrist(side: BodySide) =
        if (side == BodySide.LEFT) LandmarkType.LEFT_WRIST else LandmarkType.RIGHT_WRIST

    private fun hip(side: BodySide) =
        if (side == BodySide.LEFT) LandmarkType.LEFT_HIP else LandmarkType.RIGHT_HIP

    private fun shoulder(side: BodySide) =
        if (side == BodySide.LEFT) LandmarkType.LEFT_SHOULDER else LandmarkType.RIGHT_SHOULDER

    private fun ankle(side: BodySide) =
        if (side == BodySide.LEFT) LandmarkType.LEFT_ANKLE else LandmarkType.RIGHT_ANKLE

    /** Arms extended, body in a plank: the top of a push-up. */
    val topPosition: ExercisePosition =
        ExercisePosition(
            BodySide.entries.map { side ->
                val (shoulder, elbowJoint, wristJoint) = elbow(side)
                listOf(
                    AngleConstraint(shoulder, elbowJoint, wristJoint, targetAngle = 165.0, tolerance = 15.0),
                    // Head well clear of the hands.
                    HeightConstraint(wrist(side), LandmarkType.NOSE, minDelta = -1f, maxDelta = 0.35f),
                    // Torso roughly level — this is what rules out standing up.
                    //
                    // Was wrist-to-ankle ("hands and feet level"), which asked for the one part
                    // of the body most likely to be out of shot. Because only the top position
                    // read the ankles and only a top *and* a bottom make a rep, feet outside the
                    // frame meant nothing was ever counted, silently. Shoulder and hip say the
                    // same thing — the body is horizontal, not upright — and both are already
                    // required by the elbow angle, so this needs nothing new in frame.
                    HeightConstraint(shoulder(side), hip(side), minDelta = TORSO_MIN, maxDelta = TORSO_MAX),
                    // Hands and feet roughly level — rules out standing up.
                    HeightConstraint(wrist(side), ankle(side), minDelta = -LIMBS_LEVEL, maxDelta = LIMBS_LEVEL),
                )
            },
        )

    /** Chest lowered, elbows bent: the bottom of a push-up. */
    val bottomPosition: ExercisePosition =
        ExercisePosition(
            BodySide.entries.map { side ->
                val (shoulder, elbowJoint, wristJoint) = elbow(side)
                listOf(
                    AngleConstraint(shoulder, elbowJoint, wristJoint, targetAngle = 100.0, tolerance = 25.0),
                    // Head down near the hands — the depth requirement.
                    //
                    // -0.2 asked the nose to come within a fifth of the frame height of the
                    // hands, which side-on is close to touching them. A normal push-up lowers
                    // the head by perhaps 0.2 of frame height from a top of around -0.45, so
                    // the old floor was often simply unreachable and the bottom never matched.
                    // The elbow angle above is the real discriminator; this only has to insist
                    // the head actually came down.
                    HeightConstraint(wrist(side), LandmarkType.NOSE, minDelta = -0.4f, maxDelta = 0.15f),
                    HeightConstraint(shoulder(side), hip(side), minDelta = TORSO_MIN, maxDelta = TORSO_MAX),
                    // Same requirement as the top: both halves of a rep must need the same
                    // landmarks, or a partly visible body silently never completes one.
                    HeightConstraint(wrist(side), ankle(side), minDelta = -LIMBS_LEVEL, maxDelta = LIMBS_LEVEL),
                )
            },
        )

    fun detector(config: RepDetectorConfig = RepDetectorConfig()): RepDetector =
        RepDetector(startPosition = topPosition, endPosition = bottomPosition, config = config)
}
