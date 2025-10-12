package com.kazox.autoreps.core

import androidx.compose.runtime.Composable

interface Permissions {
    fun hasCameraPermission(): Boolean
    @Composable
    fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit)
}