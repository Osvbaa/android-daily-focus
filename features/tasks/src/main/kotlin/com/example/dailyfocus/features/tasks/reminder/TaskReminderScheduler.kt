package com.example.dailyfocus.features.tasks.reminder

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.dailyfocus.core.model.TaskReminderSettings
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class TaskReminderScheduler(private val context: Context) {
    fun schedule(settings: TaskReminderSettings, now: ZonedDateTime = ZonedDateTime.now()) {
        val delay = TaskReminderPlanner.delayUntilNextRun(settings, now).toMillis()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TaskReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build(),
        )
    }

    fun sync(settings: TaskReminderSettings, now: ZonedDateTime = ZonedDateTime.now()) {
        if (settings.enabled) schedule(settings, now) else cancel()
    }

    fun cancel() = WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)

    private companion object { const val WORK_NAME = "task-daily-reminder" }
}
