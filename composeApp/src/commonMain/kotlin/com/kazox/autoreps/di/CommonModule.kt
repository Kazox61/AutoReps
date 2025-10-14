package com.kazox.autoreps.di

import com.kazox.autoreps.MainViewModel
import com.kazox.autoreps.feature.record.presentation.countReps.CountRepsViewModel
import com.kazox.autoreps.feature.workout.data.repository.WorkoutRepositoryImpl
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository
import com.kazox.autoreps.feature.workout.domain.useCase.AddReps
import com.kazox.autoreps.feature.workout.domain.useCase.AddWorkout
import com.kazox.autoreps.feature.workout.domain.useCase.DeleteWorkout
import com.kazox.autoreps.feature.workout.domain.useCase.GetCurrentStreak
import com.kazox.autoreps.feature.workout.domain.useCase.GetRepsForWorkout
import com.kazox.autoreps.feature.workout.domain.useCase.GetTodayReps
import com.kazox.autoreps.feature.workout.domain.useCase.GetTotalReps
import com.kazox.autoreps.feature.workout.domain.useCase.GetWorkouts
import com.kazox.autoreps.feature.workout.domain.useCase.WorkoutUseCases
import com.kazox.autoreps.feature.workout.presentation.addEditWorkout.AddEditViewModel
import com.kazox.autoreps.feature.workout.presentation.workouts.WorkoutsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    singleOf(::WorkoutRepositoryImpl).bind<WorkoutRepository>()
    single {
        WorkoutUseCases(
            addWorkout = AddWorkout(get()),
            deleteWorkout = DeleteWorkout(get()),
            addReps = AddReps(get()),
            getWorkouts = GetWorkouts(get()),
            getTotalReps = GetTotalReps(get()),
            getTodayReps = GetTodayReps(get()),
            getCurrentStreak = GetCurrentStreak(get()),
            getRepsForWorkout = GetRepsForWorkout(get())
        )
    }
    viewModelOf(::MainViewModel)
    viewModelOf(::AddEditViewModel)
    viewModelOf(::WorkoutsViewModel)
    viewModelOf(::CountRepsViewModel)
}