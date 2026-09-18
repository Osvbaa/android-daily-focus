package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.FocusSessionRepository
import com.example.dailyfocus.core.model.FocusSession
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.TaskId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory focus-session contract fake for feature tests. */
class FakeFocusSessionRepository : FocusSessionRepository {
    private val sessions = MutableStateFlow<Map<String, FocusSession>>(emptyMap())

    override suspend fun get(id: String): FocusSession? = sessions.value[id]

    override fun observeActive(): Flow<FocusSession?> = sessions.map { values ->
        values.values.filter { it.status in setOf(FocusSessionStatus.RUNNING, FocusSessionStatus.PAUSED) }
            .maxByOrNull(FocusSession::startedAtMillis)
    }

    override fun observePendingTarget(): Flow<FocusSession?> = sessions.map { values ->
        values.values.filter { it.status == FocusSessionStatus.COMPLETED && it.targetDecision == FocusTargetDecision.PENDING }
            .maxByOrNull { it.completedAtMillis ?: it.startedAtMillis }
    }

    override fun observeCompletedTaskIds(): Flow<Set<TaskId>> = sessions.map { values ->
        values.values.asSequence()
            .filter { it.status == FocusSessionStatus.COMPLETED }
            .mapNotNull(FocusSession::taskId)
            .toSet()
    }

    override suspend fun start(
        id: String,
        durationSeconds: Long,
        startedAtMillis: Long,
        taskId: TaskId?,
        habitId: HabitId?,
        habitLevel: HabitCompletionLevel?,
    ): Result<FocusSession> = runCatching {
        require(taskId == null || habitId == null)
        FocusSession(id, taskId, habitId, habitLevel, durationSeconds, durationSeconds, startedAtMillis, startedAtMillis + durationSeconds * 1_000L)
            .also { session -> sessions.value = sessions.value + (id to session) }
    }

    override suspend fun updateStatus(
        id: String,
        status: FocusSessionStatus,
        completedAtMillis: Long?,
        remainingSeconds: Long?,
        deadlineAtMillis: Long?,
    ): Result<Unit> = runCatching {
        val session = requireNotNull(sessions.value[id])
        val hasTarget = session.taskId != null || session.habitId != null
        val justCompleted = status == FocusSessionStatus.COMPLETED && session.status != FocusSessionStatus.COMPLETED
        sessions.value = sessions.value + (id to session.copy(
            status = status,
            completedAtMillis = completedAtMillis ?: session.completedAtMillis,
            remainingSeconds = remainingSeconds ?: session.remainingSeconds,
            deadlineAtMillis = deadlineAtMillis,
            targetDecision = if (justCompleted && hasTarget) {
                FocusTargetDecision.PENDING
            } else {
                session.targetDecision
            },
        ))
    }

    override suspend fun resolveTarget(id: String, decision: FocusTargetDecision): Result<Boolean> = runCatching {
        require(decision == FocusTargetDecision.CONFIRMED || decision == FocusTargetDecision.DISMISSED)
        val session = sessions.value[id] ?: return@runCatching false
        if (session.status != FocusSessionStatus.COMPLETED || session.targetDecision != FocusTargetDecision.PENDING) {
            return@runCatching false
        }
        sessions.value = sessions.value + (id to session.copy(targetDecision = decision))
        true
    }
}
