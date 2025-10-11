package com.kazox.posedetection

import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun PoseCameraView(
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    val poseHelper = remember { PoseLandmarkerHelper(context) }
    val pose = remember { mutableStateOf<Pose?>(null) }
    val currentFrameWidth = remember { mutableIntStateOf(0) }
    val currentFrameHeight = remember { mutableIntStateOf(0) }

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
            pose.value = detectedPose
            onPoseDetected(detectedPose)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
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

                            currentFrameWidth.intValue =
                                if (rotated) imageProxy.height else imageProxy.width
                            currentFrameHeight.intValue =
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
        LandmarkOverlay(pose.value, currentFrameWidth.intValue, currentFrameHeight.intValue)
    }
}

private fun rotateLandmark(x: Float, y: Float): Pair<Float, Float> {
    return Pair(y, 1-x)
}
