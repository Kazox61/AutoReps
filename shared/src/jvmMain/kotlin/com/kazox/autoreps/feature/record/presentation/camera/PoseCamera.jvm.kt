package com.kazox.autoreps.feature.record.presentation.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.foundation.KazTheme

/**
 * Desktop has no pose detection: MediaPipe ships mobile-only for this task, and the desktop app
 * exists to review UI, not to count push-ups.
 *
 * A visible placeholder rather than an empty Box — silently rendering nothing would look like a
 * broken camera instead of an unsupported platform.
 */
@Composable
actual fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onFrame: (PoseFrame) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Aufzeichnung ist nur auf Android und iOS verfügbar.",
            variant = TextVariant.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(KazTheme.spacing.lg),
        )
    }
}

@Composable
actual fun rememberCameraPermissionController(): CameraPermissionController =
    object : CameraPermissionController {
        override val isGranted: Boolean = false

        override fun request(onResult: (Boolean) -> Unit) = onResult(false)
    }
