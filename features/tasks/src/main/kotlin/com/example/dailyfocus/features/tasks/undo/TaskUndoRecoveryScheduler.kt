package com.example.dailyfocus.features.tasks.undo

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class TaskUndoRecoveryScheduler(private val context: Context) {
    fun schedule() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<TaskUndoRecoveryWorker>().build(),
        )
    }

    private companion object { const val WORK_NAME = "task-undo-recovery" }
}
