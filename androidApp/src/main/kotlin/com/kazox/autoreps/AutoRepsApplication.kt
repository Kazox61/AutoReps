package com.kazox.autoreps

import android.app.Application
import com.kazox.autoreps.app.di.initKoin
import org.koin.android.ext.koin.androidContext

class AutoRepsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@AutoRepsApplication)
        }
    }
}