package com.kazox.autoreps

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kazox.autoreps.core.PoseCameraView
import com.kazox.autoreps.core.PushupExercise
import com.kazox.autoreps.core.providePermissions
import kotlinx.coroutines.delay

@Composable
fun CounterScreen() {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box (
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val permissions = providePermissions()
            val cameraPermissionState = remember { mutableStateOf(permissions.hasCameraPermission()) }

            if (!cameraPermissionState.value) {
                permissions.RequestCameraPermission(
                    onGranted = { cameraPermissionState.value = true },
                    onDenied = { println("Camera Permission Denied") }
                )
            }

            if (cameraPermissionState.value) {
                var timer by remember { mutableStateOf(0) }
                var started by remember { mutableStateOf(false) }


                var repCount by remember { mutableStateOf(0) }
                val pushupExercise = remember { PushupExercise() }

                remember {
                    pushupExercise.onRep {
                        if (!started) started = true
                        repCount += 1
                    }
                }

                LaunchedEffect(started) {
                    if (started) {
                        while (true) {
                            delay(1000)
                            timer += 1
                        }
                    }
                }

                val infiniteTransition = rememberInfiniteTransition(label = "breathing")
                val breathingScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutSine),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "breathing"
                )

                val circleColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.primary,
                    animationSpec = tween(300),
                    label = "circleColor"
                )

                val onCircleColor by animateColorAsState(
                    targetValue = MaterialTheme.colorScheme.onPrimary,
                    animationSpec = tween(300),
                    label = "onCircleColor"
                )

                val formattedTime by remember(timer) {
                    derivedStateOf {
                        val minutes = timer / 60
                        val seconds = timer % 60
                        "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                    }
                }

                PoseCameraView(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-32).dp, y = 32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .width(120.dp),
                    showLandmarks = false
                ) { pose ->
                    pose?.let {
                        pushupExercise.process(it)
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Text(
                        text = formattedTime,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 60.sp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .aspectRatio(1f)
                            .scale(breathingScale)
                            .clip(CircleShape)
                            .background(circleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = repCount.toString(),
                            color = onCircleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 120.sp
                        )
                    }
                }
            }
        }
    }
}