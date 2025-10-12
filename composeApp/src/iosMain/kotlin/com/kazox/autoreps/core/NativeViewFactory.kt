package com.kazox.autoreps.core

import platform.UIKit.UIView

interface NativeViewFactory {
    fun createPoseCameraView(
        showLandmarks: Boolean,
        onPoseDetected: (Pose?, Int, Int) -> Unit
    ): UIView
}