package com.kazox.autoreps.app.di

import com.kazox.autoreps.core.data.data_source.DatabaseFactory
import com.kazox.autoreps.core.data.data_source.SettingsFactory
import com.kazox.autoreps.core.data.data_source.WorkoutDatabase
import com.kazox.autoreps.core.data.data_source.getWorkoutDatabase
import com.russhwolf.settings.Settings
import org.koin.dsl.module

actual val platformModule = module {
    single<WorkoutDatabase> {
        val builder = DatabaseFactory().createBuilder()
        getWorkoutDatabase(builder)
    }

    single<Settings> { SettingsFactory().create() }
}
