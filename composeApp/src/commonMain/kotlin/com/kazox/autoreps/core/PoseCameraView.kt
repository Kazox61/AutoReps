package com.kazox.autoreps.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean = true,
    onPoseDetected: (Pose?) -> Unit
)

