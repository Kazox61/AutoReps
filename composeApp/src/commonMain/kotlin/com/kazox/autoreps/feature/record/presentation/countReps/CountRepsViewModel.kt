package com.kazox.autoreps.feature.record.presentation.countReps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.PushupExercise
import com.kazox.autoreps.core.domain.util.formatDuration
import com.kazox.autoreps.feature.workout.domain.model.Rep
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class CountRepsViewModel() : ViewModel() {
    private val _state = MutableStateFlow(CountRepsState())
    val state = _state.asStateFlow()

    private val exercise = PushupExercise()

    private var duration = 0

    init {
        viewModelScope.launch {
            exercise.onRep {
                _state.update { current ->
                    val newSetId =
                        if (current.duration - current.lastRepTimeStamp > 5)
                            current.setId + 1
                        else
                            current.setId

                    current.copy(
                        repCount = current.repCount + 1,
                        lastRepTimeStamp = duration,
                        setId = newSetId,
                        reps = current.reps + Rep(
                            timestamp = duration,
                            setId = newSetId
                        )
                    )
                }
            }
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)

                duration++
                _state.update {
                    it.copy(
                        duration = duration,
                        formattedTime = formatDuration(duration)
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    fun onEvent(event: CountRepsEvent) {
        when (event) {
            is CountRepsEvent.StartWorkout -> {
                _state.update {
                    it.copy(
                        startedDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                        repCount = 0,
                        duration = 0
                    )
                }
                duration = 0
                startTimer()
            }
            is CountRepsEvent.PoseDetected -> {
                exercise.process(event.pose)
            }
        }
    }
}