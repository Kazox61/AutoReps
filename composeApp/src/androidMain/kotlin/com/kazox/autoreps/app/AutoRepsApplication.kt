package com.kazox.autoreps.app

import android.app.Application
import com.kazox.autoreps.di.initKoin
import org.koin.android.ext.koin.androidContext

class AutoRepsApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        instance = this

        initKoin {
            androidContext(this@AutoRepsApplication)
        }
    }

    companion object {
        lateinit var instance: AutoRepsApplication
            private set
    }
}