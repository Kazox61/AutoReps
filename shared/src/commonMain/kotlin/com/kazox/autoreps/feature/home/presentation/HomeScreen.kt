package com.kazox.autoreps.feature.home.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.resources.Res
import com.kazox.autoreps.resources.home_goal_reached
import com.kazox.autoreps.resources.home_heatmap_less
import com.kazox.autoreps.resources.home_heatmap_more
import com.kazox.autoreps.resources.home_recent
import com.kazox.autoreps.resources.home_reps_to_go
import com.kazox.autoreps.resources.home_stat_best
import com.kazox.autoreps.resources.home_stat_total
import com.kazox.autoreps.resources.home_stat_week
import com.kazox.autoreps.resources.home_streak
import com.kazox.autoreps.resources.home_streak_days
import com.kazox.autoreps.resources.home_today
import com.kazox.autoreps.resources.home_today_progress
import com.kazox.autoreps.resources.month_april
import com.kazox.autoreps.resources.month_august
import com.kazox.autoreps.resources.month_december
import com.kazox.autoreps.resources.month_february
import com.kazox.autoreps.resources.month_january
import com.kazox.autoreps.resources.month_july
import com.kazox.autoreps.resources.month_june
import com.kazox.autoreps.resources.month_march
import com.kazox.autoreps.resources.month_may
import com.kazox.autoreps.resources.month_november
import com.kazox.autoreps.resources.month_october
import com.kazox.autoreps.resources.month_september
import com.kazox.autoreps.resources.reps_count
import com.kazox.autoreps.resources.weekday_1
import com.kazox.autoreps.resources.weekday_2
import com.kazox.autoreps.resources.weekday_3
import com.kazox.autoreps.resources.weekday_4
import com.kazox.autoreps.resources.weekday_5
import com.kazox.autoreps.resources.weekday_6
import com.kazox.autoreps.resources.weekday_7
import com.kazox.ui.components.card.Card
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.separator.Separator
import com.kazox.ui.components.skeleton.Skeleton
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.foundation.KazTheme
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max

@Composable
fun HomeRoot(
    bottomBar: @Composable () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreen(
        bottomBar = bottomBar,
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun HomeScreen(
    bottomBar: @Composable () -> Unit,
    state: HomeState,
    onAction: (HomeAction) -> Unit,
) {
    Scaffold(
        bottomBar = bottomBar,
        // Cards pass behind the floating bar rather than stopping above it.
        overlayBottomBar = true,
    ) { padding ->
        val gutter = KazTheme.spacing.lg
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = gutter,
                        end = gutter,
                        top = gutter,
                        bottom = padding.calculateBottomPadding() + gutter,
                    ),
            verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.md),
        ) {
            val today = state.today
            if (state.isLoading || today == null) {
                repeat(3) {
                    Skeleton(modifier = Modifier.fillMaxWidth().height(120.dp), shape = KazTheme.shapes.lg)
                }
                return@Column
            }

            TodayCard(state)
            StreakCard(state)
            StatRow(state)
            MonthCard(state, today)
            RecentCard(state)
        }
    }
}

// ─── Today ────────────────────────────────────────────────────

@Composable
private fun TodayCard(state: HomeState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                GoalRing(
                    progress = state.todayReps.toFloat() / state.dailyGoal,
                    strokeWidth = 10.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                Text(
                    text = state.todayReps.toString(),
                    style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.xs)) {
                Text(text = stringResource(Res.string.home_today), variant = TextVariant.Muted)
                Text(text = stringResource(Res.string.home_today_progress, state.todayReps, state.dailyGoal), variant = TextVariant.H3)
                val remaining = max(0, state.dailyGoal - state.todayReps)
                Text(
                    text =
                        if (remaining == 0) {
                            stringResource(Res.string.home_goal_reached)
                        } else {
                            pluralStringResource(Res.plurals.home_reps_to_go, remaining, remaining)
                        },
                    variant = TextVariant.Small,
                    color = if (remaining == 0) KazTheme.colors.success else KazTheme.colors.onMuted,
                )
            }
        }
    }
}

// ─── Streak ───────────────────────────────────────────────────

@Composable
private fun StreakCard(state: HomeState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.xs),
        ) {
            Text(text = stringResource(Res.string.home_streak), variant = TextVariant.Muted)
            Text(
                text = state.streak.toString(),
                style = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = pluralStringResource(Res.plurals.home_streak_days, state.streak),
                variant = TextVariant.Large,
            )
        }
    }
}

// ─── Stats ────────────────────────────────────────────────────

@Composable
private fun StatRow(state: HomeState) {
    Row(horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm)) {
        StatTile(stringResource(Res.string.home_stat_week), formatCount(state.weekTotal), Modifier.weight(1f))
        StatTile(stringResource(Res.string.home_stat_best), formatCount(state.bestDay), Modifier.weight(1f))
        StatTile(stringResource(Res.string.home_stat_total), formatCount(state.lifetimeReps), Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier, label = label) {
        Column(Modifier.padding(KazTheme.spacing.md)) {
            Text(text = label, variant = TextVariant.Muted, maxLines = 1)
            // Large, not H3: three tiles across leave about 90dp, and H3 hard-clipped a
            // four-digit lifetime total to three digits with no ellipsis to show it had.
            Text(text = value, variant = TextVariant.Large, maxLines = 1)
        }
    }
}

// ─── Month heatmap ────────────────────────────────────────────

@Composable
private fun MonthCard(
    state: HomeState,
    today: LocalDate,
) {
    val firstOfMonth = remember(today) { LocalDate(today.year, today.month, 1) }
    val daysInMonth =
        remember(firstOfMonth) {
            firstOfMonth.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1)).day
        }
    val leadingBlanks = firstOfMonth.dayOfWeek.ordinal // Monday = 0

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(KazTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
        ) {
            Text(
                text = "${monthNames()[today.monthNumber - 1]} ${today.year}",
                variant = TextVariant.H3,
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayInitials().forEach {
                    Text(
                        text = it,
                        variant = TextVariant.Small,
                        color = KazTheme.colors.onMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            var day = 1
            while (day <= daysInMonth) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    repeat(7) { column ->
                        val blank = day == 1 && column < leadingBlanks
                        if (blank || day > daysInMonth) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            HeatmapCell(
                                date = LocalDate(today.year, today.month, day),
                                reps = state.repsByDate[LocalDate(today.year, today.month, day)] ?: 0,
                                goal = state.dailyGoal,
                                today = today,
                                modifier = Modifier.weight(1f),
                            )
                            day++
                        }
                    }
                }
            }

            HeatmapLegend()
        }
    }
}

@Composable
private fun HeatmapCell(
    date: LocalDate,
    reps: Int,
    goal: Int,
    today: LocalDate,
    modifier: Modifier,
) {
    val isFuture = date > today
    // Opacity carries the amount. A three-level scale made 10 reps and 99 reps identical, and
    // painting the near-miss in the error colour read as "you failed" on a day that went well.
    val intensity = (reps.toFloat() / goal).coerceIn(0f, 1f)
    val alpha = HEATMAP_MIN_ALPHA + (1f - HEATMAP_MIN_ALPHA) * intensity

    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clip(KazTheme.shapes.sm)
                .background(
                    when {
                        isFuture -> Color.Transparent
                        reps == 0 -> KazTheme.colors.muted
                        else -> KazTheme.colors.primary.copy(alpha = alpha)
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = date.day.toString(),
            variant = TextVariant.Small,
            // Keyed to the computed alpha, not the intensity: the fill darkens faster than the
            // raw ratio, and a fixed intensity threshold left mid-range days unreadable.
            color =
                when {
                    isFuture -> KazTheme.colors.onMuted.copy(alpha = 0.4f)
                    reps > 0 && alpha > 0.5f -> KazTheme.colors.onPrimary
                    else -> KazTheme.colors.onMuted
                },
            style = if (date == today) TextStyle(fontWeight = FontWeight.Bold) else TextStyle.Default,
        )
    }
}

@Composable
private fun HeatmapLegend() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = stringResource(Res.string.home_heatmap_less), variant = TextVariant.Small, color = KazTheme.colors.onMuted)
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { level ->
            Box(
                Modifier
                    .size(12.dp)
                    .clip(KazTheme.shapes.sm)
                    .background(
                        if (level == 0f) {
                            KazTheme.colors.muted
                        } else {
                            KazTheme.colors.primary.copy(
                                alpha = HEATMAP_MIN_ALPHA + (1f - HEATMAP_MIN_ALPHA) * level,
                            )
                        },
                    ),
            )
        }
        Text(text = stringResource(Res.string.home_heatmap_more), variant = TextVariant.Small, color = KazTheme.colors.onMuted)
    }
}

// ─── Recent ───────────────────────────────────────────────────

@Composable
private fun RecentCard(state: HomeState) {
    if (state.recentWorkouts.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(KazTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
        ) {
            Text(text = stringResource(Res.string.home_recent), variant = TextVariant.H3)
            state.recentWorkouts.forEachIndexed { index, workout ->
                if (index > 0) Separator()
                RecentRow(workout)
            }
        }
    }
}

@Composable
private fun RecentRow(workout: Workout) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = formatDate(workout.startedAt), variant = TextVariant.P)
        Text(text = stringResource(Res.string.reps_count, workout.reps), variant = TextVariant.P)
    }
}

// ─── Shared ───────────────────────────────────────────────────

@Composable
private fun GoalRing(
    progress: Float,
    strokeWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val track = KazTheme.colors.muted
    val fill = KazTheme.colors.primary
    Canvas(modifier) {
        val stroke = strokeWidth.toPx()
        val inset = stroke / 2
        val diameter = size.minDimension - stroke
        drawArc(
            color = track,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(inset, inset),
            size = Size(diameter, diameter),
        )
        drawArc(
            color = fill,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            topLeft = Offset(inset, inset),
            size = Size(diameter, diameter),
        )
    }
}

/**
 * Abbreviates counts that outgrow a stat tile.
 *
 * Lifetime reps reach five digits within a few months, and the tiles are a third of the screen
 * wide — so past ten thousand it becomes `12,3k` rather than being silently truncated.
 */
private fun formatCount(value: Int): String =
    if (value < 10_000) {
        value.toString()
    } else {
        val tenths = (value / 100.0).let { kotlin.math.round(it).toInt() }
        "${tenths / 10},${tenths % 10}k"
    }

/** Lowest fill a day with any reps gets, so one rep is still visibly different from none. */
private const val HEATMAP_MIN_ALPHA = 0.25f

@Composable
private fun weekdayInitials(): List<String> =
    listOf(
        stringResource(Res.string.weekday_1),
        stringResource(Res.string.weekday_2),
        stringResource(Res.string.weekday_3),
        stringResource(Res.string.weekday_4),
        stringResource(Res.string.weekday_5),
        stringResource(Res.string.weekday_6),
        stringResource(Res.string.weekday_7),
    )

@Composable
private fun monthNames(): List<String> =
    listOf(
        stringResource(Res.string.month_january),
        stringResource(Res.string.month_february),
        stringResource(Res.string.month_march),
        stringResource(Res.string.month_april),
        stringResource(Res.string.month_may),
        stringResource(Res.string.month_june),
        stringResource(Res.string.month_july),
        stringResource(Res.string.month_august),
        stringResource(Res.string.month_september),
        stringResource(Res.string.month_october),
        stringResource(Res.string.month_november),
        stringResource(Res.string.month_december),
    )

/** `2026-08-27T18:30:00` renders as `27. August`; falls back to the raw value. */
@Composable
private fun formatDate(startedAt: String): String {
    val parsed = runCatching { LocalDate.parse(startedAt.substringBefore('T')) }.getOrNull()
        ?: return startedAt
    return "${parsed.day}. ${monthNames()[parsed.monthNumber - 1]}"
}
