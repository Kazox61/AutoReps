package com.kazox.autoreps.core

import kotlin.math.PI
import kotlin.math.acos
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