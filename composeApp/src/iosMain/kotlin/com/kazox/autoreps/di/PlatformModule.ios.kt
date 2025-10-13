package com.kazox.autoreps.di

import com.kazox.autoreps.feature.workout.data.data_source.WorkoutDatabase
import com.kazox.autoreps.feature.workout.data.data_source.getWorkoutDatabase
import com.kazox.autoreps.getDatabaseBuilder
import org.koin.dsl.module

actual val platformModule = module {
    single<WorkoutDatabase> {
        val builder = getDatabaseBuilder()
        getWorkoutDatabase(builder)
    }
}