package com.kazox.autoreps.feature.history.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.resources.Res
import com.kazox.autoreps.resources.history_empty_hint
import com.kazox.autoreps.resources.history_empty_title
import com.kazox.autoreps.resources.history_title
import com.kazox.autoreps.resources.history_workout_fallback
import com.kazox.autoreps.resources.reps_count
import com.kazox.ui.components.card.Card
import com.kazox.ui.components.card.CardAnimation
import com.kazox.ui.components.icon.Icon
import com.kazox.ui.components.icon.IconSize
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.skeleton.Skeleton
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.components.topappbar.TopAppBar
import com.kazox.ui.foundation.KazTheme
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoot(
    bottomBar: @Composable () -> Unit,
    onWorkoutClick: (Workout) -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HistoryScreen(
        bottomBar = bottomBar,
        onWorkoutClick = onWorkoutClick,
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun HistoryScreen(
    bottomBar: @Composable () -> Unit,
    onWorkoutClick: (Workout) -> Unit,
    state: HistoryState,
    onAction: (HistoryAction) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = stringResource(Res.string.history_title)) },
        bottomBar = bottomBar,
        // Rows scroll behind the floating bar rather than stopping above it, staying visible
        // through the gaps around the capsule. In exchange the body owns its own bottom
        // spacing — see the contentPadding below.
        overlayBottomBar = true,
    ) { padding ->
        // Never Modifier.padding(padding): the slot is already inset for the top bar, so that
        // would double-count it. The bottom value is used as scroll padding instead, which is
        // what keeps the last row clear of the bar it scrolls under.
        val gutter = KazTheme.spacing.lg
        val bottomInset = padding.calculateBottomPadding() + gutter

        when {
            state.isLoading -> LoadingList(bottomInset = bottomInset)
            state.workouts.isEmpty() -> EmptyState(bottomInset = bottomInset)
            else ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            start = gutter,
                            end = gutter,
                            top = gutter,
                            bottom = bottomInset,
                        ),
                    verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
                ) {
                    items(state.workouts, key = { it.id }) { workout ->
                        WorkoutRow(
                            workout = workout,
                            onClick = { onWorkoutClick(workout) },
                        )
                    }
                }
        }
    }
}

// ─── Row ──────────────────────────────────────────────────────

@Composable
private fun WorkoutRow(
    workout: Workout,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        animation = CardAnimation.Press,
        label = workout.name ?: stringResource(Res.string.history_workout_fallback),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.xs),
            ) {
                Text(
                    text = workout.name?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.history_workout_fallback),
                    variant = TextVariant.Large,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formatStartedAt(workout.startedAt),
                    variant = TextVariant.Small,
                    color = KazTheme.colors.onMuted,
                    maxLines = 1,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(Res.string.reps_count, workout.reps),
                    variant = TextVariant.P,
                    maxLines = 1,
                )
                Text(
                    text = formatDuration(workout.duration),
                    variant = TextVariant.Small,
                    color = KazTheme.colors.onMuted,
                    maxLines = 1,
                )
            }

            Icon(
                imageVector = KazIcons.ChevronRight,
                contentDescription = null,
                tint = KazTheme.colors.onMuted,
                size = IconSize.Default,
            )
        }
    }
}

// ─── States ───────────────────────────────────────────────────

@Composable
private fun LoadingList(
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    start = KazTheme.spacing.lg,
                    end = KazTheme.spacing.lg,
                    top = KazTheme.spacing.lg,
                    bottom = bottomInset,
                ),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
    ) {
        repeat(6) {
            Skeleton(
                modifier = Modifier.fillMaxWidth().height(76.dp),
                shape = KazTheme.shapes.lg,
            )
        }
    }
}

@Composable
private fun EmptyState(
    bottomInset: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    start = KazTheme.spacing.xl,
                    end = KazTheme.spacing.xl,
                    top = KazTheme.spacing.xl,
                    bottom = bottomInset,
                ),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = KazIcons.List,
            contentDescription = null,
            tint = KazTheme.colors.onMuted,
            size = IconSize.Xl,
            modifier = Modifier.size(48.dp),
        )
        Text(text = stringResource(Res.string.history_empty_title), variant = TextVariant.Large)
        Text(
            text = stringResource(Res.string.history_empty_hint),
            variant = TextVariant.Muted,
        )
    }
}

// ─── Formatting ───────────────────────────────────────────────

/**
 * [Workout.startedAt] is stored as `LocalDateTime.toString()`, so it parses as ISO-8601. Falls
 * back to the raw value rather than throwing if a row was written in some other format.
 */
private fun formatStartedAt(startedAt: String): String =
    runCatching {
        val dt = LocalDateTime.parse(startedAt)
        val day = dt.day.toString().padStart(2, '0')
        val month = dt.monthNumber.toString().padStart(2, '0')
        val hour = dt.hour.toString().padStart(2, '0')
        val minute = dt.minute.toString().padStart(2, '0')
        "$day.$month.${dt.year}, $hour:$minute"
    }.getOrDefault(startedAt)

/** [Workout.duration] is assumed to be seconds — see the note in the review. */
private fun formatDuration(duration: Int): String {
    val minutes = duration / 60
    val seconds = duration % 60
    return "$minutes:${seconds.toString().padStart(2, '0')} min"
}
