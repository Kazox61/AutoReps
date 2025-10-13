package com.kazox.autoreps.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.kazox.autoreps.LocalNativeViewFactory
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.compareTo
import kotlin.text.toFloat

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val factory = LocalNativeViewFactory.current

    var pose by remember { mutableStateOf<Pose?>(null) }
    var imageWidth by remember { mutableIntStateOf(0) }
    var imageHeight by remember { mutableIntStateOf(0) }

    val aspectRatio by remember(imageWidth, imageHeight) {
        derivedStateOf {
            if (imageWidth > 0 && imageHeight > 0) {
                imageWidth.toFloat() / imageHeight.toFloat()
            } else {
                3f / 4f
            }
        }
    }

    Box(
        modifier = modifier.then(Modifier.aspectRatio(aspectRatio))
    ) {
        UIKitView(
            factory = {
                factory.createPoseCameraView(showLandmarks, { detectedPose, width, height ->
                    pose = detectedPose
                    imageWidth = width
                    imageHeight = height
                    onPoseDetected(detectedPose)
                } )
            },
            modifier = Modifier.fillMaxSize()
        )

        if (showLandmarks) {
            LandmarkOverlay(pose, imageWidth, imageHeight)
        }
    }
}