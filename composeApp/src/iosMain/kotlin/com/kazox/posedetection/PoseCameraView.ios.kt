package com.kazox.posedetection

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

    val pose = remember { mutableStateOf<Pose?>(null) }

    UIKitView(
        factory = {
            factory.createPoseCameraView(showLandmarks, {
                pose.value = it
                onPoseDetected(it)
                it?.let {
                    it.landmarks.forEach { x ->
                        println("${x.wx}, ${x.wy}, ${x.wz}")
                    }
                }
            } )
        },
        modifier = Modifier.fillMaxSize()
    )

    LandmarkView(pose.value, 2000, 1600)
}