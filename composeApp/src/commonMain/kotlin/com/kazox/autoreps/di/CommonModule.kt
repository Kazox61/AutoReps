package com.kazox.autoreps.di

import com.kazox.autoreps.MainViewModel
import com.kazox.autoreps.feature.workout.data.repository.WorkoutRepositoryImpl
import com.kazox.autoreps.feature.workout.domain.repository.WorkoutRepository
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module


val commonModule = module {
    singleOf(::WorkoutRepositoryImpl).bind<WorkoutRepository>()
    viewModelOf(::MainViewModel)
}