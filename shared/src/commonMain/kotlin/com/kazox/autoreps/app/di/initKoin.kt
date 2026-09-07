package com.kazox.autoreps.app.di

import com.kazox.autoreps.core.data.repository.RoomWorkoutRepository
import com.kazox.autoreps.core.data.repository.StoredSettingsRepository
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import com.kazox.autoreps.core.sound.SoundPlayer
import com.kazox.autoreps.core.sound.createSoundPlayer
import com.kazox.autoreps.feature.history.presentation.HistoryViewModel
import com.kazox.autoreps.feature.home.presentation.HomeViewModel
import com.kazox.autoreps.feature.record.presentation.RecordViewModel
import com.kazox.autoreps.feature.settings.presentation.SettingsViewModel
import com.kazox.autoreps.feature.workout.presentation.AddEditWorkoutViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.includes
import org.koin.dsl.module

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        includes(configuration)
        modules(viewModelModule, platformModule, repositoryModule, soundModule)
    }
}

fun doInitKoin() = initKoin()

val viewModelModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::HistoryViewModel)
    viewModelOf(::RecordViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AddEditWorkoutViewModel)
}

val repositoryModule = module {
    singleOf(::RoomWorkoutRepository).bind<WorkoutRepository>()
    // A single, not a factory: it caches the store in memory and is the only writer, so a second
    // instance would hold a copy that the first one's writes never reach.
    singleOf(::StoredSettingsRepository).bind<SettingsRepository>()
}

val soundModule = module {
    // A factory, not a single: the player holds real audio handles per tone, and the screen that
    // uses it releases them when it goes away. A shared instance would have nobody to do that.
    factory<SoundPlayer> { createSoundPlayer() }
}

expect val platformModule: Module