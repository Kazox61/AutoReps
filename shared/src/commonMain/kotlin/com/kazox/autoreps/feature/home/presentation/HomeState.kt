package com.kazox.autoreps.feature.home.presentation

import androidx.compose.runtime.Stable
import com.kazox.autoreps.core.domain.model.AppSettings
import com.kazox.autoreps.core.domain.model.Workout
import kotlinx.datetime.LocalDate

@Stable
data class HomeState(
    val today: LocalDate? = null,
    val todayReps: Int = 0,
    /** Reps that count as a full day, from Settings. Every number here is measured against it. */
    val dailyGoal: Int = AppSettings().dailyGoal,
    /** Consecutive days with at least one rep, ending today or yesterday. */
    val streak: Int = 0,
    /** Total across the last seven days including today. */
    val weekTotal: Int = 0,
    /** Best single day ever. */
    val bestDay: Int = 0,
    val lifetimeReps: Int = 0,
    /** Reps per day, for the heatmap and the weekly bars. */
    val repsByDate: Map<LocalDate, Int> = emptyMap(),
    val recentWorkouts: List<Workout> = emptyList(),
    val isLoading: Boolean = true,
)

