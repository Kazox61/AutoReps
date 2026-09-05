package com.kazox.autoreps.core.domain.pose

/**
 * MediaPipe's 33 pose landmarks, in the order the model emits them.
 *
 * The ordinal is the wire format: [fromIndex] maps a model index to a type, so the order of
 * these entries must not be changed.
 */
enum class LandmarkType {
    NOSE,
    LEFT_EYE_INNER,
    LEFT_EYE,
    LEFT_EYE_OUTER,
    RIGHT_EYE_INNER,
    RIGHT_EYE,
    RIGHT_EYE_OUTER,
    LEFT_EAR,
    RIGHT_EAR,
    MOUTH_LEFT,
    MOUTH_RIGHT,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_PINKY,
    RIGHT_PINKY,
    LEFT_INDEX,
    RIGHT_INDEX,
    LEFT_THUMB,
    RIGHT_THUMB,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX,
    ;

    companion object {
        private val byIndex = entries.toTypedArray()

        /** Null rather than throwing, so a model returning more points cannot crash a frame. */
        fun fromIndex(index: Int): LandmarkType? = byIndex.getOrNull(index)
    }
}

/** The two sides of the body, so an exercise can be evaluated against whichever one is visible. */
enum class BodySide {
    LEFT,
    RIGHT,
}
