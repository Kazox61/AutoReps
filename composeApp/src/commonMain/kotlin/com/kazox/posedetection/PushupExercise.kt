package com.kazox.posedetection

val startPosition = ExercisePosition(
    angleConstraints = listOf(
        AngleConstraint(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST, 165.0, 15.0),
        AngleConstraint(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST, 165.0, 15.0)
    ),
    heightConstraints = listOf(
        HeightConstraint(LandmarkType.LEFT_WRIST, LandmarkType.NOSE, -1f, 0.35f),
        HeightConstraint(LandmarkType.RIGHT_WRIST, LandmarkType.NOSE, -1f, 0.35f),
        HeightConstraint(LandmarkType.LEFT_WRIST, LandmarkType.LEFT_ANKLE, -0.2f, 0.2f),
        HeightConstraint(LandmarkType.RIGHT_WRIST, LandmarkType.RIGHT_ANKLE, -0.2f, 0.2f),
        HeightConstraint(LandmarkType.LEFT_WRIST, LandmarkType.LEFT_HIP, -1f, 0f),
        HeightConstraint(LandmarkType.RIGHT_WRIST, LandmarkType.RIGHT_HIP, -1f, 0f)
    )
)

val endPosition = ExercisePosition(
    angleConstraints = listOf(
        AngleConstraint(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST, 100.0, 25.0),
        AngleConstraint(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST, 100.0, 25.0)
    ),
    heightConstraints = listOf(
        HeightConstraint(LandmarkType.LEFT_WRIST, LandmarkType.NOSE, -0.2f, 0.1f),
        HeightConstraint(LandmarkType.RIGHT_WRIST, LandmarkType.NOSE, -0.2f, 0.1f),
        HeightConstraint(LandmarkType.LEFT_WRIST, LandmarkType.LEFT_HIP, -1f, 0f),
        HeightConstraint(LandmarkType.RIGHT_WRIST, LandmarkType.RIGHT_HIP, -1f, 0f)
    )
)

class PushupExercise : RepExercise(startPosition, endPosition)
