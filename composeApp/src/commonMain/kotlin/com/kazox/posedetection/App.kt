package com.kazox.posedetection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Scaffold(
            modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars)
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
                Box(modifier = Modifier.fillMaxSize()) {
                    val repCount = remember { mutableStateOf(0) }
                    val pushupExercise = remember { PushupExercise() }

                    remember {
                        pushupExercise.onRep {
                            repCount.value += 1
                        }
                    }

                    PoseCameraView { pose ->
                        pose?.let {
                            pushupExercise.process(it)
                        }
                    }

                    Text(
                        text = "Pushups: ${repCount.value}",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.systemBars)
                    )
                }
            }
        }
    }
}
