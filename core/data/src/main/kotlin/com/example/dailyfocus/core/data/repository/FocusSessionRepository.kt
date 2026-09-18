package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.FocusSession
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.TaskId
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    suspend fun get(id: String): FocusSession?
    fun observeActive(): Flow<FocusSession?>
    fun observePendingTarget(): Flow<FocusSession?>
    /** Task IDs that have at least one completed focus session. */
    fun observeCompletedTaskIds(): Flow<Set<TaskId>>
    suspend fun start(
        id: String,
        durationSeconds: Long,
        startedAtMillis: Long,
        taskId: TaskId? = null,
        habitId: HabitId? = null,
        habitLevel: HabitCompletionLevel? = null,
    ): Result<FocusSession>
    suspend fun updateStatus(
        id: String,
        status: FocusSessionStatus,
        completedAtMillis: Long? = null,
        remainingSeconds: Long? = null,
        deadlineAtMillis: Long? = null,
    ): Result<Unit>

    /** Resolves a completed session's target exactly once; false means it was already resolved. */
    suspend fun resolveTarget(id: String, decision: FocusTargetDecision): Result<Boolean>
}
