package com.example.spotted

import android.app.Application
import com.example.spotted.data.di.appModule
import com.example.spotted.ui.theme.ThemeManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Spotted : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@Spotted)
            modules(appModule)
        }
        ThemeManager.init(applicationContext)
    }
}