package com.kazox.autoreps.feature.record.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.kazox.autoreps.core.domain.pose.LandmarkType
import com.kazox.autoreps.core.domain.pose.Pose
import java.util.concurrent.Executors

private const val MODEL_ASSET = "pose_landmarker_full.task"

@Composable
actual fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onFrame: (PoseFrame) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    // Held in rememberUpdatedState so the analyzer always calls the newest lambda without the
    // camera pipeline having to be rebuilt when the caller recomposes.
    val currentOnFrame by rememberUpdatedState(onFrame)

    var overlayPose by remember { mutableStateOf<Pose?>(null) }
    var frameWidth by remember { mutableStateOf(0) }
    var frameHeight by remember { mutableStateOf(0) }
    val smoother = remember { PoseSmoother() }

    val analyzer =
        remember {
            PoseAnalyzer(context) { frame ->
                // Only the drawn skeleton is smoothed; currentOnFrame gets the raw pose, since
                // the rep detector's timing must not be shifted by a filter. A lost pose still
                // resets the overlay, or it would keep drawing the last skeleton over an empty
                // room.
                overlayPose = smoother.smooth(frame.pose, frame.timestampMillis)
                frameWidth = frame.widthPx
                frameHeight = frame.heightPx
                currentOnFrame(frame)
            }
        }

    DisposableEffect(Unit) {
        onDispose { analyzer.close() }
    }

    // Camera binding belongs in an effect, not in AndroidView's update block. update runs on
    // every recomposition, and this composable recomposes on every analysed frame — so binding
    // there tore down and rebuilt the whole pipeline ~30 times a second, via a blocking get().
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.awaitInstance(context)
        val preview =
            Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }
        val imageAnalysis =
            ImageAnalysis
                .Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                // RGBA out of the box beats converting YUV per frame ourselves.
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .apply { setAnalyzer(analyzer.executor, analyzer) }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            preview,
            imageAnalysis,
        )
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (showLandmarks) {
            LandmarkOverlay(pose = overlayPose, frameWidth = frameWidth, frameHeight = frameHeight)
        }
    }
}

/**
 * Runs pose detection on camera frames.
 *
 * Analysis happens on a dedicated single thread — the earlier version used the main executor, so
 * every frame's colour conversion competed with rendering.
 */
private class PoseAnalyzer(
    context: Context,
    private val onFrame: (PoseFrame) -> Unit,
) : ImageAnalysis.Analyzer {
    val executor = Executors.newSingleThreadExecutor()

    /** Frame geometry currently in flight, for the result callback. */
    private var pendingWidth = 0
    private var pendingHeight = 0
    private var pendingRotation = 0

    private val landmarker: PoseLandmarker =
        PoseLandmarker.createFromOptions(
            context,
            PoseLandmarker.PoseLandmarkerOptions
                .builder()
                .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_ASSET).build())
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .setResultListener(::onResult)
                .setErrorListener { it.printStackTrace() }
                .build(),
        )

    override fun analyze(imageProxy: ImageProxy) {
        // try/finally: if conversion throws even once without closing the proxy, the analyzer
        // stalls forever on a leaked buffer.
        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val upright = rotation == 90 || rotation == 270
            pendingWidth = if (upright) imageProxy.height else imageProxy.width
            pendingHeight = if (upright) imageProxy.width else imageProxy.height

            pendingRotation = rotation

            val image = BitmapImageBuilder(imageProxy.toBitmap()).build()
            // No ImageProcessingOptions: MediaPipe returns landmarks in the *input* image's
            // coordinate space, so telling it the rotation does not bring the output upright —
            // it only affects inference. The rotation is undone on the results instead, which is
            // what the working version of this app did on both platforms.
            //
            // The frame's own monotonic capture time. Wall-clock time can jump backwards and
            // MediaPipe rejects non-increasing timestamps.
            landmarker.detectAsync(image, imageProxy.imageInfo.timestamp / 1_000_000)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun onResult(
        result: PoseLandmarkerResult,
        @Suppress("UNUSED_PARAMETER") input: com.google.mediapipe.framework.image.MPImage,
    ) {
        val normalized = result.landmarks().firstOrNull()
        val world = result.worldLandmarks().firstOrNull()
        val width = pendingWidth
        val height = pendingHeight
        val rotation = pendingRotation
        val timestamp = result.timestampMs()

        if (normalized == null || world == null) {
            onFrame(PoseFrame(pose = null, timestampMillis = timestamp, widthPx = width, heightPx = height))
            return
        }

        val landmarks =
            normalized.mapIndexedNotNull { index, point ->
                val type = LandmarkType.fromIndex(index) ?: return@mapIndexedNotNull null
                val worldPoint = world.getOrNull(index) ?: return@mapIndexedNotNull null
                val (rx, ry) = rotateNormalized(point.x(), point.y(), rotation)
                Pose.Landmark(
                    type = type,
                    // The preview is mirrored for the front camera but the analysis frame is
                    // not, so x is flipped to put the skeleton on top of the body.
                    x = 1f - rx,
                    y = ry,
                    wx = worldPoint.x(),
                    wy = worldPoint.y(),
                    wz = worldPoint.z(),
                    visibility = point.visibility().orElse(1f),
                )
            }

        onFrame(
            PoseFrame(
                pose = Pose(landmarks),
                timestampMillis = timestamp,
                widthPx = width,
                heightPx = height,
            ),
        )
    }

    fun close() {
        landmarker.close()
        executor.shutdown()
    }
}

/**
 * Maps a normalized point from the sensor frame into the upright display frame.
 *
 * [rotationDegrees] is how far the sensor image must turn clockwise to stand upright, which is
 * what `ImageInfo.rotationDegrees` reports. Applying the same turn to the point puts it where the
 * preview draws it.
 */
private fun rotateNormalized(
    x: Float,
    y: Float,
    rotationDegrees: Int,
): Pair<Float, Float> =
    when (rotationDegrees) {
        90 -> (1f - y) to x
        180 -> (1f - x) to (1f - y)
        270 -> y to (1f - x)
        else -> x to y
    }

@Composable
actual fun rememberCameraPermissionController(): CameraPermissionController {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(context.hasCameraPermission()) }
    var pendingCallback by remember { mutableStateOf<((Boolean) -> Unit)?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            granted = result
            pendingCallback?.invoke(result)
            pendingCallback = null
        }

    var requested by remember { mutableStateOf(false) }
    // Launching is a side effect, so it happens in an effect rather than during composition as
    // the earlier version did — that fired the system dialog again on every recomposition.
    LaunchedEffect(requested) {
        if (requested) {
            requested = false
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    return remember(granted) {
        object : CameraPermissionController {
            override val isGranted: Boolean get() = granted

            override fun request(onResult: (Boolean) -> Unit) {
                if (context.hasCameraPermission()) {
                    granted = true
                    onResult(true)
                    return
                }
                pendingCallback = onResult
                requested = true
            }
        }
    }
}

private fun Context.hasCameraPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
