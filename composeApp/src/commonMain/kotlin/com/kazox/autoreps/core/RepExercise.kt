package com.kazox.autoreps.core

enum class ExerciseState {
    START,
    END,
    TRANSITION,
    UNKNOWN
}

open class RepExercise(
    val startPosition: ExercisePosition,
    val endPosition: ExercisePosition
) {
    private var lastState: ExerciseState = ExerciseState.UNKNOWN
    private var wasInEndState: Boolean = false
    private var listeners: MutableList<() -> Unit> = mutableListOf()

    fun onRep(cb: () -> Unit): () -> Unit {
        listeners.add(cb)
        return {
            listeners = listeners.filter { it != cb }.toMutableList()
        }
    }

    private fun emitRep() {
        listeners.forEach { it() }
    }

    private fun updateReps(state: ExerciseState) {
        when (state) {
            ExerciseState.END -> {
                if (lastState == ExerciseState.START) {
                    wasInEndState = true
                }
            }

            ExerciseState.START -> {
                if (wasInEndState && lastState == ExerciseState.END) {
                    wasInEndState = false
                    lastState = state
                    emitRep()
                    return
                }
            }

            else -> {}
        }

        if (state != ExerciseState.TRANSITION) {
            lastState = state
        }
    }

    fun process(pose: Pose) {
        val currentState = when {
            startPosition.hasValidConstraints(pose) -> ExerciseState.START
            endPosition.hasValidConstraints(pose) -> ExerciseState.END
            else -> ExerciseState.TRANSITION
        }

        updateReps(currentState)
    }
}
