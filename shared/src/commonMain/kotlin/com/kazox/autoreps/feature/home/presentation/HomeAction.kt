package com.kazox.autoreps.feature.home.presentation

sealed interface HomeAction {
    /** A day in the heatmap was tapped. */
    data class SelectDay(val date: kotlinx.datetime.LocalDate?) : HomeAction
}
