package com.zulian.ayudafinanzas

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupRecurringWork()
    }

    private fun setupRecurringWork() {
        val workManager = WorkManager.getInstance(applicationContext)

        val dailyReminderRequest = PeriodicWorkRequestBuilder<ChequeReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        workManager.enqueueUniquePeriodicWork(
            ChequeReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyReminderRequest
        )
    }
}
