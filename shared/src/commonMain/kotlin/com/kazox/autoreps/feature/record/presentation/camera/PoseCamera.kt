package com.kazox.autoreps.feature.record.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kazox.autoreps.core.domain.pose.Pose

/** One analysed camera frame, handed to the caller on the main thread. */
data class PoseFrame(
    /** The detected pose, or null when nobody was found in this frame. */
    val pose: Pose?,
    /**
     * The frame's capture time in milliseconds, from the camera's own monotonic clock.
     *
     * Not wall-clock time: `System.currentTimeMillis()` can jump backwards (NTP, timezone, the
     * user changing the clock) and MediaPipe rejects timestamps that go back. Frame timestamps
     * are also more accurate, being when the light actually hit the sensor.
     */
    val timestampMillis: Long,
    /** Frame dimensions after rotation, for laying the landmark overlay over the preview. */
    val widthPx: Int,
    val heightPx: Int,
)

/**
 * Live camera preview with pose detection.
 *
 * [onFrame] is called on the main thread for every analysed frame, including ones with no pose —
 * the rep detector needs to know tracking was lost, not merely stop hearing about it.
 */
@Composable
expect fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onFrame: (PoseFrame) -> Unit,
)

/** Camera permission, which each platform asks for its own way. */
interface CameraPermissionController {
    val isGranted: Boolean

    /** Asks the user. Safe to call repeatedly; the platform collapses duplicate prompts. */
    fun request(onResult: (granted: Boolean) -> Unit)
}

@Composable
expect fun rememberCameraPermissionController(): CameraPermissionController
