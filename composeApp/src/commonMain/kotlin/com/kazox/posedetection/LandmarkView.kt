package com.kazox.posedetection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun LandmarkView(pose: Pose?, frameWidth: Int, frameHeight: Int, isFrontCamera: Boolean = true) {
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

            fun transformX(x: Float) = if (isFrontCamera) maxCanvasX - x / frameWidth * lengthX else minCanvasX + x / frameWidth * lengthX
            fun transformY(y: Float) = minCanvasY + y / frameHeight * lengthY

            pose.landmarks.forEach { landmark ->
                drawCircle(
                    color = Color.Blue,
                    radius = 8f,
                    center = Offset(transformX(landmark.x), transformY(landmark.y)),
                )
                drawCircle(
                    color = Color.White,
                    radius = 20f,
                    center = Offset(transformX(landmark.x), transformY(landmark.y)),
                    style = Stroke(
                        width = 4f
                    )
                )
            }

            fun line(a: Int, b: Int) {
                val l1 = pose.landmarks.find { it.type == a }
                val l2 = pose.landmarks.find { it.type == b }
                if (l1 != null && l2 != null) {
                    drawLine(
                        color = Color.Blue,
                        start = Offset(transformX(l1.x), transformY(l1.y)),
                        end = Offset(transformX(l2.x), transformY(l2.y)),
                        strokeWidth = 6f
                    )
                }
            }

            line(11, 13) // left shoulder → left elbow
            line(13, 15) // left elbow → left wrist
            line(12, 14) // right shoulder → right elbow
            line(14, 16) // right elbow → right wrist
            line(11, 12) // shoulders
            line(23, 24) // hips
            line(11, 23) // left torso side
            line(12, 24) // right torso side
        }
    }
}
