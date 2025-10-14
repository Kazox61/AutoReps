package com.kazox.autoreps.app.navigation

import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.exercises
import autoreps.composeapp.generated.resources.home
import autoreps.composeapp.generated.resources.house
import autoreps.composeapp.generated.resources.record
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


sealed interface TopLevelRoute {
    val icon: DrawableResource
    val description: StringResource
    val title: StringResource
}
data object Home : TopLevelRoute {
    override val icon = Res.drawable.house
    override val description = Res.string.home
    override val title = Res.string.home
}

data object Record : TopLevelRoute {
    override val icon = Res.drawable.record
    override val description = Res.string.record
    override val title = Res.string.record
}

data object Exercises : TopLevelRoute {
    override val icon = Res.drawable.house
    override val description = Res.string.exercises
    override val title = Res.string.exercises
}

val TOP_LEVEL_ROUTES : List<TopLevelRoute> = listOf(Home, Record, Exercises)

data object DailyGoal

data class AddWorkout(
    val workout: Workout,
    val reps: List<Rep>
)

data class EditWorkout(
    val workout: Workout,
    val reps: List<Rep>
)