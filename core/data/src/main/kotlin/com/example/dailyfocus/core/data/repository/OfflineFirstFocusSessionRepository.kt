package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.database.dao.FocusSessionDao
import com.example.dailyfocus.core.database.model.FocusSessionEntity
import com.example.dailyfocus.core.database.model.asEntity
import com.example.dailyfocus.core.database.model.asExternalModel
import com.example.dailyfocus.core.model.FocusSession
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.TaskId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OfflineFirstFocusSessionRepository @Inject constructor(
    private val dao: FocusSessionDao,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : FocusSessionRepository {
    override suspend fun get(id: String): FocusSession? = withContext(ioDispatcher) { dao.get(id)?.asExternalModel() }

    override fun observeActive(): Flow<FocusSession?> = dao.observeActive()
        .map { it?.asExternalModel() }
        .flowOn(ioDispatcher)

    override fun observePendingTarget(): Flow<FocusSession?> = dao.observePendingTarget()
        .map { it?.asExternalModel() }
        .flowOn(ioDispatcher)

    override fun observeCompletedTaskIds(): Flow<Set<TaskId>> = dao.observeCompletedTaskIds()
        .map { ids -> ids.map(::TaskId).toSet() }
        .flowOn(ioDispatcher)

    override suspend fun start(
        id: String,
        durationSeconds: Long,
        startedAtMillis: Long,
        taskId: TaskId?,
        habitId: HabitId?,
        habitLevel: HabitCompletionLevel?,
    ): Result<FocusSession> = withContext(ioDispatcher) {
        runCatching {
            require(id.isNotBlank())
            require(durationSeconds > 0)
            check(taskId == null || habitId == null) { "A focus session can have one target" }
            FocusSession(
                id = id,
                taskId = taskId,
                habitId = habitId,
                habitLevel = habitLevel,
                durationSeconds = durationSeconds,
                remainingSeconds = durationSeconds,
                startedAtMillis = startedAtMillis,
                deadlineAtMillis = startedAtMillis + durationSeconds * 1_000L,
            ).also { dao.upsert(it.asEntity()) }
        }
    }

    override suspend fun updateStatus(
        id: String,
        status: FocusSessionStatus,
        completedAtMillis: Long?,
        remainingSeconds: Long?,
        deadlineAtMillis: Long?,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val current = checkNotNull(dao.get(id)) { "Focus session not found" }
            val hasTarget = current.taskId != null || current.habitId != null
            val justCompleted = status == FocusSessionStatus.COMPLETED && current.status != FocusSessionStatus.COMPLETED.name
            dao.upsert(
                current.copy(
                    status = status.name,
                    completedAtMillis = completedAtMillis ?: current.completedAtMillis,
                    remainingSeconds = remainingSeconds ?: current.remainingSeconds,
                    deadlineAtMillis = deadlineAtMillis,
                    targetDecision = if (justCompleted && hasTarget) {
                        FocusTargetDecision.PENDING.name
                    } else {
                        current.targetDecision
                    },
                ),
            )
        }
    }

    override suspend fun resolveTarget(id: String, decision: FocusTargetDecision): Result<Boolean> = withContext(ioDispatcher) {
        runCatching {
            require(decision == FocusTargetDecision.CONFIRMED || decision == FocusTargetDecision.DISMISSED)
            dao.resolveTarget(id, decision.name) == 1
        }
    }
}
