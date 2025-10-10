package com.kazox.posedetection

import androidx.compose.runtime.Composable

interface Permissions {
    fun hasCameraPermission(): Boolean
    @Composable
    fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit)
}