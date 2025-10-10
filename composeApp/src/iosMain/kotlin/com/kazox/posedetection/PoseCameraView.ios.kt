package com.kazox.posedetection

import androidx.compose.runtime.Composable

@Composable
actual fun PoseCameraView(
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
}