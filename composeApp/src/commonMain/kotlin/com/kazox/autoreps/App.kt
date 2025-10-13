package com.kazox.autoreps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabase
import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabaseConstructor
import com.kazox.autoreps.feature.workout.data.data_source.getWorkoutDatabase
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.navigation.NavigationRoot
import com.kazox.autoreps.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val scope = CoroutineScope(Dispatchers.Default)

    LaunchedEffect(Unit) {
        delay(2000)


        val db: WorkoutDatabase = WorkoutDatabaseConstructor.initialize()
        val dao = db.getDao()

        scope.launch {

            // Sample insert
            val workout = Workout(
                reps = 20,
                startedAt = "2024-10-01T10:00:00Z",
                duration = 20
            )

            dao.insertWorkout(workout)

            // Sample query
            val workouts = dao.getWorkouts()
            println("Workouts from DB: ${workouts.count()}")
        }
    }

    AppTheme {
        NavigationRoot()
    }
}