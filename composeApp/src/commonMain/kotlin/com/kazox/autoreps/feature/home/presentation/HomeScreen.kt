package com.kazox.autoreps.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kazox.autoreps.feature.home.presentation.components.MonthlyHeatmapCalendar
import com.kazox.autoreps.feature.home.presentation.components.TodaysProgress
import com.kazox.autoreps.app.navigation.BottomNavigationBar
import com.kazox.autoreps.app.navigation.TopLevelBackStack
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    topLevelBackStack: TopLevelBackStack<Any>,
    viewModel: HomeViewModel = koinViewModel()
) {
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val todayReps by viewModel.todayReps.collectAsState()
    val workouts by viewModel.workouts.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(topLevelBackStack) },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TodaysProgress(dailyGoal, todayReps)
            Spacer(Modifier.height(64.dp))
            MonthlyHeatmapCalendar(dailyGoal, workouts)
        }
    }
}