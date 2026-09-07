package com.kazox.autoreps.feature.record.presentation.camera

import com.kazox.autoreps.core.domain.pose.Pose
import kotlin.math.PI
import kotlin.math.abs

/**
 * Smooths landmark positions for drawing — never for anything that measures a rep.
 *
 * Raw MediaPipe output jitters a few pixels per frame even with a perfectly still body, which
 * reads as low tracking quality on screen. A One-Euro filter — a low-pass whose cutoff rises
 * with speed — removes that jitter while adding almost no lag to real movement: a still pose
 * gets heavy smoothing, a fast one passes through nearly untouched.
 *
 * The filtered poses must not reach the rep detector: smoothing both distorts the depth of an
 * angle minimum and delays it, which shifts rep boundaries. It exists purely for the overlay.
 *
 * Not thread-safe; called from whatever single thread delivers frames on each platform.
 */
class PoseSmoother(
    /** Baseline low-pass cutoff in Hz. Lower is smoother at rest and laggier. */
    private val minCutoffHz: Double = 1.2,
    /** How aggressively the cutoff rises with speed. Higher is less lag in fast motion. */
    private val beta: Double = 0.6,
    /** Cutoff of the speed estimate itself, in Hz. */
    private val derivativeCutoffHz: Double = 1.0,
) {
    private class Axis {
        var value = 0.0
        var speed = 0.0
        var primed = false
    }

    /** Two axes (x, y) per landmark, indexed by position in [Pose.landmarks]. */
    private var axes: Array<Axis> = emptyArray()
    private var lastTimestampMs: Long? = null

    /**
     * Returns [pose] with landmark x/y filtered. A null pose (tracking lost) resets the filter,
     * so the next detection starts from its own position instead of being dragged in from the
     * previous person's.
     */
    fun smooth(pose: Pose?, timestampMillis: Long): Pose? {
        if (pose == null) {
            reset()
            return null
        }

        val last = lastTimestampMs
        if (last == null) {
            // First frame after a (re)start: adopt the pose wholesale rather than filtering
            // towards it from nothing.
            axes = Array(pose.landmarks.size * 2) { Axis() }
            lastTimestampMs = timestampMillis
            return pose
        }

        val dtSec = (timestampMillis - last) / 1000.0
        if (dtSec <= 0.0) {
            // MediaPipe timestamps are monotonic; this only guards a pathological caller.
            return pose
        }
        if (axes.size != pose.landmarks.size * 2) {
            // A differently-shaped pose is a different stream — start over rather than mix.
            axes = Array(pose.landmarks.size * 2) { Axis() }
        }
        lastTimestampMs = timestampMillis

        val landmarks =
            pose.landmarks.mapIndexed { index, landmark ->
                landmark.copy(
                    x = filter(axes[index * 2], landmark.x.toDouble(), dtSec).toFloat(),
                    y = filter(axes[index * 2 + 1], landmark.y.toDouble(), dtSec).toFloat(),
                )
            }
        return Pose(landmarks)
    }

    private fun reset() {
        axes = emptyArray()
        lastTimestampMs = null
    }

    /** One One-Euro step over a single coordinate. */
    private fun filter(
        axis: Axis,
        x: Double,
        dtSec: Double,
    ): Double {
        if (!axis.primed) {
            axis.value = x
            axis.speed = 0.0
            axis.primed = true
            return x
        }

        val rawSpeed = (x - axis.value) / dtSec
        val speed = lowPass(rawSpeed, axis.speed, alpha(derivativeCutoffHz, dtSec))
        val cutoffHz = minCutoffHz + beta * abs(speed)
        val smoothed = lowPass(x, axis.value, alpha(cutoffHz, dtSec))
        axis.value = smoothed
        axis.speed = speed
        return smoothed
    }

    private fun lowPass(
        x: Double,
        previous: Double,
        alpha: Double,
    ): Double = alpha * x + (1.0 - alpha) * previous

    private fun alpha(
        cutoffHz: Double,
        dtSec: Double,
    ): Double = 1.0 / (1.0 + 1.0 / (2.0 * PI * cutoffHz * dtSec))
}
