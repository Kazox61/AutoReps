package com.kazox.autoreps.core

data class Pose(
    val landmarks: List<Landmark>
) {
    data class Landmark(
        val type: LandmarkType,
        val x: Float,
        val y: Float,
        val wx: Float,
        val wy: Float,
        val wz: Float
    )

    fun getLandmark(type: LandmarkType): Landmark? {
        return landmarks.find { it.type == type }
    }
}