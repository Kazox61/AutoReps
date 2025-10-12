package com.kazox.autoreps.core

import androidx.compose.runtime.Composable

@Composable
expect fun PoseCameraView(
    showLandmarks: Boolean = true,
    onPoseDetected: (Pose?) -> Unit
)

