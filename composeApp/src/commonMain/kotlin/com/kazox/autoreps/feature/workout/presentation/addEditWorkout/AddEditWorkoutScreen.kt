package com.kazox.autoreps.feature.workout.presentation.addEditWorkout

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.name_workout
import autoreps.composeapp.generated.resources.reps
import autoreps.composeapp.generated.resources.sets
import com.kazox.autoreps.app.navigation.TopLevelBackStack
import com.kazox.autoreps.core.domain.util.format
import com.kazox.autoreps.feature.workout.domain.model.Rep
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.feature.workout.presentation.addEditWorkout.components.TopBar
import com.kazox.autoreps.feature.workout.presentation.workouts.components.WorkoutInsights
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.RowChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.VerticalIndicatorProperties
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddEditWorkoutScreen (
    topLevelBackStack: TopLevelBackStack<Any>,
    workout: Workout,
    reps: List<Rep>,
    onSaveNavigation: () -> Unit,
    viewModel: AddEditViewModel = koinViewModel()
) {
    viewModel.initWorkout(workout, reps)

    Scaffold(
        topBar = {
            TopBar(
                onBack = {
                    topLevelBackStack.removeLast()
                },
                onSave = {
                    viewModel.onEvent(AddEditWorkoutEvent.SaveWorkout)
                    onSaveNavigation()
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                value = viewModel.workoutName.value,
                onValueChange = {
                    viewModel.onEvent(AddEditWorkoutEvent.EnteredName(it))
                },
                placeholder = { Text(stringResource(Res.string.name_workout)) },
                singleLine = true
            )

            WorkoutInsights(workout, reps)

            // SetBarChart(reps)
            RepsLineChart(reps)

            /*Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(ButtonDefaults.outlinedButtonBorder().width, MaterialTheme.colorScheme.error),
            ) {
                Text(
                    text = stringResource(R.string.discard_workout),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }*/
        }
    }
}

@Composable
fun SetBarChart(reps: List<Rep>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.sets),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        val primaryColor = SolidColor(MaterialTheme.colorScheme.primary)

        val data = remember {
            getSetReps(reps).mapIndexed { index, repsInSet ->
                Bars(
                    label = (index + 1).toString(),
                    values = listOf(
                        Bars.Data(
                            value = repsInSet.toDouble(),
                            color = primaryColor
                        )
                    )
                )
            }
        }
        RowChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(data.size * 40.dp),
            data = data,
            barProperties = BarProperties(
                spacing = 1.dp,
                thickness = 12.dp,
            ),
            indicatorProperties = VerticalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                count = IndicatorCount.StepBased(1.0),
                contentBuilder = { indicator ->
                    indicator.format(1)
                },
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            ),
            gridProperties = GridProperties(
                enabled = false
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
}

@Composable
fun RepsLineChart(reps: List<Rep>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(Res.string.reps),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        val primaryColor = SolidColor(MaterialTheme.colorScheme.primary)
        val data = remember {
            listOf(
                Line(
                    label = "Reps",
                    values = getSetReps(reps).map { rep ->
                        rep.toDouble()
                    },
                    color = primaryColor,
                    strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                    drawStyle = DrawStyle.Stroke(2.dp)
                )
            )
        }
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            data = data,
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
                count = IndicatorCount.StepBased(1.0),
                contentBuilder = { indicator ->
                    indicator.format(0)
                },
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                ),
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            ),
            gridProperties = GridProperties(
                enabled = true
            )
        )
    }
}

fun getSetReps(reps: List<Rep>): List<Int> {
    return reps.groupBy { it.setId }
        .mapValues { it.value.size }
        .values
        .toList()
}