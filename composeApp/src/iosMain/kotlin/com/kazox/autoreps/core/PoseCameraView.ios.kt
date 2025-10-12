package com.kazox.autoreps.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.kazox.autoreps.LocalNativeViewFactory
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PoseCameraView(
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val factory = LocalNativeViewFactory.current

    val pose = remember { mutableStateOf<Pose?>(null) }
    val imageWidth = remember { mutableIntStateOf(0) }
    val imageHeight = remember { mutableIntStateOf(0) }

    UIKitView(
        factory = {
            factory.createPoseCameraView(showLandmarks, { detectedPose, width, height ->
                pose.value = detectedPose
                imageWidth.value = width
                imageHeight.value = height
                onPoseDetected(detectedPose)
            } )
        },
        modifier = Modifier.fillMaxSize()
    )

    if (showLandmarks) {
        LandmarkOverlay(pose.value, imageWidth.value, imageHeight.value)
    }
}