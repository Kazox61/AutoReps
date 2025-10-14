package com.kazox.autoreps.feature.record.presentation.countReps

import com.kazox.autoreps.core.Pose

sealed class CountRepsEvent {
    data object StartWorkout : CountRepsEvent()
    data class PoseDetected(val pose: Pose) : CountRepsEvent()
}