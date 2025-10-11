package com.kazox.posedetection

class HeightConstraint(
    val first: LandmarkType,
    val last: LandmarkType,
    val minDelta: Float,
    val maxDelta: Float
) {

    fun withinThreshold(pose: Pose): Boolean {
        val a = pose.getLandmark(first)
        val b = pose.getLandmark(last)

        if (a == null || b == null) return false

        val heightDiff = b.y - a.y
        return heightDiff in minDelta..maxDelta
    }

    fun debug(pose: Pose): String {
        val check = if (withinThreshold(pose)) "✓" else "✗"
        return check
    }
}
