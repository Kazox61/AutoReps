package com.kazox.posedetection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PoseCameraView(
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val factory = LocalNativeViewFactory.current
    UIKitView(
        factory = {
            factory.createPoseCameraView(showLandmarks, onPoseDetected)
        },
        modifier = Modifier.fillMaxSize()
    )
}