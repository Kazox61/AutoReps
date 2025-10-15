package com.kazox.autoreps.feature.record.presentation.countReps

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.finish
import autoreps.composeapp.generated.resources.start
import com.kazox.autoreps.core.PoseCameraView
import com.kazox.autoreps.core.providePermissions
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.app.navigation.AddWorkoutKey
import com.kazox.autoreps.app.navigation.TopLevelBackStack
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun CountRepsScreen(
    topLevelBackStack: TopLevelBackStack<Any>,
    viewModel: CountRepsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                PoseCameraView(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-32).dp, y = 32.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .width(120.dp),
                    showLandmarks = false
                ) { pose ->
                    pose?.let {
                        if (state.startedDateTime != null) {
                            viewModel.onEvent(CountRepsEvent.PoseDetected(pose))
                        }
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = state.formattedTime,
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
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.repCount.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 120.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    if (state.startedDateTime == null) {
                        Button(
                            onClick = {
                                viewModel.onEvent(CountRepsEvent.StartWorkout)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.start),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    else {
                        Button(
                            onClick = {
                                topLevelBackStack.add(
                                    AddWorkoutKey(
                                        Workout(
                                            reps = state.repCount,
                                            startedAt = (state.startedDateTime
                                                ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
                                                .toString(),
                                            duration = state.duration
                                        ),
                                        state.reps
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(2.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.finish),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}