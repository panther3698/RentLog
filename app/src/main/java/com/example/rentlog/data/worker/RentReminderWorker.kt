package com.example.rentlog.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.rentlog.ui.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class RentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        NotificationHelper.showReminderNotification(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "RentReminderWork"

        fun scheduleMonthlyReminder(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Calculate delay until next month's 1st
            val calendar = Calendar.getInstance()
            val now = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 10) // 10 AM
            calendar.set(Calendar.MINUTE, 0)
            
            val delay = calendar.timeInMillis - now

            val reminderRequest = PeriodicWorkRequestBuilder<RentReminderWorker>(30, TimeUnit.DAYS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                reminderRequest
            )
        }
    }
}
