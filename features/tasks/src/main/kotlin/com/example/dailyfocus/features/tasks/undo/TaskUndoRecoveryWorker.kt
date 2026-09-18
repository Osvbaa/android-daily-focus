package com.example.dailyfocus.features.tasks.undo

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.dailyfocus.core.common.analytics.AnalyticsEvent
import com.example.dailyfocus.core.common.analytics.AnalyticsTracker
import com.example.dailyfocus.core.common.analytics.InteractionSurface
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.common.time.MonotonicClock
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.data.repository.TaskRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Reclaims expired Undo operations after process death without resurrecting data. */
class TaskUndoRecoveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val deps = EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
        val operations = deps.repository().consumeExpiredUndo(deps.monotonicClock().elapsedRealtimeMillis())
        operations.forEach { operation ->
            val task = deps.repository().getTask(operation.taskId) ?: return@forEach
            deps.analytics().track(
                AnalyticsEvent.TaskCompleted(
                    InteractionSurface.APP_SCREEN,
                    ((deps.wallClock().currentTimeMillis() - task.createdAtMillis) / 3_600_000L).coerceAtLeast(0).toInt(),
                    task.dueDateEpochDays?.let { it < deps.dates().today().toEpochDay() } == true,
                ),
            )
        }
        return Result.success()
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun repository(): TaskRepository
        fun analytics(): AnalyticsTracker
        fun dates(): DateProvider
        fun wallClock(): WallClock
        fun monotonicClock(): MonotonicClock
    }
}
