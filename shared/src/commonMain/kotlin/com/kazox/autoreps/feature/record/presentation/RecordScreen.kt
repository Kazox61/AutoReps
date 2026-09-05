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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.kazox.autoreps.core.presentation.KeepScreenOn
import com.kazox.autoreps.core.presentation.ObserveAsEvents
import com.kazox.autoreps.feature.record.domain.RepPhase
import com.kazox.autoreps.feature.record.presentation.camera.PoseCameraView
import com.kazox.autoreps.feature.record.presentation.camera.PoseFrame
import com.kazox.autoreps.feature.record.presentation.camera.rememberCameraPermissionController
import com.kazox.ui.components.button.Button
import com.kazox.ui.components.button.ButtonSize
import com.kazox.ui.components.button.ButtonVariant
import com.kazox.ui.components.icon.Icon
import com.kazox.ui.components.icon.KazIcons
import com.kazox.ui.components.scaffold.Scaffold
import com.kazox.ui.components.text.Text
import com.kazox.ui.components.text.TextVariant
import com.kazox.ui.components.topappbar.TopAppBar
import com.kazox.ui.foundation.KazTheme
import org.koin.compose.viewmodel.koinViewModel

/** Fixed size — the preview is for checking your framing, not for watching yourself. */
private val CameraWidth = 108.dp

// Position names for the status line and the diagnostics readout, which agree because they share
// these. Indexed to match RepDetector.diagnose's [start, end] order.
private const val TopLabel = "Oben"
private const val BottomLabel = "Unten"
private val PositionLabels = listOf(TopLabel, BottomLabel)

@Composable
fun RecordRoot(
    onBack: () -> Unit,
    onWorkoutSaved: (workoutId: Int) -> Unit,
    viewModel: RecordViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissions = rememberCameraPermissionController()

    // The workout exists by the time this fires, so the naming screen can be reached by id.
    // A session with no reps was never written, so there is nothing to name — just leave.
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
        onBack = onBack,
    )
}

@Composable
fun RecordScreen(
    state: RecordState,
    onAction: (RecordAction) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = "Liegestütze",
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                        label = "Zurück",
                    ) {
                        Icon(imageVector = KazIcons.ArrowLeft, contentDescription = null)
                    }
                },
                actions = {
                    Button(
                        onClick = { onAction(RecordAction.ToggleDiagnostics) },
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Icon,
                        label = "Erkennung anzeigen",
                    ) {
                        Icon(imageVector = KazIcons.Eye, contentDescription = null)
                    }
                },
            )
        },
    ) {
        if (!state.hasCameraPermission) {
            PermissionBody(denied = state.permissionDenied)
            return@Scaffold
        }

        // Push-ups are done away from the phone, so the whole screen is untouched input-wise —
        // exactly what the idle timer reads as "gone". Held for the screen rather than only
        // while recording: getting into position takes long enough to sleep through.
        KeepScreenOn()

        // A Column rather than aligned children in a Box: the preview and the counter must never
        // be able to land on top of each other.
        Column(
            modifier = Modifier.fillMaxSize().padding(KazTheme.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                FramingPreview(
                    visible = !state.isPersonVisible,
                    onFrame = { onAction(RecordAction.FrameAnalysed(it)) },
                )
            }

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
                if (state.showDiagnostics) {
                    DiagnosticsReadout(state)
                }
                Text(
                    text = state.saveError ?: statusLabel(state),
                    variant = TextVariant.Muted,
                    // Unspecified leaves the variant's own colour alone, which is the normal case.
                    color = if (state.saveError != null) KazTheme.colors.destructive else Color.Unspecified,
                    textAlign = TextAlign.Center,
                )
                // A failed save keeps the reps in the view model, so the same button retries
                // rather than dropping back to "Starten" — which would wipe the set.
                val retrying = state.saveError != null
                Button(
                    text =
                        when {
                            retrying -> "Erneut speichern"
                            state.isRecording -> "Fertig"
                            else -> "Starten"
                        },
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
                    variant =
                        if (retrying || state.isActive) ButtonVariant.Secondary else ButtonVariant.Default,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * The camera preview, shown only while nobody is detected.
 *
 * Always composed, never conditionally: [PoseCameraView] owns the capture session, and adding or
 * removing it would tear the camera down and rebuild it every time tracking flickered. Visibility
 * is opacity only, so the frames — and therefore detection — never stop.
 */
@Composable
private fun FramingPreview(
    visible: Boolean,
    onFrame: (PoseFrame) -> Unit,
) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "preview")

    Box(
        modifier =
            Modifier
                .width(CameraWidth)
                // 9:16 matches what both platforms deliver from the front camera under the .high
                // preset (1080x1920). At 3:4 the preview cropped a third of the frame vertically,
                // cutting off the head or feet of a full-body framing — the overlay mapped it
                // correctly, straight off screen.
                .aspectRatio(9f / 16f)
                .alpha(alpha)
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

/**
 * Which constraints currently hold, and what they measure.
 *
 * The thresholds were carried over untested, and a rep that does not count looks identical to a
 * camera that cannot see — this makes the difference visible without a debugger attached.
 */
@Composable
private fun DiagnosticsReadout(state: RecordState) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(KazTheme.shapes.md)
                .background(KazTheme.colors.muted)
                .padding(KazTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        state.diagnostics.forEachIndexed { index, position ->
            Text(
                text = "${PositionLabels[index]}${if (position.matches) " ✓" else ""}",
                variant = TextVariant.Small,
                color = if (position.matches) KazTheme.colors.success else KazTheme.colors.onMuted,
            )
            position.readings.forEach { reading ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = reading.label,
                        variant = TextVariant.Small,
                        color = KazTheme.colors.onMuted,
                    )
                    Text(
                        text = if (reading.measurable) reading.value else "nicht sichtbar",
                        variant = TextVariant.Small,
                        color =
                            when {
                                !reading.measurable -> KazTheme.colors.destructive
                                reading.satisfied -> KazTheme.colors.success
                                else -> KazTheme.colors.destructive
                            },
                    )
                }
            }
        }
    }
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
