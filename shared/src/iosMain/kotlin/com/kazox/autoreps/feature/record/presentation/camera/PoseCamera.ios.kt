package com.kazox.autoreps.feature.record.presentation.camera

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.kazox.autoreps.core.domain.pose.Pose
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Bridge implemented in Swift, where MediaPipe lives.
 *
 * MediaPipe has no Kotlin/Native bindings, so the whole capture-and-detect pipeline stays on the
 * Swift side and hands finished poses back through here. Set by the iOS app at launch.
 */
interface PoseCameraFactory {
    /**
     * @param onFrame invoked on the main thread per analysed frame, with a null pose when
     *   nothing was detected. Timestamps come from the sample buffer's presentation time.
     */
    fun createView(
        showLandmarks: Boolean,
        onFrame: (pose: Pose?, timestampMillis: Long, widthPx: Int, heightPx: Int) -> Unit,
    ): UIView
}

/** Populated by the iOS app before Compose starts. */
object PoseCameraBridge {
    var factory: PoseCameraFactory? = null
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onFrame: (PoseFrame) -> Unit,
) {
    val factory = PoseCameraBridge.factory
    val currentOnFrame by rememberUpdatedState(onFrame)

    var overlayPose by remember { mutableStateOf<Pose?>(null) }
    var frameWidth by remember { mutableStateOf(0) }
    var frameHeight by remember { mutableStateOf(0) }
    val smoother = remember { PoseSmoother() }

    if (factory == null) return

    Box(modifier = modifier) {
        UIKitView(
            factory = {
                factory.createView(showLandmarks) { pose, timestampMillis, widthPx, heightPx ->
                    // Only the drawn skeleton is smoothed; onFrame gets the raw pose, since the
                    // rep detector's timing must not be shifted by a filter.
                    overlayPose = smoother.smooth(pose, timestampMillis)
                    frameWidth = widthPx
                    frameHeight = heightPx
                    currentOnFrame(
                        PoseFrame(
                            pose = pose,
                            timestampMillis = timestampMillis,
                            widthPx = widthPx,
                            heightPx = heightPx,
                        ),
                    )
                }
            },
            modifier = Modifier.fillMaxSize(),
        )
        if (showLandmarks) {
            LandmarkOverlay(pose = overlayPose, frameWidth = frameWidth, frameHeight = frameHeight)
        }
    }
}

@Composable
actual fun rememberCameraPermissionController(): CameraPermissionController {
    var granted by remember { mutableStateOf(isCameraAuthorized()) }

    return remember(granted) {
        object : CameraPermissionController {
            override val isGranted: Boolean get() = granted

            override fun request(onResult: (Boolean) -> Unit) {
                if (isCameraAuthorized()) {
                    granted = true
                    onResult(true)
                    return
                }
                // requestAccess calls back on an arbitrary queue; state and the callback have to
                // reach the main thread. The earlier version also called this straight from the
                // composable body, so it re-fired on every recomposition.
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        granted = isGranted
                        onResult(isGranted)
                    }
                }
            }
        }
    }
}

private fun isCameraAuthorized(): Boolean =
    AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
