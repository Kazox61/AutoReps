package com.kazox.autoreps.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.kazox.autoreps.feature.home.presentation.HomeScreen
import com.kazox.autoreps.feature.record.presentation.countReps.CountRepsScreen
import com.kazox.autoreps.feature.setup.presentation.dailyGoal.DailyGoalScreen
import com.kazox.autoreps.feature.workout.presentation.addEditWorkout.AddEditWorkoutScreen
import com.kazox.autoreps.feature.workout.presentation.workouts.components.Workouts
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NavigationRoot(
    navigationViewModel: NavigationViewModel = koinViewModel()
) {

    val setupFinished = navigationViewModel.setupFinished.collectAsState()

    if (setupFinished.value == null) {
        //TODO: should never happen since the splash screen waits for the setupFinished value to be set
        SetupNavDisplay()
    }
    else if (setupFinished.value == false) {
        SetupNavDisplay()
    }
    else if (setupFinished.value == true) {
        MainNavDisplay()
    }
}

@Composable
fun SetupNavDisplay(
    navigationViewModel: NavigationViewModel = koinViewModel()
) {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(DailyGoal) }

    val coroutineScope = rememberCoroutineScope()

    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
            entry<DailyGoal>{
                DailyGoalScreen(
                    onFinish = {
                        navigationViewModel.updateSetupFinished()
                    }
                )
            }
        }
    )
}

@Composable
fun MainNavDisplay() {
    val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }

    NavDisplay(
        backStack = topLevelBackStack.backStack,
        onBack = { topLevelBackStack.removeLast() },
        entryProvider = entryProvider {
            entry<Home>{
                HomeScreen(
                    topLevelBackStack
                )
            }
            entry<Record>{
                CountRepsScreen(
                    topLevelBackStack
                )
            }
            entry<Exercises>{
                WorkoutsScreen(
                    topLevelBackStack
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