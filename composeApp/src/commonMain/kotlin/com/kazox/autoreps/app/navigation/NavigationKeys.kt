package com.kazox.autoreps.app.navigation

import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.dumbbell
import autoreps.composeapp.generated.resources.home_screen_title
import autoreps.composeapp.generated.resources.house
import autoreps.composeapp.generated.resources.record
import autoreps.composeapp.generated.resources.record_screen_title
import autoreps.composeapp.generated.resources.workouts_screen_title
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
    override val description = Res.string.home_screen_title
    override val title = Res.string.home_screen_title
}

data object Record : TopLevelRoute {
    override val icon = Res.drawable.record
    override val description = Res.string.record_screen_title
    override val title = Res.string.record_screen_title
}

data object Exercises : TopLevelRoute {
    override val icon = Res.drawable.dumbbell
    override val description = Res.string.workouts_screen_title
    override val title = Res.string.workouts_screen_title
}

val TOP_LEVEL_ROUTES : List<TopLevelRoute> = listOf(Home, Record, Exercises)

data object Explanation
data object DailyGoal

data class AddWorkout(
    val workout: Workout,
    val reps: List<Rep>
)

data class EditWorkout(
    val workout: Workout,
    val reps: List<Rep>
)