package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose

/**
 * One recognisable body position, expressed as alternative ways of recognising it.
 *
 * A [variant] is a complete description using one side of the body. The position matches when
 * **any** variant does, which is the point: push-ups are filmed side-on, so one arm is occluded
 * almost the whole time. Requiring both arms — as the earlier version did by putting left and
 * right constraints in a single list — means every frame is judged partly on a limb the model
 * cannot see, and with visibility gating on it would simply never match.
 */
class ExercisePosition(
    private val variants: List<List<PoseConstraint>>,
) {
    init {
        require(variants.isNotEmpty()) { "An ExercisePosition needs at least one variant" }
        require(variants.all { it.isNotEmpty() }) { "A variant needs at least one constraint" }
    }

    fun matches(pose: Pose): Boolean = variants.any { variant -> variant.all { it.isSatisfied(pose) } }

    /**
     * True when at least one variant could be evaluated at all — every landmark it reads is
     * visible.
     *
     * "I cannot see your feet" and "your body is in the wrong shape" are different problems with
     * different fixes, and only the first one is solved by moving the phone.
     */
    fun isMeasurable(pose: Pose): Boolean = variants.any { variant -> variant.all { it.isMeasurable(pose) } }

    /**
     * Live readings from the most complete variant, for tuning against a real body.
     *
     * Picks the variant with the most measurable constraints, so the side the camera can
     * actually see is the one reported.
     */
    fun diagnose(pose: Pose): List<ConstraintReading> =
        variants
            .maxByOrNull { variant -> variant.count { it.isMeasurable(pose) } }
            .orEmpty()
            .map { it.describe(pose) }

    /** Every landmark any variant needs — what has to be in frame for this to work at all. */
    val landmarks: Set<LandmarkType> = variants.flatten().flatMap { it.landmarks }.toSet()
}
