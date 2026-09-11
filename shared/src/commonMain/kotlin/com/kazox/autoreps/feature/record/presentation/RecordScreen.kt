package com.kazox.autoreps.feature.record.presentation

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.kazox.autoreps.core.presentation.KeepScreenOn
import com.kazox.autoreps.core.presentation.ObserveAsEvents
import com.kazox.autoreps.feature.record.domain.RepPhase
import com.kazox.autoreps.feature.record.presentation.camera.PoseCameraView
import com.kazox.autoreps.feature.record.presentation.camera.PoseFrame
import com.kazox.autoreps.feature.record.presentation.camera.rememberCameraPermissionController
import com.kazox.ui.components.alertdialog.AlertDialog
import com.kazox.ui.components.alertdialog.AlertDialogAction
import com.kazox.ui.components.alertdialog.AlertDialogActionVariant
import com.kazox.ui.components.alertdialog.AlertDialogCancel
import com.kazox.ui.components.alertdialog.AlertDialogFooter
import com.kazox.ui.components.alertdialog.AlertDialogHeader
import com.kazox.ui.components.button.Button
import com.kazox.ui.components.button.ButtonSize
import com.kazox.ui.components.button.ButtonVariant
import com.kazox.ui.components.icon.Icon
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.foundation.KazTheme
import org.koin.compose.viewmodel.koinViewModel

/** Fixed size — the preview is for checking your framing, not for watching yourself. */
private val CameraWidth = 108.dp

// Position names for the status line. The detector's [start, end] order.
private const val TopLabel = "Oben"
private const val BottomLabel = "Unten"

@Composable
fun RecordRoot(
    onBack: () -> Unit,
    onWorkoutSaved: (workoutId: Int) -> Unit,
    viewModel: RecordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissions = rememberCameraPermissionController()

    // The workout exists by the time this fires, so the naming screen can be reached by id.
    // A session with no reps — never started, emptied, or confirmed discarded — was never
    // written, so there is nothing to name: just leave.
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RecordEvent.Saved -> onWorkoutSaved(event.workoutId)
            RecordEvent.Discarded -> onBack()
        }
    }

    // Asking is a side effect, so it belongs in an effect rather than the composable body.
    LaunchedEffect(Unit) {
        if (permissions.isGranted) {
            viewModel.onAction(RecordAction.CameraPermissionResult(true))
        } else {
            permissions.request { granted ->
                viewModel.onAction(RecordAction.CameraPermissionResult(granted))
            }
        }
    }

    RecordScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
fun RecordScreen(
    state: RecordState,
    onAction: (RecordAction) -> Unit,
) {
    // Pure presentation — the view model only hears about an actual discard, so the dialog's
    // open/close needs no state of its own upstream.
    var confirmingDiscard by remember { mutableStateOf(false) }

    // There is no back button: the back gesture is the way out, on both platforms. While a set
    // is in progress — running, or reps sitting unsaved after a failed write — this handler
    // outranks NavDisplay's own and the gesture opens the discard dialog instead of popping.
    // Being disabled hands the gesture straight back, so a fresh screen still leaves without
    // ceremony. Saving gets no way out at all: the write is sub-second, and leaving mid-write
    // could produce a workout the user believes they threw away.
    val atStake = state.isActive || state.repCount > 0
    NavigationBackHandler(
        state = rememberNavigationEventState<NavigationEventInfo>(NavigationEventInfo.None),
        isBackEnabled = atStake && !state.isSaving,
        onBackCompleted = { confirmingDiscard = true },
    )

    Scaffold {
        if (!state.hasCameraPermission) {
            PermissionBody(denied = state.permissionDenied)
            return@Scaffold
        }

        // Push-ups are done away from the phone, so the whole screen is untouched input-wise —
        // exactly what the idle timer reads as "gone". Held for the screen rather than only
        // while recording: getting into position takes long enough to sleep through.
        KeepScreenOn()

        // A Box, not a column: the preview is deliberately an overlay that can sit on top of
        // the readout. It is there to be glanced at while getting into position, never a row
        // the workout has to make room for — which is what made the top look empty whenever
        // it was off.
        Box(
            modifier = Modifier.fillMaxSize().padding(KazTheme.spacing.lg),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.md),
                    ) {
                        Text(
                            text = formatElapsed(state.elapsedMillis),
                            style = TextStyle(fontSize = 56.sp, fontWeight = FontWeight.SemiBold),
                        )
                        RepCounter(
                            reps = state.repCount,
                            active = state.isRecording && state.isPersonVisible,
                        )
                        state.emom?.let { EmomProgressLine(it) }
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm),
                ) {
                    Text(
                        text = state.saveError ?: statusLabel(state),
                        // Lead, not Muted: this line is read from about a metre away, mid-set,
                        // so the standard helper size is illegible there.
                        variant = TextVariant.Lead,
                        // Unspecified leaves the variant's own colour alone, which is the normal case.
                        color = if (state.saveError != null) KazTheme.colors.destructive else Color.Unspecified,
                        textAlign = TextAlign.Center,
                    )
                    // A failed save keeps the reps in the view model, so the same button retries
                    // rather than dropping back to "Starten" — which would wipe the set.
                    val retrying = state.saveError != null
                    val actionLabel =
                        when {
                            retrying -> "Erneut speichern"
                            state.isRecording -> "Fertig"
                            else -> "Starten"
                        }
                    Button(
                        onClick = {
                            onAction(
                                when {
                                    retrying || state.isRecording -> RecordAction.FinishRecording
                                    else -> RecordAction.StartRecording
                                },
                            )
                        },
                        enabled = !state.isSaving,
                        loading = state.isSaving,
                        size = ButtonSize.Lg,
                        label = actionLabel,
                        variant =
                            when {
                                retrying -> ButtonVariant.Secondary
                                // Green for the finish tap and for as long as its write runs:
                                // dropping to plain primary the instant saving starts would read
                                // as the action bouncing off.
                                state.isRecording || (state.isSaving && state.repCount > 0) ->
                                    ButtonVariant.Success
                                else -> ButtonVariant.Default
                            },
                        // The one control on this screen, operated after a workout: a full-width,
                        // hand-tall target with text sized to be read from a metre away.
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Text(text = actionLabel, style = KazTheme.typography.large)
                    }
                }
            }

            // The toggle and the preview live in opposite corners: the eye is always reachable,
            // the preview only ever covers the edge of the readout, and neither crowds the
            // centered counter.
            Button(
                onClick = { onAction(RecordAction.TogglePreview) },
                variant = ButtonVariant.Ghost,
                size = ButtonSize.Icon,
                label = "Kamera anzeigen",
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Icon(imageVector = KazIcons.Eye, contentDescription = null)
            }

            FramingPreview(
                modifier = Modifier.align(Alignment.TopEnd),
                visible = state.showPreview,
                onFrame = { onAction(RecordAction.FrameAnalysed(it)) },
            )
        }
    }

    DiscardDialog(
        open = confirmingDiscard,
        onDismiss = { confirmingDiscard = false },
        onConfirm = {
            confirmingDiscard = false
            onAction(RecordAction.Discard)
        },
    )
}

/**
 * The camera preview, shown while the eye in the corner has it toggled on.
 *
 * Always composed, never conditionally: [PoseCameraView] owns the capture session, and adding or
 * removing it would tear the camera down and rebuild it every time the toggle flickered.
 *
 * Hiding is a cover, not opacity: the native camera surfaces (PreviewView, UIKitView) do not
 * follow Compose alpha, so an opaque lid fades in over the live view instead. The frames — and
 * therefore detection — never stop either way.
 */
@Composable
private fun FramingPreview(
    visible: Boolean,
    onFrame: (PoseFrame) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The cover starts fully closed, so a first composition with the toggle off — the normal
    // case — never shows the camera underneath.
    val coverAlpha by animateFloatAsState(if (visible) 0f else 1f, label = "preview-cover")

    Box(
        modifier =
            modifier
                .width(CameraWidth)
                // 9:16 matches what both platforms deliver from the front camera under the .high
                // preset (1080x1920). At 3:4 the preview cropped a third of the frame vertically,
                // cutting off the head or feet of a full-body framing — the overlay mapped it
                // correctly, straight off screen.
                .aspectRatio(9f / 16f)
                .clip(KazTheme.shapes.lg)
                .background(KazTheme.colors.muted),
    ) {
        PoseCameraView(
            modifier = Modifier.fillMaxSize(),
            // The skeleton is for checking the camera sees you, which is the only reason this
            // preview is on screen at all.
            showLandmarks = true,
            onFrame = onFrame,
        )

        // Screen-colored, so the covered window reads as "not there" rather than as a muted box.
        if (coverAlpha > 0f) {
            Box(
                modifier =
                    Modifier
                        .matchParentSize()
                        .alpha(coverAlpha)
                        .background(KazTheme.colors.background),
            )
        }
    }
}

/**
 * The discard confirmation. It names the loss, whatever kind it is: reps counted mid-set, or
 * reps a failed save was still holding — to the user they are the same thing.
 */
@Composable
private fun DiscardDialog(
    open: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        open = open,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        label = "Training abbrechen",
    ) {
        AlertDialogHeader(
            title = "Training abbrechen?",
            description = "Die gezählten Wiederholungen werden nicht gespeichert.",
        )
        AlertDialogFooter {
            // Not "Abbrechen" — that is the word for the thing being confirmed.
            AlertDialogCancel(onClick = onDismiss, text = "Weiter trainieren")
            AlertDialogAction(
                text = "Verwerfen",
                onClick = onConfirm,
                variant = AlertDialogActionVariant.Destructive,
            )
        }
    }
}

/** The count, in a circle that breathes while reps are actually being counted. */
@Composable
private fun RepCounter(
    reps: Int,
    active: Boolean,
) {
    val transition = rememberInfiniteTransition(label = "breathing")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier =
            Modifier
                .fillMaxWidth(0.62f)
                .aspectRatio(1f)
                .scale(if (active) pulse else 1f)
                .clip(CircleShape)
                .background(if (active) KazTheme.colors.primary else KazTheme.colors.muted),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = reps.toString(),
            color = if (active) KazTheme.colors.onPrimary else KazTheme.colors.onMuted,
            style = TextStyle(fontSize = 96.sp, fontWeight = FontWeight.Bold),
        )
    }
}

/**
 * The current round, under the counter.
 *
 * The round number is what tells you how much is left to do.
 */
@Composable
private fun EmomProgressLine(emom: EmomState) {
    Text(
        text = "Runde ${emom.round}",
        variant = TextVariant.Muted,
        textAlign = TextAlign.Center,
    )
}

private fun statusLabel(state: RecordState): String =
    when {
        // Framing stays the first thing said: the seconds are there precisely so the shot can
        // still be fixed, and "Mach dich bereit" would hide that it needs fixing.
        !state.isPersonVisible -> "Nicht ganz im Bild"
        !state.isRecording -> "Bereit"
        state.phase == RepPhase.START -> TopLabel
        state.phase == RepPhase.END -> BottomLabel
        else -> "Bewegung"
    }

private fun formatElapsed(elapsedMillis: Long): String {
    val totalSeconds = elapsedMillis / 1000
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}

@Composable
private fun PermissionBody(denied: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(KazTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(KazTheme.spacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "Kamera benötigt", variant = TextVariant.Large)
        Text(
            text =
                if (denied) {
                    "Ohne Kamerazugriff können Liegestütze nicht gezählt werden. Du kannst ihn in den Einstellungen erlauben."
                } else {
                    "AutoReps zählt deine Liegestütze über die Kamera. Es werden keine Videos gespeichert."
                },
            variant = TextVariant.Muted,
            textAlign = TextAlign.Center,
        )
    }
}
