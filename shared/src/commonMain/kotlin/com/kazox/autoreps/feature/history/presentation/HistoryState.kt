package com.kazox.autoreps.feature.history.presentation

import androidx.compose.runtime.Stable
import com.kazox.autoreps.core.domain.model.Workout

@Stable
data class HistoryState(
    val workouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
)
