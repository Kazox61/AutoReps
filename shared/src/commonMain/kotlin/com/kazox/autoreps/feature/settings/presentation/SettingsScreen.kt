package com.kazox.autoreps.feature.settings.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kazox.autoreps.core.domain.model.ThemeChoice
import com.kazox.autoreps.resources.Res
import com.kazox.autoreps.resources.action_cancel
import com.kazox.autoreps.resources.action_delete
import com.kazox.autoreps.resources.action_less
import com.kazox.autoreps.resources.action_more
import com.kazox.autoreps.resources.settings_daily_goal
import com.kazox.autoreps.resources.settings_daily_goal_hint
import com.kazox.autoreps.resources.settings_delete_all
import com.kazox.autoreps.resources.settings_delete_all_message
import com.kazox.autoreps.resources.settings_delete_all_title
import com.kazox.autoreps.resources.settings_emom
import com.kazox.autoreps.resources.settings_emom_hint
import com.kazox.autoreps.resources.settings_emom_interval
import com.kazox.autoreps.resources.settings_emom_interval_hint
import com.kazox.autoreps.resources.settings_emom_warning
import com.kazox.autoreps.resources.settings_emom_warning_hint
import com.kazox.autoreps.resources.settings_group_appearance
import com.kazox.autoreps.resources.settings_group_emom
import com.kazox.autoreps.resources.settings_group_goal
import com.kazox.autoreps.resources.settings_group_sound
import com.kazox.autoreps.resources.settings_group_training
import com.kazox.autoreps.resources.settings_rest_seconds
import com.kazox.autoreps.resources.settings_rest_seconds_hint
import com.kazox.autoreps.resources.settings_sound_per_rep
import com.kazox.autoreps.resources.settings_sound_per_rep_hint
import com.kazox.autoreps.resources.settings_theme
import com.kazox.autoreps.resources.settings_title
import com.kazox.autoreps.resources.settings_warning_off
import com.kazox.autoreps.resources.theme_dark
import com.kazox.autoreps.resources.theme_light
import com.kazox.autoreps.resources.theme_system
import com.kazox.ui.components.alertdialog.AlertDialog
import com.kazox.ui.components.alertdialog.AlertDialogAction
import com.kazox.ui.components.alertdialog.AlertDialogActionVariant
import com.kazox.ui.components.alertdialog.AlertDialogCancel
import com.kazox.ui.components.alertdialog.AlertDialogFooter
import com.kazox.ui.components.alertdialog.AlertDialogHeader
import com.kazox.ui.components.button.Button
import com.kazox.ui.components.button.ButtonVariant
import com.kazox.ui.components.card.Card
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.separator.Separator
import com.kazox.ui.components.stepper.Stepper
import com.kazox.ui.components.stepper.StepperVariant
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.components.toggle.Toggle
import com.kazox.ui.components.togglegroup.ToggleGroup
import com.kazox.ui.components.togglegroup.ToggleGroupItem
import com.kazox.ui.components.topappbar.TopAppBar
import com.kazox.ui.foundation.KazTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoot(
    bottomBar: @Composable () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        bottomBar = bottomBar,
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun SettingsScreen(
    bottomBar: @Composable () -> Unit,
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = stringResource(Res.string.settings_title)) },
        bottomBar = bottomBar,
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
            verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.lg),
        ) {
            SettingsGroup(stringResource(Res.string.settings_group_goal)) {
                StepperRow(
                    label = stringResource(Res.string.settings_daily_goal),
                    hint = stringResource(Res.string.settings_daily_goal_hint),
                    value = state.settings.dailyGoal,
                    range = 10..500,
                    step = 5,
                    onValue = { onAction(SettingsAction.DailyGoalChanged(it)) },
                )
            }

            SettingsGroup(stringResource(Res.string.settings_group_training)) {
                StepperRow(
                    label = stringResource(Res.string.settings_rest_seconds),
                    hint = stringResource(Res.string.settings_rest_seconds_hint),
                    value = state.settings.restSeconds,
                    range = 5..60,
                    step = 5,
                    onValue = { onAction(SettingsAction.RestSecondsChanged(it)) },
                )
            }

            SettingsGroup(stringResource(Res.string.settings_group_emom)) {
                SwitchRow(
                    label = stringResource(Res.string.settings_emom),
                    hint = stringResource(Res.string.settings_emom_hint),
                    checked = state.settings.emomEnabled,
                    onCheckedChange = { onAction(SettingsAction.EmomEnabled(it)) },
                )
                // The rest is meaningless while EMOM is off, so it is not there to be read.
                AnimatedVisibility(
                    visible = state.settings.emomEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Separator()
                        StepperRow(
                            label = stringResource(Res.string.settings_emom_interval),
                            hint = stringResource(Res.string.settings_emom_interval_hint),
                            value = state.settings.emomIntervalSeconds,
                            range = 30..300,
                            step = 15,
                            onValue = { onAction(SettingsAction.EmomIntervalChanged(it)) },
                        )
                        Separator()
                        ChoiceRow(
                            label = stringResource(Res.string.settings_emom_warning),
                            // Three short tones then a different one at zero: the pitch change
                            // carries the "go", so the count does not have to be followed.
                            hint = stringResource(Res.string.settings_emom_warning_hint),
                            options = warningOptionLabels(),
                            selectedIndex = WARNING_CHOICES.indexOfFirst { it == state.settings.emomWarningSeconds }
                                .coerceAtLeast(0),
                            onSelect = { onAction(SettingsAction.EmomWarningChanged(WARNING_CHOICES[it])) },
                        )
                    }
                }
            }

            SettingsGroup(stringResource(Res.string.settings_group_sound)) {
                SwitchRow(
                    label = stringResource(Res.string.settings_sound_per_rep),
                    hint = stringResource(Res.string.settings_sound_per_rep_hint),
                    checked = state.settings.soundPerRep,
                    onCheckedChange = { onAction(SettingsAction.SoundPerRepChanged(it)) },
                )
            }

            SettingsGroup(stringResource(Res.string.settings_group_appearance)) {
                ChoiceRow(
                    label = stringResource(Res.string.settings_theme),
                    hint = null,
                    options = themeChoiceLabels(),
                    selectedIndex = state.settings.theme.ordinal,
                    onSelect = { onAction(SettingsAction.ThemeChanged(ThemeChoice.entries[it])) },
                )
            }

            Button(
                text = stringResource(Res.string.settings_delete_all),
                onClick = { onAction(SettingsAction.DeleteAllData) },
                variant = ButtonVariant.Destructive,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        DeleteAllDataDialog(
            open = state.confirmingDelete,
            isDeleting = state.isDeleting,
            onConfirm = { onAction(SettingsAction.ConfirmDeleteAllData) },
            onDismiss = { onAction(SettingsAction.DismissDeleteAllData) },
        )
    }
}

/**
 * The one irreversible action in the app, so it asks first — and names both halves of what it
 * takes, since "alle Daten" covers the settings on this very screen as well as the workouts.
 */
@Composable
private fun DeleteAllDataDialog(
    open: Boolean,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        open = open,
        // Dismissing mid-delete would leave the dialog's own buttons behind a wipe still running.
        onDismiss = { if (!isDeleting) onDismiss() },
        onConfirm = onConfirm,
        label = stringResource(Res.string.settings_delete_all),
    ) {
        AlertDialogHeader(
            title = stringResource(Res.string.settings_delete_all_title),
            description = stringResource(Res.string.settings_delete_all_message),
        )
        AlertDialogFooter {
            AlertDialogCancel(onClick = onDismiss, text = stringResource(Res.string.action_cancel))
            AlertDialogAction(
                text = stringResource(Res.string.action_delete),
                onClick = onConfirm,
                variant = AlertDialogActionVariant.Destructive,
            )
        }
    }
}

/** Localized labels for the theme choices, ordered to match [ThemeChoice.entries]. */
@Composable
private fun themeChoiceLabels(): List<String> =
    listOf(
        stringResource(Res.string.theme_light),
        stringResource(Res.string.theme_dark),
        stringResource(Res.string.theme_system),
    )

/** Countdown choices in seconds, ordered to match [themeChoiceLabels]' "Off" handling. */
private val WARNING_CHOICES = listOf(0, 3, 5)

@Composable
private fun warningOptionLabels(): List<String> =
    WARNING_CHOICES.map { seconds ->
        if (seconds == 0) stringResource(Res.string.settings_warning_off) else "${seconds} s"
    }

// ─── Rows ─────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.xs)) {
        Text(text = title, variant = TextVariant.Muted)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column { content() }
        }
    }
}

/**
 * Label and hint on the left, a compact stepper on the right.
 *
 * The value carries no unit: at any width that leaves room for the label, "100 Wdh." wrapped onto
 * two lines inside the stepper. The unit lives in the hint instead, where it has the whole row.
 */
@Composable
private fun StepperRow(
    label: String,
    hint: String?,
    value: Int,
    range: IntRange,
    step: Int,
    onValue: (Int) -> Unit,
    formatValue: (Int) -> String = { it.toString() },
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = label, variant = TextVariant.P)
            if (hint != null) {
                Text(text = hint, variant = TextVariant.Small, color = KazTheme.colors.onMuted)
            }
        }
        Stepper(
            value = value,
            onValueChange = onValue,
            min = range.first,
            max = range.last,
            step = step,
            variant = StepperVariant.Outline,
            label = label,
            valueFormatter = formatValue,
            decreaseDescription = stringResource(Res.string.action_less),
            increaseDescription = stringResource(Res.string.action_more),
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    hint: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = label, variant = TextVariant.P)
            if (hint != null) {
                Text(text = hint, variant = TextVariant.Small, color = KazTheme.colors.onMuted)
            }
        }
        Toggle(checked = checked, onCheckedChange = onCheckedChange, label = label)
    }
}

/** Options sit under the label rather than beside it — three chips never fit next to a sentence. */
@Composable
private fun ChoiceRow(
    label: String,
    hint: String?,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(KazTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
    ) {
        Column {
            Text(text = label, variant = TextVariant.P)
            if (hint != null) {
                Text(text = hint, variant = TextVariant.Small, color = KazTheme.colors.onMuted)
            }
        }
        ToggleGroup {
            options.forEachIndexed { index, option ->
                ToggleGroupItem(
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    label = option,
                ) { Text(text = option, variant = TextVariant.Small) }
            }
        }
    }
}
