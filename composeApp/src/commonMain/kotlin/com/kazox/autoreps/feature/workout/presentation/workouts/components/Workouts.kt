package com.kazox.autoreps.feature.workout.presentation.workouts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazox.autoreps.feature.workout.presentation.workouts.WorkoutsViewModel
import com.kazox.autoreps.navigation.EditWorkout
import com.kazox.autoreps.navigation.TopLevelBackStack
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Workouts(
    topLevelBackStack: TopLevelBackStack<Any>,
    viewModel: WorkoutsViewModel = koinViewModel()
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(viewModel.workouts.value) { workout ->
            val reps by viewModel.repsForWorkout(workout.id).collectAsState()
            WorkoutItem(
                workout = workout,
                reps,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        topLevelBackStack.add(EditWorkout(
                            workout,
                            reps
                        ))
                    }
            )
        }
    }

}