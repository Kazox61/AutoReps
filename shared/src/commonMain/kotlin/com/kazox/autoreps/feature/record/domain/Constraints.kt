package com.kazox.autoreps.feature.record.domain

import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * A test a single frame either passes or fails.
 *
 * Every implementation **fails closed**: a missing landmark, or one the model is not confident
 * enough about at this constraint's [minVisibility], returns false. Reporting "constraint
 * satisfied" from data that does not exist is how a rep gets counted for a body that left the
 * frame.
 */
sealed interface PoseConstraint {
    /**
     * Every landmark this constraint reads.
     *
     * Lets callers ask "could this even be evaluated?" separately from "does it hold". Without
     * that split, a body the camera can only half-see is indistinguishable from a body in the
     * wrong position — and the difference is the whole message to the user.
     */
    val landmarks: Set<LandmarkType>

    /**
     * How visible a landmark must be before this constraint trusts its position.
     *
     * Per constraint, not global, because strictness is a property of the *measurement*: an
     * angle computed from a joint the model invented is garbage, while a coarse level check
     * survives a guessed landmark easily. The one bar for everything was why push-ups with feet
     * occluded behind the torso stopped counting — the model's predicted ankles are good enough
     * to say "feet level with hands", but never cleared a 0.5 visibility gate.
     */
    val minVisibility: Float

    fun isSatisfied(pose: Pose): Boolean

    /** All of [landmarks] present and confident enough to use. */
    fun isMeasurable(pose: Pose): Boolean = landmarks.all { pose.visibleLandmark(it, minVisibility) != null }

    /** A human-readable measurement, for tuning thresholds against a real body. */
    fun describe(pose: Pose): ConstraintReading
}

/** One constraint's live measurement. */
data class ConstraintReading(
    val label: String,
    /** The measured value, or "—" when the landmarks are not visible. */
    val value: String,
    val satisfied: Boolean,
    val measurable: Boolean,
)

/** "ELBOW" from LEFT_ELBOW or RIGHT_ELBOW — a name that fits one diagnostics line. */
private fun LandmarkType.shortName(): String = name.removePrefix("LEFT_").removePrefix("RIGHT_")

/**
 * The angle at [mid], between the limbs running out to [first] and [last], must sit within
 * [tolerance] degrees of [targetAngle].
 *
 * Measured in **world space**, so it means the same thing regardless of where the camera is or
 * how the phone is held — an elbow bent to 90 degrees reads 90 from any angle.
 *
 * Strict by default: an angle whose joint the model invented from nothing is precisely the
 * garbage the visibility gate exists to keep out.
 */
class AngleConstraint(
    private val first: LandmarkType,
    private val mid: LandmarkType,
    private val last: LandmarkType,
    private val targetAngle: Double,
    private val tolerance: Double,
    override val minVisibility: Float = 0.5f,
) : PoseConstraint {
    override val landmarks: Set<LandmarkType> = setOf(first, mid, last)

    override fun isSatisfied(pose: Pose): Boolean =
        measure(pose)?.let { it in (targetAngle - tolerance)..(targetAngle + tolerance) } == true

    override fun describe(pose: Pose): ConstraintReading {
        val angle = measure(pose)
        return ConstraintReading(
            label = "${mid.shortName()} ${targetAngle.toInt()}°±${tolerance.toInt()}",
            value = angle?.let { "${it.toInt()}°" } ?: "—",
            satisfied = isSatisfied(pose),
            measurable = angle != null,
        )
    }

    private fun measure(pose: Pose): Double? {
        val a = pose.visibleLandmark(first, minVisibility) ?: return null
        val b = pose.visibleLandmark(mid, minVisibility) ?: return null
        val c = pose.visibleLandmark(last, minVisibility) ?: return null
        return angleAt(a, b, c)
    }

    /** Null when either limb has zero length, which would make the angle meaningless. */
    private fun angleAt(
        a: Pose.Landmark,
        b: Pose.Landmark,
        c: Pose.Landmark,
    ): Double? {
        val abx = a.wx - b.wx
        val aby = a.wy - b.wy
        val abz = a.wz - b.wz
        val cbx = c.wx - b.wx
        val cby = c.wy - b.wy
        val cbz = c.wz - b.wz

        val magAb = sqrt(abx * abx + aby * aby + abz * abz)
        val magCb = sqrt(cbx * cbx + cby * cby + cbz * cbz)
        // Returning 0.0 here, as the previous version did, silently satisfied any constraint
        // targeting a small angle.
        if (magAb == 0f || magCb == 0f) return null

        val dot = abx * cbx + aby * cby + abz * cbz
        val cosTheta = (dot / (magAb * magCb)).coerceIn(-1f, 1f)
        return acos(cosTheta.toDouble()) * (180.0 / PI)
    }
}

/**
 * The vertical gap between two landmarks, as a fraction of frame height, must fall in
 * `[minDelta, maxDelta]`.
 *
 * Measured in **display space** where y grows downward, so a positive delta means [last] sits
 * *below* [first] on screen. Unlike [AngleConstraint] this depends on framing — it answers
 * "is the chest near the floor", which is a question about the picture, not the body.
 *
 * Permissive by default (existence is the bar): a band this wide tolerates a landmark the model
 * merely predicted, and a predicted position is anchored on the neighbours it can see. This is
 * what let the old version count push-ups whose feet sat behind the torso — the guessed ankle
 * is good enough to say "feet level with hands", which is all this constraint ever asks.
 */
class HeightConstraint(
    private val first: LandmarkType,
    private val last: LandmarkType,
    private val minDelta: Float,
    private val maxDelta: Float,
    override val minVisibility: Float = 0f,
) : PoseConstraint {
    override val landmarks: Set<LandmarkType> = setOf(first, last)

    override fun isSatisfied(pose: Pose): Boolean = measure(pose)?.let { it in minDelta..maxDelta } == true

    override fun describe(pose: Pose): ConstraintReading {
        val delta = measure(pose)
        return ConstraintReading(
            label = "${first.shortName().take(5)}→${last.shortName().take(5)} $minDelta..$maxDelta",
            value = delta?.let { formatDelta(it) } ?: "—",
            satisfied = isSatisfied(pose),
            measurable = delta != null,
        )
    }

    private fun measure(pose: Pose): Float? {
        val a = pose.visibleLandmark(first, minVisibility) ?: return null
        val b = pose.visibleLandmark(last, minVisibility) ?: return null
        return b.y - a.y
    }

    private fun formatDelta(value: Float): String {
        val hundredths = kotlin.math.round(value * 100).toInt()
        val sign = if (hundredths < 0) "-" else ""
        val abs = kotlin.math.abs(hundredths)
        return "$sign${abs / 100}.${(abs % 100).toString().padStart(2, '0')}"
    }
}
