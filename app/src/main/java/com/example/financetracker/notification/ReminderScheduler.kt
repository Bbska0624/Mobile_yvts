package com.example.financetracker.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {
    private const val DAILY_REMINDER_WORK = "daily_finance_reminder"
    private const val REMINDER_INTERVAL_HOURS = 24L

    fun scheduleDailyReminder(
        context: Context,
        policy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
    ) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            REMINDER_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setInitialDelay(REMINDER_INTERVAL_HOURS, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            policy,
            workRequest
        )
    }
}
