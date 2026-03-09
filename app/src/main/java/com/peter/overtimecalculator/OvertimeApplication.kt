package com.peter.overtimecalculator

import android.app.Application
import com.peter.overtimecalculator.data.AppContainer
import com.peter.overtimecalculator.data.holiday.HolidayRefreshResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OvertimeApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this, applicationScope)
        appContainer.scheduleHolidaySync()
        applicationScope.launch {
            when (appContainer.holidayRulesRepository.refreshIfStale()) {
                HolidayRefreshResult.Updated,
                HolidayRefreshResult.Skipped,
                is HolidayRefreshResult.Failed -> Unit
            }
        }
    }
}
