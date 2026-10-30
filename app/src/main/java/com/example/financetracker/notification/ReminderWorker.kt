package com.example.financetracker.notification

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        NotificationHelper.showReminderNotification(applicationContext)
        return Result.success()
    }
}
