package com.kazox.autoreps.feature.workout.presentation

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.presentation.ObserveAsEvents
import com.kazox.autoreps.resources.Res
import com.kazox.autoreps.resources.action_back
import com.kazox.autoreps.resources.action_save
import com.kazox.autoreps.resources.error_save_failed
import com.kazox.autoreps.resources.format_date_day_month
import com.kazox.autoreps.resources.format_duration
import com.kazox.autoreps.resources.format_seconds_short
import com.kazox.autoreps.resources.format_seconds_whole
import com.kazox.autoreps.resources.reps_count
import com.kazox.autoreps.resources.unit_avg
import com.kazox.autoreps.resources.unit_reps_short
import com.kazox.autoreps.resources.unit_sec_per_rep
import com.kazox.autoreps.resources.workout_cadence_avg
import com.kazox.autoreps.resources.workout_cadence_fastest
import com.kazox.autoreps.resources.workout_cadence_slowest
import com.kazox.autoreps.resources.workout_cadence_subtitle
import com.kazox.autoreps.resources.workout_cadence_title
import com.kazox.autoreps.resources.workout_comparison_before
import com.kazox.autoreps.resources.workout_comparison_change
import com.kazox.autoreps.resources.workout_comparison_percent
import com.kazox.autoreps.resources.workout_comparison_reps_delta
import com.kazox.autoreps.resources.workout_comparison_title
import com.kazox.autoreps.resources.workout_edit_title
import com.kazox.autoreps.resources.workout_empty_hint
import com.kazox.autoreps.resources.workout_empty_title
import com.kazox.autoreps.resources.workout_name
import com.kazox.autoreps.resources.workout_rest_avg
import com.kazox.autoreps.resources.workout_rest_rest
import com.kazox.autoreps.resources.workout_rest_subtitle
import com.kazox.autoreps.resources.workout_rest_title
import com.kazox.autoreps.resources.workout_rest_work
import com.kazox.autoreps.resources.workout_stat_dropoff
import com.kazox.autoreps.resources.workout_stat_duration
import com.kazox.autoreps.resources.workout_stat_reps
import com.kazox.autoreps.resources.workout_stat_sets
import com.kazox.autoreps.resources.workout_trend_subtitle
import com.kazox.autoreps.resources.workout_trend_title
import com.kazox.ui.components.button.Button
import com.kazox.ui.components.button.ButtonSize
import com.kazox.ui.components.button.ButtonVariant
import com.kazox.ui.components.card.Card
import com.kazox.ui.components.card.CardContent
import com.kazox.ui.components.card.CardHeader
import com.kazox.ui.components.icon.Icon
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.input.Input
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.skeleton.Skeleton
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.components.topappbar.TopAppBar
import com.kazox.ui.foundation.KazTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AddEditWorkoutRoot(
    workoutId: Int?,
    onBack: () -> Unit,
    /** Where a successful save leaves to, which is not always where back goes. */
    onSaved: () -> Unit = onBack,
    viewModel: AddEditWorkoutViewModel =
        koinViewModel(key = "AddEditWorkout_$workoutId") {
            parametersOf(workoutId)
        },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.saved) { onSaved() }

    AddEditWorkoutScreen(
        onBack = onBack,
        state = state,
        onAction = viewModel::onAction,
    )
}

/**
 * Pushed onto the current tab's stack, so it renders no bottom bar and is left via [onBack] —
 * or, after a successful save, wherever [AddEditWorkoutRoot]'s `onSaved` points.
 *
 * With a loaded [AddEditWorkoutState.workout] the screen shows stats tiles and two charts
 * (per-set trend vs. average, per-set comparison); without one (`workoutId == null` route)
 * it degrades to the name field and an explanation until the Record flow creates rows.
 */
@Composable
fun AddEditWorkoutScreen(
    onBack: () -> Unit,
    state: AddEditWorkoutState,
    onAction: (AddEditWorkoutAction) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.workout_edit_title),
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                        label = stringResource(Res.string.action_back),
                    ) {
                        Icon(imageVector = KazIcons.ArrowLeft, contentDescription = null)
                    }
                },
                actions = {
                    Button(
                        onClick = { onAction(AddEditWorkoutAction.SaveWorkout) },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                        label = stringResource(Res.string.action_save),
                        enabled = state.workout != null && !state.isSaving,
                        loading = state.isSaving,
                    ) {
                        Icon(imageVector = KazIcons.Check, contentDescription = null)
                    }
                },
            )
        },
    ) {
        if (state.isLoading) {
            LoadingBody()
        } else {
            ContentBody(
                state = state,
                onAction = onAction,
            )
        }
    }
}

// ─── Body ─────────────────────────────────────────────────────

@Composable
private fun ContentBody(
    state: AddEditWorkoutState,
    onAction: (AddEditWorkoutAction) -> Unit,
) {
    // Derived once per reps change; remember also keeps the charts' data instances stable
    // so typing in the name field below doesn't replay their entry animation.
    val perSet = remember(state.reps) { repsPerSet(state.reps) }
    val workout = state.workout

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = KazTheme.spacing.lg,
                    end = KazTheme.spacing.lg,
                    top = KazTheme.spacing.lg,
                    bottom = KazTheme.spacing.lg,
                ),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.md),
    ) {
        Input(
            value = state.name,
            onValueChange = { onAction(AddEditWorkoutAction.EnteredName(it)) },
            placeholder = stringResource(Res.string.workout_name),
            label = stringResource(Res.string.workout_name),
            singleLine = true,
            isError = state.saveError != null,
            errorMessage = state.saveError?.let { stringResource(Res.string.error_save_failed, it) }.orEmpty(),
        )

        if (workout == null || perSet.isEmpty()) {
            EmptyDataCard()
        } else {
            StatTiles(workout = workout, perSet = perSet, reps = state.reps)
            ComparisonCard(workout = workout, previous = state.previousWorkout)
            TrendCard(workout = workout, perSet = perSet)
            CadenceCard(reps = state.reps)
            RestCard(reps = state.reps)
        }
    }
}

// ─── Stats ────────────────────────────────────────────────────

@Composable
private fun StatTiles(
    workout: Workout,
    perSet: List<Int>,
    reps: List<Rep>,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm)) {
        StatTile(label = stringResource(Res.string.workout_stat_reps), value = workout.reps.toString(), modifier = Modifier.weight(1f))
        StatTile(label = stringResource(Res.string.workout_stat_sets), value = perSet.size.toString(), modifier = Modifier.weight(1f))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm)) {
        StatTile(label = stringResource(Res.string.workout_stat_duration), value = formatDuration(workout.duration), modifier = Modifier.weight(1f))
        StatTile(
            label = stringResource(Res.string.workout_stat_dropoff),
            value = formatDropOff(remember(reps) { dropOff(reps) }),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, label = label) {
        Text(text = label, variant = TextVariant.Muted)
        Text(text = value, variant = TextVariant.Large, maxLines = 1)
    }
}

// ─── Charts ───────────────────────────────────────────────────

@Composable
private fun TrendCard(
    workout: Workout,
    perSet: List<Int>,
) {
    val colors = KazTheme.colors
    val primaryBrush = SolidColor(colors.primary)
    val averageBrush = SolidColor(colors.onMuted)
    val chartText = KazTheme.typography.small.copy(color = colors.onMuted)
    val average = workout.reps.toDouble() / perSet.size

    Card {
        CardHeader {
            Text(text = stringResource(Res.string.workout_trend_title), variant = TextVariant.H3)
            Text(text = stringResource(Res.string.workout_trend_subtitle), variant = TextVariant.Muted)
        }
        CardContent {
            // Keyed on perSet (structurally comparable, unlike SolidColor): rebuilt when the
            // reps actually change, reused verbatim on every other recomposition — that is
            // what keeps typing in the name field from replaying the entry animation.
            val repsLabel = stringResource(Res.string.unit_reps_short)
            val avgLabel = stringResource(Res.string.unit_avg)
            val data =
                remember(perSet, repsLabel, avgLabel) {
                    listOf(
                        Line(
                            label = repsLabel,
                            values = perSet.map { it.toDouble() },
                            color = primaryBrush,
                            strokeAnimationSpec = tween(900, easing = EaseInOutCubic),
                            drawStyle = DrawStyle.Stroke(2.dp),
                        ),
                        Line(
                            label = avgLabel,
                            values = List(perSet.size) { average },
                            color = averageBrush,
                            strokeAnimationSpec = tween(900),
                            drawStyle = DrawStyle.Stroke(1.dp),
                        ),
                    )
                }
            LineChart(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                data = data,
                indicatorProperties =
                    HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = chartText,
                        count = IndicatorCount.StepBased(3.0),
                        contentBuilder = { it.toInt().toString() },
                    ),
                labelProperties = LabelProperties(enabled = true, textStyle = chartText),
                labelHelperProperties = LabelHelperProperties(enabled = true),
                gridProperties = GridProperties(enabled = true),
            )
        }
    }
}

/**
 * Rest between sets. Deliberately *not* another view of reps-per-set — that is what [TrendCard]
 * already shows, and a second encoding of the same series says nothing new. Reps are what you
 * managed; rest is what it cost you, and the two together explain the fatigue curve.
 *
 * Every value here comes from [Rep.timestamp], which the rest of the screen ignores.
 */
@Composable
private fun RestCard(
    reps: List<Rep>,
) {
    val colors = KazTheme.colors
    val primaryBrush = SolidColor(colors.primary)
    val chartText = KazTheme.typography.small.copy(color = colors.onMuted)

    val rests = remember(reps) { restAfterEachSet(reps) }
    val work = remember(reps) { workSeconds(reps) }
    val rest = remember(reps) { restSeconds(reps) }

    if (rests.isEmpty()) return

    Card {
        CardHeader {
            Text(text = stringResource(Res.string.workout_rest_title), variant = TextVariant.H3)
            Text(text = stringResource(Res.string.workout_rest_subtitle), variant = TextVariant.Muted)
        }
        CardContent {
            // Work vs rest — the same split Strava draws as moving time vs elapsed time.
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = KazTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.lg),
            ) {
                InlineStat(label = stringResource(Res.string.workout_rest_work), value = formatDuration(work))
                InlineStat(label = stringResource(Res.string.workout_rest_rest), value = formatDuration(rest))
                InlineStat(
                    label = stringResource(Res.string.workout_rest_avg),
                    value = formatDuration(rest / rests.size),
                )
            }

            // See TrendCard for why this is remembered and keyed on the source list.
            val data =
                remember(rests) {
                    rests.mapIndexed { index, seconds ->
                        Bars(
                            // Labelled by the set the rest follows: the gap after set 1, 2, …
                            label = (index + 1).toString(),
                            values =
                                listOf(
                                    Bars.Data(
                                        value = seconds.toDouble(),
                                        color = primaryBrush,
                                    ),
                                ),
                        )
                    }
                }
            ColumnChart(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                data = data,
                barProperties = BarProperties(spacing = 2.dp, thickness = 14.dp),
                indicatorProperties =
                    HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = chartText,
                        count = IndicatorCount.StepBased(30.0),
                        contentBuilder = { "${it.toInt()}s" },
                    ),
                labelProperties = LabelProperties(enabled = true, textStyle = chartText),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                gridProperties = GridProperties(enabled = true),
            )
        }
    }
}

@Composable
private fun InlineStat(
    label: String,
    value: String,
) {
    Column {
        Text(text = label, variant = TextVariant.Muted)
        Text(text = value, variant = TextVariant.P, maxLines = 1)
    }
}

/**
 * This session against the one before it. Nothing else on the screen answers
 * "am I getting better?" — every other card describes this workout in isolation.
 *
 * Renders nothing when there is no earlier session, rather than showing an empty comparison.
 */
@Composable
private fun ComparisonCard(
    workout: Workout,
    previous: Workout?,
) {
    if (previous == null || previous.reps == 0) return

    val deltaReps = workout.reps - previous.reps
    val deltaPercent = deltaReps.toDouble() / previous.reps
    val improved = deltaReps >= 0
    val accent = if (improved) KazTheme.colors.success else KazTheme.colors.destructive

    Card {
        CardHeader {
            Text(text = stringResource(Res.string.workout_comparison_title), variant = TextVariant.H3)
            // The name is a session label and may be blank, so the date carries the identity.
            Text(
                text =
                    previous.name
                        ?.takeIf { it.isNotBlank() }
                        ?.let { "$it · ${formatDayMonth(previous.startedAt)}" }
                        ?: formatDayMonth(previous.startedAt),
                variant = TextVariant.Muted,
            )
        }
        CardContent {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        Res.string.workout_comparison_reps_delta,
                        if (improved) "+" else "",
                        deltaReps,
                    ),
                    variant = TextVariant.H2,
                    color = accent,
                )
                InlineStat(
                    label = stringResource(Res.string.workout_comparison_change),
                    value = stringResource(
                        Res.string.workout_comparison_percent,
                        if (improved) "+" else "",
                        kotlin.math.round(deltaPercent * 100).toInt(),
                    ),
                )
                InlineStat(
                    label = stringResource(Res.string.workout_comparison_before),
                    value = stringResource(Res.string.reps_count, previous.reps),
                )
            }
        }
    }
}

/**
 * Seconds per rep across the whole workout. Unlike the other charts this is not per-set — it is
 * one point per rep, so the within-set slowdown and the reset after each rest show up as a
 * sawtooth. This is the chart that uses the data only AutoReps has.
 */
@Composable
private fun CadenceCard(
    reps: List<Rep>,
) {
    val colors = KazTheme.colors
    val primaryBrush = SolidColor(colors.primary)
    val chartText = KazTheme.typography.small.copy(color = colors.onMuted)

    val cadence = remember(reps) { repCadence(reps) }
    val average = remember(reps) { averageCadence(reps) }

    if (cadence.size < 2) return

    Card {
        CardHeader {
            Text(text = stringResource(Res.string.workout_cadence_title), variant = TextVariant.H3)
            Text(text = stringResource(Res.string.workout_cadence_subtitle), variant = TextVariant.Muted)
        }
        CardContent {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = KazTheme.spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.lg),
            ) {
                InlineStat(label = stringResource(Res.string.workout_cadence_avg), value = formatSeconds(average))
                InlineStat(
                    label = stringResource(Res.string.workout_cadence_fastest),
                    value = stringResource(Res.string.format_seconds_whole, cadence.min()),
                )
                InlineStat(
                    label = stringResource(Res.string.workout_cadence_slowest),
                    value = stringResource(Res.string.format_seconds_whole, cadence.max()),
                )
            }

            // See TrendCard for why this is remembered and keyed on the source list.
            val cadenceLabel = stringResource(Res.string.unit_sec_per_rep)
            val data =
                remember(cadence, cadenceLabel) {
                    listOf(
                        Line(
                            label = cadenceLabel,
                            values = cadence.map { it.toDouble() },
                            color = primaryBrush,
                            strokeAnimationSpec = tween(900, easing = EaseInOutCubic),
                            drawStyle = DrawStyle.Stroke(2.dp),
                        ),
                    )
                }
            LineChart(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                data = data,
                indicatorProperties =
                    HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = chartText,
                        count = IndicatorCount.StepBased(1.0),
                        contentBuilder = { "${it.toInt()}s" },
                    ),
                // One label per rep would be unreadable at ~150 reps, so the axis is bare.
                labelProperties = LabelProperties(enabled = false, textStyle = chartText),
                labelHelperProperties = LabelHelperProperties(enabled = false),
                gridProperties = GridProperties(enabled = true),
            )
        }
    }
}

// ─── States ───────────────────────────────────────────────────

@Composable
private fun LoadingBody() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = KazTheme.spacing.lg,
                    end = KazTheme.spacing.lg,
                    top = KazTheme.spacing.lg,
                ),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.md),
    ) {
        Skeleton(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            shape = KazTheme.shapes.lg,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm)) {
            Skeleton(
                modifier = Modifier.weight(1f).height(88.dp),
                shape = KazTheme.shapes.lg,
            )
            Skeleton(
                modifier = Modifier.weight(1f).height(88.dp),
                shape = KazTheme.shapes.lg,
            )
        }
        Skeleton(
            modifier = Modifier.fillMaxWidth().height(280.dp),
            shape = KazTheme.shapes.lg,
        )
    }
}

@Composable
private fun EmptyDataCard() {
    Card {
        Text(text = stringResource(Res.string.workout_empty_title), variant = TextVariant.H3)
        Text(
            text = stringResource(Res.string.workout_empty_hint),
            variant = TextVariant.Muted,
        )
    }
}

// ─── Formatting ───────────────────────────────────────────────

/** [Workout.duration] is seconds; renders as `m:ss min`. */
@Composable
private fun formatDuration(duration: Int): String {
    val minutes = duration / 60
    val seconds = duration % 60
    return stringResource(Res.string.format_duration, minutes, seconds.toString().padStart(2, '0'))
}

/** `2.4` renders as `2,4s` (de) / `2.4s` (en); null as `–`. */
@Composable
private fun formatSeconds(seconds: Double?): String {
    if (seconds == null) return "–"
    val tenths = kotlin.math.round(seconds * 10).toInt()
    return stringResource(Res.string.format_seconds_short, tenths / 10, tenths % 10)
}

/** `2026-08-22T18:30:00` renders as `22.08.` (de) / `08/22` (en); falls back to the raw value. */
@Composable
private fun formatDayMonth(startedAt: String): String {
    val date = startedAt.substringBefore('T').split('-')
    if (date.size < 3) return startedAt
    return stringResource(Res.string.format_date_day_month, date[2], date[1])
}

/** How far the last set fell below the best, e.g. `-75%`. `–` when there is no second set. */
private fun formatDropOff(fraction: Double?): String =
    if (fraction == null) "–" else "-${kotlin.math.round(fraction * 100).toInt()}%"
