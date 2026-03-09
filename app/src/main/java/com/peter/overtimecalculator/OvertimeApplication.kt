package com.peter.overtimecalculator

import android.app.Application
import com.peter.overtimecalculator.data.AppContainer

class OvertimeApplication : Application() {
    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
    }
}

