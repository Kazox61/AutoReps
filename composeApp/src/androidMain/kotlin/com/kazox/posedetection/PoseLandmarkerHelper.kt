package com.kazox.posedetection

import android.content.Context
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

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
