package com.kazox.autoreps.core.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
fun PushupAnimation(
    modifier: Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val anim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    Canvas(modifier = modifier
        .height(130.dp)
    ) {
        val centerX = size.width / 2
        val groundY = size.height - 40f

        val bodySize = 40f;
        val groundStartY = groundY - bodySize / 2
        val feet = Offset(centerX - 200f, groundStartY)
        val hip = Offset(feet.x + 150f, feet.y - 80f)
        val shoulder = Offset(hip.x + 150f, hip.y - 30f)
        val elbow = Offset(shoulder.x - 10f, shoulder.y + (feet.y - shoulder.y) / 2)
        val hand = Offset(shoulder.x, groundStartY)
        val head = Offset(shoulder.x + 70f, shoulder.y)

        drawRect(
            color = surfaceColor,
            topLeft = Offset(0f, groundY),
            size = Size(size.width, 40f)
        )

        drawRect(
            color = surfaceColor,
            topLeft = Offset(size.width - 40f, 0f),
            size = Size(40f, size.height)
        )

        drawLine(
            color = onBackgroundColor,
            start = feet,
            end = hip.copy(y = hip.y + (groundStartY - hip.y) / 2 * anim),
            strokeWidth = bodySize,
            cap = StrokeCap.Round
        )

        drawLine(
            color = onBackgroundColor,
            start = hip.copy(y = hip.y + (groundStartY - hip.y) / 2 * anim),
            end = shoulder.copy(y = shoulder.y + (groundStartY - shoulder.y) / 2 * anim),
            strokeWidth = bodySize,
            cap = StrokeCap.Round
        )

        drawLine(
            color = onBackgroundColor,
            start = shoulder.copy(y = shoulder.y + (groundStartY - shoulder.y) / 2 * anim),
            end = elbow.copy(x = elbow.x - 30 * anim, y = elbow.y + (groundStartY - elbow.y) / 2 * anim),
            strokeWidth = bodySize,
            cap = StrokeCap.Round
        )

        drawLine(
            color = onBackgroundColor,
            start = elbow.copy(x = elbow.x - 30 * anim, y = elbow.y + (groundStartY - elbow.y) / 2 * anim),
            end = hand,
            strokeWidth = bodySize,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = onBackgroundColor,
            center = head.copy(y = head.y + (groundStartY - head.y) / 2 * anim),
            radius = bodySize
        )

        drawLine(
            color = primaryColor,
            start = Offset(size.width - 70f, groundY - 10f),
            end = Offset(size.width - 50f, groundY - 80f),
            strokeWidth = 20f,
            cap = StrokeCap.Round
        )

        drawCircle(
            color = primaryColor,
            center = Offset(size.width - 50f, groundY - 65f),
            radius = 10f
        )

        val path = Path().apply {
            // Define triangle points
            val p1 = Offset(size.width - 60f, groundY - 70f)
            val p2 = Offset(size.width - 170f, groundY - 130f)
            val p3 = Offset(size.width - 170f, groundY - 40f)

            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p3.x, p3.y)
            close()
        }

        val brush = Brush.linearGradient(
            colors = listOf(primaryColor, primaryColor.copy(alpha = 0.1f)),
            start = Offset(size.width - 60f, groundY - 70f),
            end = Offset(size.width - 170f, groundY - 70f)
        )

        drawPath(
            brush = brush,
            path = path,
            style = Fill,
        )
    }
}