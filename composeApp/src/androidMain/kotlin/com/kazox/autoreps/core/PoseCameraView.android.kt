package com.kazox.autoreps.core

import android.content.Context
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun PoseCameraView(
    modifier: Modifier,
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    val poseHelper = remember { PoseLandmarkerHelper(context) }
    var pose by remember { mutableStateOf<Pose?>(null) }
    var currentFrameWidth by remember { mutableIntStateOf(0) }
    var currentFrameHeight by remember { mutableIntStateOf(0) }

    val aspectRatio by remember(currentFrameWidth, currentFrameHeight) {
        derivedStateOf {
            if (currentFrameWidth > 0 && currentFrameHeight > 0) {
                currentFrameWidth.toFloat() / currentFrameHeight.toFloat()
            } else {
                3f / 4f
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { poseHelper.close() }
    }

    poseHelper.onPoseResult = { result, _ ->
        val mediaPipeLandmarks = result.landmarks().firstOrNull()
        val worldLandmarks = result.worldLandmarks().firstOrNull()

        if (mediaPipeLandmarks == null || worldLandmarks == null) {
            onPoseDetected(null)
        } else {
            val landmarks = mediaPipeLandmarks.mapIndexed { index, lm ->
                val world = worldLandmarks[index]
                val rotated = rotateLandmark(lm.x(), lm.y())
                Pose.Landmark(
                    type = LandmarkType.fromIndex(index),
                    x = rotated.first,
                    y = rotated.second,
                    wx = world.x(),
                    wy = world.y(),
                    wz = world.z()
                )
            }

            val detectedPose = Pose(landmarks)
            pose = detectedPose
            onPoseDetected(detectedPose)
        }
    }

    Box(
        modifier = modifier.then(Modifier.aspectRatio(aspectRatio))
    ) {
        AndroidView(
            factory = { previewView },
            update = {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                val preview = Preview.Builder().build().apply {
                    surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val bitmap = imageProxy.toBitmap()
                                val mpImage = BitmapImageBuilder(bitmap).build()
                                val timestamp = System.currentTimeMillis()
                                poseHelper.detectAsync(mpImage, timestamp)

                                val rotated =
                                    imageProxy.imageInfo.rotationDegrees == 90 ||
                                            imageProxy.imageInfo.rotationDegrees == 270

                                currentFrameWidth =
                                    if (rotated) imageProxy.height else imageProxy.width
                                currentFrameHeight =
                                    if (rotated) imageProxy.width else imageProxy.height
                            }
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            }
        )

        if (showLandmarks) {
            LandmarkOverlay(pose, currentFrameWidth, currentFrameHeight)
        }
    }
}

class PoseLandmarkerHelper(context: Context) {
    private val modelName = "pose_landmarker_full.task"

    val poseLandmarker: PoseLandmarker
    var onPoseResult: ((PoseLandmarkerResult, MPImage) -> Unit)? = null

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath(modelName)
            .build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(0.5f)
            .setMinPosePresenceConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setResultListener { result, mpImage ->
                onPoseResult?.invoke(result, mpImage)
            }
            .setErrorListener { e ->
                e.printStackTrace()
            }
            .build()

        poseLandmarker = PoseLandmarker.createFromOptions(context, options)
    }

    fun detectAsync(mpImage: MPImage, timestamp: Long) {
        poseLandmarker.detectAsync(mpImage, timestamp)
    }

    fun close() {
        poseLandmarker.close()
    }
}

private fun rotateLandmark(x: Float, y: Float): Pair<Float, Float> {
    return Pair(y, 1-x)
}
