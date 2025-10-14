package com.kazox.autoreps.feature.setup.presentation.dailyGoal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.next
import autoreps.composeapp.generated.resources.setup_daily_goal
import com.kazox.autoreps.core.presentation.components.NumberWheelPicker
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DailyGoalScreen(
    onNext: () -> Unit,
    viewModel: DailyGoalViewModel = koinViewModel()
) {
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                stringResource(Res.string.setup_daily_goal),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            NumberWheelPicker(
                initial = dailyGoal,
                to = 500,
                onValueChanged = { newGoal ->
                    coroutineScope.launch {
                        viewModel.onEvent(DailyGoalEvent.UpdateDailyGoal(newGoal))
                    }
                }
            )

            Button(
                onClick = onNext,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    stringResource(Res.string.next),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}