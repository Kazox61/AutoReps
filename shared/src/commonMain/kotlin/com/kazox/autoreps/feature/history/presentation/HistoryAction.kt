package com.kazox.autoreps.feature.history.presentation

import com.kazox.autoreps.core.domain.model.Workout

sealed interface HistoryAction {
    data class DeleteWorkout(val workout: Workout) : HistoryAction
}
