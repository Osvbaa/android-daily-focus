package com.example.dailyfocus.features.tasks.reminder

import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.features.tasks.notification.TaskNotificationReceiver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

/** Recoverable daily summary; the system may delay this work by design. */
class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val dependencies = EntryPointAccessors.fromApplication(
            applicationContext,
            TaskReminderDependencies::class.java,
        )
        val repository = dependencies.taskRepository()
        val dates = dependencies.dateProvider()
        val settings = repository.getReminderSettings()
        val today = dates.today().toEpochDay()
        val pending = repository.observeTasksForDay(today).first()
            .filterNot { it.isCompleted }
        val scheduler = TaskReminderScheduler(applicationContext)
        if (!TaskReminderPlanner.shouldPublish(settings, today, pending.isNotEmpty())) {
            scheduler.sync(settings)
            return Result.success()
        }

        publishSummary(pending.size)
        pending.forEachIndexed { index, task -> publishTask(index, task.id.value) }
        val updatedSettings = settings.copy(lastPublishedEpochDay = today)
        repository.saveReminderSettings(updatedSettings)
        scheduler.sync(updatedSettings)
        return Result.success()
    }

    private fun publishSummary(pendingCount: Int) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Recordatorios de tareas", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        manager.notify(
            SUMMARY_ID,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Resumen de tareas")
                .setContentText("Tienes $pendingCount pendientes para hoy")
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build(),
        )
    }

    private fun publishTask(index: Int, taskId: String) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val complete = actionPendingIntent(TaskNotificationReceiver.ACTION_COMPLETE, taskId, index * 2)
        val postpone = actionPendingIntent(TaskNotificationReceiver.ACTION_POSTPONE, taskId, index * 2 + 1)
        val open = openTaskPendingIntent(taskId, 10_000 + index)
        manager.notify(
            CHILD_ID_BASE + index,
            NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle("Tarea pendiente")
                .setContentText("Una tarea de hoy requiere tu atención")
                .setContentIntent(open)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setGroup(CHANNEL_ID)
                .addAction(android.R.drawable.ic_menu_send, "Completar todas", complete)
                .addAction(android.R.drawable.ic_menu_recent_history, "Mañana", postpone)
                .build(),
        )
    }

    private fun openTaskPendingIntent(taskId: String, requestCode: Int): PendingIntent {
        val intent = Intent()
            .setClassName(applicationContext, "com.example.dailyfocus.app.MainActivity")
            .setAction(TaskNotificationReceiver.ACTION_OPEN)
            .putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getActivity(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun actionPendingIntent(action: String, taskId: String, requestCode: Int): PendingIntent {
        val intent = Intent(applicationContext, TaskNotificationReceiver::class.java)
            .setAction(action)
            .putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, taskId)
        return PendingIntent.getBroadcast(
            applicationContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val CHANNEL_ID = "task-daily-reminder"
        const val SUMMARY_ID = 1201
        const val CHILD_ID_BASE = 1300
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaskReminderDependencies {
    fun taskRepository(): TaskRepository
    fun dateProvider(): DateProvider
}
