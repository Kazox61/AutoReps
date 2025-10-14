package com.kazox.autoreps.feature.workout.presentation.workouts.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.average_frequency
import autoreps.composeapp.generated.resources.date
import autoreps.composeapp.generated.resources.duration
import autoreps.composeapp.generated.resources.max_reps_in_row
import autoreps.composeapp.generated.resources.number_of_sets
import autoreps.composeapp.generated.resources.reps
import com.kazox.autoreps.core.domain.util.formatDuration
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.domain.util.startedDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
fun WorkoutInsights(
    workout: Workout,
    reps: List<Rep>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WorkoutInsightsRow(
            description1 = stringResource(Res.string.reps),
            value1 = workout.reps.toString(),
            description2 = stringResource(Res.string.duration),
            value2 = formatDuration(workout.duration)
        )

        val averageFrequency = round(workout.reps / (workout.duration / 60f)).toInt()
        WorkoutInsightsRow(
            description1 = stringResource(Res.string.average_frequency),
            value1 = "$averageFrequency rpm",
            description2 = stringResource(Res.string.date),
            value2 = workout.startedDateTime.toString()
        )

        WorkoutInsightsRow(
            description1 = stringResource(Res.string.number_of_sets),
            value1 = getSetCount(reps).toString(),
            description2 = stringResource(Res.string.max_reps_in_row),
            value2 = getMaxRepsInSet(reps).toString()
        )
    }
}

fun getSetCount(reps: List<Rep>): Int {
    return reps.groupBy { it.setId }
        .size
}

fun getMaxRepsInSet(reps: List<Rep>): Int {
    return reps.groupBy { it.setId }
        .mapValues { it.value.size }
        .values
        .maxOrNull() ?: 0
}