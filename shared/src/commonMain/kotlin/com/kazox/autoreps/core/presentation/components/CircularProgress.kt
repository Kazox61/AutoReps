package com.kazox.autoreps.core.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kazox.ui.components.text.Text
import com.kazox.ui.foundation.KazTheme


@Composable
fun CircularProgress(
    value: Int,
    maxValue: Int,
    radius: Dp = 60.dp,
    strokeWidth: Dp = 10.dp,
    animDuration: Int = 500,
    animDelay: Int = 0
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }

    val percentage = value.toFloat() / maxValue.toFloat()

    val animatedPercentage = animateFloatAsState(
        targetValue = if (animationPlayed) percentage else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        )
    )

    val animatedValue = animateIntAsState(
        targetValue = if (animationPlayed) value else 0,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        )
    )

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(radius * 2f)
    ) {
        val surfaceColor = KazTheme.colors.surface
        val successColor = KazTheme.colors.success
        val primaryColor = KazTheme.colors.primary

        Canvas(modifier = Modifier.size(radius * 2f)) {
            val half = strokeWidth.toPx() / 2f
            val arcSize = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx())
            drawArc(
                color = surfaceColor,
                0f,
                360f,
                useCenter = false,
                topLeft = Offset(half, half),
                size = arcSize,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = if (value >= maxValue) successColor else primaryColor,
                -90f,
                360 * animatedPercentage.value,
                useCenter = false,
                topLeft = Offset(half, half),
                size = arcSize,
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = animatedValue.value.toString(),
                style = KazTheme.typography.large,
                color = KazTheme.colors.onSurface
            )
            Text(
                text = "of $maxValue",
                style = KazTheme.typography.p,
                color = KazTheme.colors.onSurface
            )
        }
    }
}