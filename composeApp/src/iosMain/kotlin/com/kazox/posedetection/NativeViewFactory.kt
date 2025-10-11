package com.kazox.posedetection

import platform.UIKit.UIView

interface NativeViewFactory {
    fun createPoseCameraView(
        showLandmarks: Boolean,
        onPoseDetected: (Pose?, Int, Int) -> Unit
    ): UIView
}