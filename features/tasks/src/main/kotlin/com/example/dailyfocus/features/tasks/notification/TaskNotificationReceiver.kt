package com.example.dailyfocus.features.tasks.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.dailyfocus.core.common.analytics.AnalyticsEvent
import com.example.dailyfocus.core.common.analytics.AnalyticsTracker
import com.example.dailyfocus.core.common.analytics.InteractionSurface
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.model.TaskId
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val dependencies = EntryPointAccessors.fromApplication(
            context.applicationContext,
            TaskNotificationDependencies::class.java,
        )
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val rawId = intent.getStringExtra(EXTRA_TASK_ID) ?: return@launch
                val id = TaskId(rawId)
                when (intent.action) {
                    ACTION_COMPLETE -> {
                        val result = dependencies.repository().completeTask(id, includeSubtasks = true)
                        if (result.isSuccess) {
                            dependencies.analytics().track(
                                AnalyticsEvent.TaskCompleted(InteractionSurface.NOTIFICATION_ACTION, 0, false),
                            )
                        }
                    }
                    ACTION_POSTPONE -> {
                        val result = dependencies.repository().rescheduleTask(
                            id,
                            dependencies.dates().today().plusDays(1).toEpochDay(),
                        )
                        if (result is TaskMutationResult.Success) {
                            dependencies.analytics().track(AnalyticsEvent.TaskRescheduled(InteractionSurface.NOTIFICATION_ACTION))
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_OPEN = "com.example.dailyfocus.tasks.OPEN"
        const val ACTION_COMPLETE = "com.example.dailyfocus.tasks.COMPLETE"
        const val ACTION_POSTPONE = "com.example.dailyfocus.tasks.POSTPONE"
        const val EXTRA_TASK_ID = "task_id"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface TaskNotificationDependencies {
    fun repository(): TaskRepository
    fun dates(): DateProvider
    fun analytics(): AnalyticsTracker
}
