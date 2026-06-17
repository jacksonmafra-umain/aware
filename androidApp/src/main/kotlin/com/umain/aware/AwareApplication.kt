package com.umain.aware

import android.app.Application
import com.umain.aware.di.initKoin

/**
 * Starts Koin once, before any UI. Registered as the app's `android:name` so `MainActivity` only
 * ever has to call `App()`.
 */
class AwareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
