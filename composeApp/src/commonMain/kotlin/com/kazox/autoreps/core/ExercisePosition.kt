package com.kazox.autoreps.core

class ExercisePosition(
    val angleConstraints: List<AngleConstraint>,
    val heightConstraints: List<HeightConstraint>
) {

    fun hasValidConstraints(pose: Pose): Boolean {
        return angleConstraints.all { it.withinThreshold(pose) } &&
                heightConstraints.all { it.withinThreshold(pose) }
    }
}
