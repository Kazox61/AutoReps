package com.kazox.posedetection

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.roundToInt
import kotlin.math.sqrt

class AngleConstraint(
    val first: LandmarkType,
    val mid: LandmarkType,
    val last: LandmarkType,
    val targetAngle: Double,
    val tolerance: Double
) {

    fun withinThreshold(pose: Pose): Boolean {
        val a = pose.getLandmark(first)
        val b = pose.getLandmark(mid)
        val c = pose.getLandmark(last)

        if (a == null || b == null || c == null) return false

        val angle = getAngle(a, b, c)
        return angle in (targetAngle - tolerance)..(targetAngle + tolerance)
    }

    fun debug(pose: Pose): String {
        val a = pose.getLandmark(first)
        val b = pose.getLandmark(mid)
        val c = pose.getLandmark(last)

        var angle = 0.0
        if (a != null && b != null && c != null) {
            angle = getAngle(a, b, c)
        }

        val check = if (withinThreshold(pose)) "✓" else "✗"
        val posA = listOf(a?.wx, a?.wy, a?.wz).map { ((it ?: 0f) * 10).roundToInt() / 10f }
        val posB = listOf(b?.wx, b?.wy, b?.wz).map { ((it ?: 0f) * 10).roundToInt() / 10f }
        val posC = listOf(c?.wx, c?.wy, c?.wz).map { ((it ?: 0f) * 10).roundToInt() / 10f }

        return "$check ${angle.roundToInt()}°, ($posA), ($posB), ($posC)"
    }

    private fun getAngle(a: Pose.Landmark, b: Pose.Landmark, c: Pose.Landmark): Double {
        val ab = Triple(a.wx - b.wx, a.wy - b.wy, a.wz - b.wz)
        val cb = Triple(c.wx - b.wx, c.wy - b.wy, c.wz - b.wz)

        val dot = ab.first * cb.first + ab.second * cb.second + ab.third * cb.third
        val magAB = sqrt(ab.first * ab.first + ab.second * ab.second + ab.third * ab.third)
        val magCB = sqrt(cb.first * cb.first + cb.second * cb.second + cb.third * cb.third)

        if (magAB == 0f || magCB == 0f) return 0.0

        val cosTheta = (dot / (magAB * magCB)).coerceIn(-1.0f, 1.0f)
        return acos(cosTheta.toDouble()) * (180.0 / PI)
    }
}