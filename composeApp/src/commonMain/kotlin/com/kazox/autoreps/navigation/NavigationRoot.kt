package com.kazox.autoreps.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kazox.autoreps.CounterScreen
import com.kazox.autoreps.StartScreen
import com.kazox.autoreps.ExerciseListScreen
import com.kazox.autoreps.feature.record.presentation.countReps.CountRepsScreen
import com.kazox.autoreps.feature.workout.presentation.addEditWorkout.AddEditWorkoutScreen
import com.kazox.autoreps.feature.workout.presentation.workouts.components.Workouts

@Composable
fun NavigationRoot() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }

    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
            entry<Home>{
                WorkoutsScreen(
                    topLevelBackStack
                )
            }
            entry<Record>{
                CountRepsScreen(
                    topLevelBackStack
                )
            }
            entry<Exercises>{
                ExerciseListScreen(
                    topLevelBackStack,
                    onExerciseSelected = { exercise ->

                    }
                )
            }
            entry<AddWorkout>{ values ->
                AddEditWorkoutScreen(
                    topLevelBackStack,
                    values.workout.copy(), // copy to avoid mutation issues due to the navkey comparing serialized objects
                    values.reps.map { it.copy() },
                    onSaveNavigation = {
                        topLevelBackStack.addTopLevel(Home)
                    }
                )
            }
            entry<EditWorkout> { values ->
                AddEditWorkoutScreen(
                    topLevelBackStack,
                    values.workout.copy(), // copy to avoid mutation issues due to the navkey comparing serialized objects
                    values.reps.map { it.copy() },
                    onSaveNavigation = {
                        topLevelBackStack.removeLast()
                    }
                )
            }
        },
    )
}

@Composable
fun WorkoutsScreen(
    topLevelBackStack: TopLevelBackStack<Any>
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomNavigationBar(topLevelBackStack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Workouts(
                topLevelBackStack
            )
        }
    }
}