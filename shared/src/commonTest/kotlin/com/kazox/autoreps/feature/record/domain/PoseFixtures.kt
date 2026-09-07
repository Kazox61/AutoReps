package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Builds synthetic poses so rep counting can be tested without a camera or a human.
 *
 * The elbow is placed at the world origin with the shoulder one unit above it, and the wrist is
 * swung to whatever angle is asked for — so [elbowAngleDegrees] is exactly what an
 * [AngleConstraint] will measure. Display-space heights are supplied directly, since that is how
 * a [HeightConstraint] reads them.
 */
fun poseOf(
    elbowAngleDegrees: Double,
    wristY: Float,
    noseY: Float,
    hipY: Float,
    ankleY: Float,
    visibility: Float = 1f,
): Pose {
    val radians = elbowAngleDegrees * PI / 180.0
    // Shoulder->elbow points along +y; the wrist is rotated away from it by the target angle.
    val wristWx = sin(radians).toFloat()
    val wristWy = (-1.0 + cos(radians)).toFloat()

    fun landmark(
        type: LandmarkType,
        y: Float,
        wx: Float = 0f,
        wy: Float = 0f,
    ) = Pose.Landmark(type = type, x = 0.5f, y = y, wx = wx, wy = wy, wz = 0f, visibility = visibility)

    return Pose(
        listOf(
            landmark(LandmarkType.NOSE, noseY),
            landmark(LandmarkType.LEFT_SHOULDER, 0.5f, wx = 0f, wy = 0f),
            landmark(LandmarkType.LEFT_ELBOW, 0.6f, wx = 0f, wy = -1f),
            landmark(LandmarkType.LEFT_WRIST, wristY, wx = wristWx, wy = wristWy),
            landmark(LandmarkType.LEFT_HIP, hipY),
            landmark(LandmarkType.LEFT_ANKLE, ankleY),
            // Right side deliberately absent: filmed side-on, only one arm is ever visible.
        ),
    )
}

/** Top of a push-up: arms extended, head high above the hands, feet level with them. */
fun topPose(visibility: Float = 1f): Pose =
    poseOf(
        elbowAngleDegrees = 165.0,
        wristY = 0.80f,
        noseY = 0.30f,
        hipY = 0.60f,
        ankleY = 0.75f,
        visibility = visibility,
    )

/** Bottom of a push-up: elbows bent, head down near the hands. */
fun bottomPose(visibility: Float = 1f): Pose =
    poseOf(
        elbowAngleDegrees = 100.0,
        wristY = 0.80f,
        noseY = 0.72f,
        hipY = 0.65f,
        ankleY = 0.75f,
        visibility = visibility,
    )

/** Halfway down: matches neither position. */
fun midPose(visibility: Float = 1f): Pose =
    poseOf(
        elbowAngleDegrees = 135.0,
        wristY = 0.80f,
        noseY = 0.50f,
        hipY = 0.62f,
        ankleY = 0.75f,
        visibility = visibility,
    )
