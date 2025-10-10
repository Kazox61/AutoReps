package com.kazox.posedetection

import android.media.Image
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

@OptIn(ExperimentalGetImage::class)
@Composable
actual fun PoseCameraView(
    showLandmarks: Boolean,
    onPoseDetected: (Pose?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val pose = remember { mutableStateOf<Pose?>(null) }
    val currentFrameWidth = remember { mutableIntStateOf(0) }
    val currentFrameHeight = remember { mutableIntStateOf(0) }

    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize(),
        update = {
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        val rotated = imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270
                        currentFrameWidth.intValue = if (rotated) imageProxy.height else imageProxy.width
                        currentFrameHeight.intValue = if (rotated) imageProxy.width else imageProxy.height
                        detectPose(
                            imageProxy.image,
                            imageProxy.imageInfo.rotationDegrees,
                            onPoseDetected = {
                                pose.value = it
                                onPoseDetected(it)
                                imageProxy.close()
                            }
                        )
                    }
                }

            provider.unbindAll()
            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview, imageAnalyzer)
        }
    )

    LandmarkView(pose.value, currentFrameWidth.intValue, currentFrameHeight.intValue)
}

val poseDetector by lazy {
    PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )
}

fun detectPose(
    mediaImage: Image?,
    rotationDegrees: Int,
    onPoseDetected: (Pose?) -> Unit
) {
    if (mediaImage == null) {
        onPoseDetected(null)
        return
    }

    try {
        val image = InputImage.fromMediaImage(
            mediaImage,
            rotationDegrees
        )

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val landmarks = pose.allPoseLandmarks.map { landmark ->
                    Pose.Landmark(
                        type = landmark.landmarkType,
                        x = landmark.position.x,
                        y = landmark.position.y,
                        wx = landmark.position3D.x,
                        wy = landmark.position3D.y,
                        wz = landmark.position3D.z,
                        confidence = landmark.inFrameLikelihood
                    )
                }

                onPoseDetected(Pose(landmarks))
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onPoseDetected(null)
            }

    } catch (e: Exception) {
        e.printStackTrace()
        onPoseDetected(null)
    }
}