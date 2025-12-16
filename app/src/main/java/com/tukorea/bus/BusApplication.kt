package com.tukorea.bus

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BusApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: BusApplication
            private set
    }
}