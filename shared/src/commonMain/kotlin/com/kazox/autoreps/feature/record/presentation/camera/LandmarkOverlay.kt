package com.kazox.autoreps.feature.record.presentation.camera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose

/** The skeleton drawn over the preview: torso and limbs, no face. */
private val BONES =
    listOf(
        LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_ELBOW,
        LandmarkType.LEFT_ELBOW to LandmarkType.LEFT_WRIST,
        LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_ELBOW,
        LandmarkType.RIGHT_ELBOW to LandmarkType.RIGHT_WRIST,
        LandmarkType.LEFT_SHOULDER to LandmarkType.RIGHT_SHOULDER,
        LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_HIP,
        LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_HIP,
        LandmarkType.LEFT_HIP to LandmarkType.RIGHT_HIP,
        LandmarkType.LEFT_HIP to LandmarkType.LEFT_KNEE,
        LandmarkType.LEFT_KNEE to LandmarkType.LEFT_ANKLE,
        LandmarkType.RIGHT_HIP to LandmarkType.RIGHT_KNEE,
        LandmarkType.RIGHT_KNEE to LandmarkType.RIGHT_ANKLE,
    )

private val JOINTS = BONES.flatMap { listOf(it.first, it.second) }.toSet()

/** Alpha for landmarks whose position the model is guessing rather than seeing. */
private const val DIMMED_ALPHA = 0.35f

/**
 * Draws [pose] over a preview that fills its box with `resizeAspectFill` semantics.
 *
 * Landmark coordinates are normalized to the *frame*, which is cropped when its aspect ratio
 * differs from the box — so the drawing maps into the visible region rather than the box, or the
 * skeleton drifts off the body at the edges.
 *
 * Everything the model predicts is drawn, dimmed when it is a guess — matching what the
 * detector consumes (height constraints run on predicted positions), and hiding the joints
 * instead makes the skeleton look broken. During a push-up the far arm and the feet are guesses
 * most of the time.
 *
 * @param minVisibility landmarks below this confidence are drawn dimmed rather than at full
 *   strength.
 */
@Composable
fun LandmarkOverlay(
    pose: Pose?,
    frameWidth: Int,
    frameHeight: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF3B82F6),
    minVisibility: Float = 0.5f,
) {
    if (pose == null || frameWidth <= 0 || frameHeight <= 0) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val frameAspect = frameWidth.toFloat() / frameHeight
        val boxAspect = size.width / size.height

        // resizeAspectFill: the frame is scaled to cover the box, so one axis overflows.
        val scale = if (frameAspect > boxAspect) size.height / frameHeight else size.width / frameWidth
        val drawnWidth = frameWidth * scale
        val drawnHeight = frameHeight * scale
        val offsetX = (size.width - drawnWidth) / 2f
        val offsetY = (size.height - drawnHeight) / 2f

        fun px(landmark: Pose.Landmark) =
            Offset(offsetX + landmark.x * drawnWidth, offsetY + landmark.y * drawnHeight)

        fun alpha(visibility: Float): Float = if (visibility >= minVisibility) 1f else DIMMED_ALPHA

        BONES.forEach { (from, to) ->
            val a = pose[from] ?: return@forEach
            val b = pose[to] ?: return@forEach
            // A bone is as trustworthy as its less trustworthy end.
            val alpha = minOf(alpha(a.visibility), alpha(b.visibility))
            drawLine(color = color.copy(alpha = alpha), start = px(a), end = px(b), strokeWidth = 6.dp.toPx() / 2)
        }

        JOINTS.forEach { type ->
            val landmark = pose[type] ?: return@forEach
            val alpha = alpha(landmark.visibility)
            val center = px(landmark)
            drawCircle(color = color.copy(alpha = alpha), radius = 4.dp.toPx(), center = center)
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 7.dp.toPx(),
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}
