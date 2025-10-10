package com.kazox.posedetection

import platform.UIKit.UIView

interface NativeViewFactory {
    fun createPoseCameraView(
        showLandmarks: Boolean,
        onPoseDetected: (Pose?) -> Unit
    ): UIView
}