package com.kazox.autoreps.feature.workout.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AddEditWorkoutViewModel(
    workoutId: Int?,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AddEditWorkoutState())
    val state = _state.asStateFlow()

    /** Emits once after a successful save so the screen can navigate back. */
    private val _saved = Channel<Unit>(Channel.BUFFERED)
    val saved = _saved.receiveAsFlow()

    init {
        if (workoutId == null) {
            // A new workout has no row yet — the Record flow creates those, so this screen
            // renders the name field only until then.
            _state.update { it.copy(isLoading = false) }
        } else {
            observeWorkout(workoutId)
            observeReps(workoutId)
            observePreviousWorkout(workoutId)
        }
    }

    fun onAction(action: AddEditWorkoutAction) {
        when (action) {
            is AddEditWorkoutAction.EnteredName ->
                _state.update { it.copy(name = action.name) }

            AddEditWorkoutAction.SaveWorkout -> save()
        }
    }

    private fun observeWorkout(id: Int) {
        viewModelScope.launch {
            workoutRepository.getWorkoutById(id).collect { workout ->
                _state.update { current ->
                    if (workout == null) {
                        // The row is gone — deleted from History while this screen was open, or
                        // the id was stale. Clearing isLoading matters: ignoring the null left
                        // the screen spinning on its skeleton forever with no way out but back.
                        current.copy(workout = null, isLoading = false)
                    } else {
                        current.copy(
                            workout = workout,
                            // Seed the field on the first emission only — later ones (e.g.
                            // triggered by our own save) must not clobber what the user typed.
                            name =
                                if (current.workout == null) {
                                    workout.name.orEmpty()
                                } else {
                                    current.name
                                },
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    /**
     * Watches for the workout before this one, so the screen can compare the two.
     *
     * Keyed on [Workout.startedAt], which never changes — so unlike the earlier name-based
     * version, editing the name cannot detach a session from its own history.
     */
    private fun observePreviousWorkout(id: Int) {
        viewModelScope.launch {
            workoutRepository
                .getWorkoutById(id)
                .map { it?.startedAt }
                .distinctUntilChanged()
                .flatMapLatest { startedAt ->
                    if (startedAt == null) flowOf(null) else workoutRepository.getPreviousWorkout(startedAt)
                }.collect { previous ->
                    _state.update { it.copy(previousWorkout = previous) }
                }
        }
    }

    private fun observeReps(id: Int) {
        viewModelScope.launch {
            workoutRepository.getRepsForWorkout(id).collect { reps ->
                _state.update { it.copy(reps = reps) }
            }
        }
    }

    private fun save() {
        val workout = state.value.workout ?: return
        val name = state.value.name
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }
            try {
                // Must be updateWorkout, not insertWorkout: re-inserting would hit SQLite's
                // REPLACE path, which deletes the row and cascades away every rep it owns.
                workoutRepository.updateWorkout(workout.copy(name = name.takeIf { it.isNotBlank() }))
                _state.update { it.copy(isSaving = false) }
                _saved.send(Unit)
            } catch (e: Exception) {
                // Without this the failed coroutine leaves isSaving stuck true: the save button
                // spins forever, stays disabled, and never navigates — with nothing on screen
                // to say why. Raw cause only — the screen composes the localized message.
                _state.update {
                    it.copy(isSaving = false, saveError = e.message ?: "")
                }
            }
        }
    }
}
