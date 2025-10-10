package com.kazox.posedetection

data class Pose(
    val landmarks: List<Landmark>
) {
    data class Landmark(
        val type: Int,
        val x: Float,
        val y: Float,
        val wx: Float,
        val wy: Float,
        val wz: Float,
        val confidence: Float
    )

    fun getLandmark(type: Int): Landmark? {
        return landmarks.find { it.type == type }
    }
}