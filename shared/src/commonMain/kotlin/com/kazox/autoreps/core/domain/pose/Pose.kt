package com.kazox.autoreps.core.domain.pose

/**
 * One frame of pose estimation.
 *
 * Two coordinate systems arrive together and they are not interchangeable:
 *
 * - [Landmark.x] / [Landmark.y] are **normalized display space**: origin top-left, `0..1` across
 *   the frame, y growing *downward*, already un-rotated and mirrored by the platform camera layer
 *   so both Android and iOS deliver the same frame. Use these for anything about screen position
 *   or relative height. They depend on how the phone is held.
 * - [Landmark.wx] / [Landmark.wy] / [Landmark.wz] are **world space**: metres, origin roughly at
 *   the hip centre, independent of framing. Use these for angles — an elbow angle must not change
 *   because the camera moved.
 *
 * Mixing them silently produces plausible-looking nonsense, which is why they are named apart.
 */
data class Pose(
    val landmarks: List<Landmark>,
) {
    /**
     * Built once per frame. The previous implementation scanned the list for every lookup, which
     * at ~10 constraints x 3 lookups x 33 landmarks ran tens of thousands of comparisons a second.
     */
    private val byType: Map<LandmarkType, Landmark> = landmarks.associateBy { it.type }

    data class Landmark(
        val type: LandmarkType,
        /** Normalized display space, `0..1`, y grows downward. See [Pose]. */
        val x: Float,
        val y: Float,
        /** World space in metres, origin near the hip centre. See [Pose]. */
        val wx: Float,
        val wy: Float,
        val wz: Float,
        /**
         * MediaPipe's confidence that this landmark is actually visible, `0..1`.
         *
         * The model always returns all 33 points; occluded ones are guesses with a low value
         * here. During a push-up the far arm is occluded most of the time, so constraints that
         * ignore this are frequently measuring an invented limb.
         */
        val visibility: Float = 1f,
    )

    operator fun get(type: LandmarkType): Landmark? = byType[type]

    /** The landmark, or null when it is missing or too likely to be a guess. */
    fun visibleLandmark(
        type: LandmarkType,
        minVisibility: Float,
    ): Landmark? = byType[type]?.takeIf { it.visibility >= minVisibility }
}
