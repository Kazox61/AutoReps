package com.kazox.posedetection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

object PoseConnections {
    val bones = listOf(
        LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_ELBOW,
        LandmarkType.LEFT_ELBOW to LandmarkType.LEFT_WRIST,
        LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_ELBOW,
        LandmarkType.RIGHT_ELBOW to LandmarkType.RIGHT_WRIST,
        LandmarkType.LEFT_SHOULDER to LandmarkType.RIGHT_SHOULDER,
        LandmarkType.LEFT_SHOULDER to LandmarkType.LEFT_HIP,
        LandmarkType.RIGHT_SHOULDER to LandmarkType.RIGHT_HIP,
        LandmarkType.LEFT_HIP to LandmarkType.RIGHT_HIP,
        LandmarkType.LEFT_HIP to LandmarkType.LEFT_ANKLE,
        LandmarkType.RIGHT_HIP to LandmarkType.RIGHT_ANKLE
    )
}

@Composable
fun LandmarkOverlay(
    pose: Pose?,
    frameWidth: Int,
    frameHeight: Int,
    isFrontCamera: Boolean = true
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        pose?.let {
            val frameAspectRatio = frameWidth.toFloat() / frameHeight.toFloat()
            val canvasAspectRatio = size.width / size.height

            val minCanvasX = if (canvasAspectRatio < frameAspectRatio) {
                (size.width - size.height * frameAspectRatio) / 2f
            } else 0f
            val maxCanvasX = size.width - minCanvasX
            val minCanvasY = if (canvasAspectRatio > frameAspectRatio) {
                (size.height - size.width / frameAspectRatio) / 2f
            } else 0f
            val maxCanvasY = size.height - minCanvasY
            val lengthX = maxCanvasX - minCanvasX
            val lengthY = maxCanvasY - minCanvasY

            fun transformX(x: Float) =
                if (isFrontCamera) maxCanvasX - x * lengthX else minCanvasX + x * lengthX

            fun transformY(y: Float) = minCanvasY + y * lengthY

            PoseConnections.bones.forEach { (startType, endType) ->
                val start = pose.landmarks.find { it.type == startType }
                val end = pose.landmarks.find { it.type == endType }
                if (start != null && end != null) {
                    drawLine(
                        color = Color.Blue,
                        start = Offset(transformX(start.x), transformY(start.y)),
                        end = Offset(transformX(end.x), transformY(end.y)),
                        strokeWidth = 6f
                    )
                }
            }

            val connectedLandmarks = PoseConnections.bones
                .flatMap { listOf(it.first, it.second) }
                .toSet()

            pose.landmarks.filter { it.type in connectedLandmarks }.forEach { landmark ->
                val cx = transformX(landmark.x)
                val cy = transformY(landmark.y)

                drawCircle(color = Color.Blue, radius = 8f, center = Offset(cx, cy))
                drawCircle(
                    color = Color.White,
                    radius = 20f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 4f)
                )
            }

        }
    }
}
