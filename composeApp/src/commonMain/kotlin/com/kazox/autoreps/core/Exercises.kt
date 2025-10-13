package com.kazox.autoreps.core


enum class ExerciseCategory {
    UPPER_BODY,
    LOWER_BODY,
    CORE,
    FULL_BODY,
    ARMS,
    SHOULDERS,
    CHEST,
    BACK,
    LEGS,
    GLUTES,
    CARDIO
}

data class ExerciseInfo(
    val name: String,
    val categories: List<ExerciseCategory>,
    val counter: () -> RepExercise
)

val exerciseList = listOf(
    ExerciseInfo(
        name = "Push-ups",
        categories = listOf(ExerciseCategory.UPPER_BODY, ExerciseCategory.CHEST, ExerciseCategory.ARMS, ExerciseCategory.SHOULDERS, ExerciseCategory.CORE),
        counter = { PushupExercise() }
    ),
    ExerciseInfo(
        name = "Squats",
        categories = listOf(ExerciseCategory.LOWER_BODY, ExerciseCategory.LEGS, ExerciseCategory.GLUTES, ExerciseCategory.CORE),
        counter = { PushupExercise() }
    ),
    ExerciseInfo(
        name = "Jumping Jacks",
        categories = listOf(ExerciseCategory.FULL_BODY, ExerciseCategory.CARDIO),
        counter = { PushupExercise() }
    ),
    ExerciseInfo(
        name = "Lunges",
        categories = listOf(ExerciseCategory.LOWER_BODY, ExerciseCategory.LEGS, ExerciseCategory.GLUTES),
        counter = { PushupExercise() }
    ),
    ExerciseInfo(
        name = "Sit-ups",
        categories = listOf(ExerciseCategory.CORE),
        counter = { PushupExercise() }
    ),
    ExerciseInfo(
        name = "Burpees",
        categories = listOf(ExerciseCategory.FULL_BODY, ExerciseCategory.CARDIO),
        counter = { PushupExercise() }
    )
)