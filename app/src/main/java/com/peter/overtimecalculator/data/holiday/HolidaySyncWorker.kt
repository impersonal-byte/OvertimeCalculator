package com.peter.overtimecalculator.data.holiday

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.peter.overtimecalculator.OvertimeApplication
import java.util.concurrent.TimeUnit

class HolidaySyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val repository = (applicationContext as OvertimeApplication).appContainer.holidayRulesRepository
        val refreshResult = repository.refreshRemoteRules()
        return when (refreshResult) {
            HolidayRefreshResult.Updated,
            HolidayRefreshResult.Skipped -> Result.success()

            is HolidayRefreshResult.Failed -> {
                if (refreshResult.retryable) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "holiday-sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = PeriodicWorkRequestBuilder<HolidaySyncWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
