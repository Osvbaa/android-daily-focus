package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.StreakEventType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class OfflineFirstFocusTargetDecisionRepository @Inject constructor(
    private val sessions: FocusSessionRepository,
    private val tasks: TaskRepository,
    private val habits: HabitRepository,
    private val activity: ActivityRepository,
    private val dates: DateProvider,
) : FocusTargetDecisionRepository {
    private val decisionMutex = Mutex()

    override fun observePending(): Flow<com.example.dailyfocus.core.model.FocusSession?> = sessions.observePendingTarget()

    override suspend fun confirm(sessionId: String): Result<Boolean> = decisionMutex.withLock {
        try {
            val session = sessions.get(sessionId)
            if (session?.status != FocusSessionStatus.COMPLETED || session.targetDecision != FocusTargetDecision.PENDING) {
                return@withLock Result.success(false)
            }
            val epochDay = dates.today().toEpochDay()
            val taskId = session.taskId
            val habitId = session.habitId
            when {
                taskId != null -> {
                    tasks.completeTask(taskId, includeSubtasks = false).getOrThrow()
                    val task = tasks.getTask(taskId)
                    activity.recordCompletion(
                        type = if (task?.projectId == null) StreakEventType.TASK else StreakEventType.PROJECT_TASK,
                        sourceId = taskId.value,
                        epochDay = epochDay,
                    ).getOrThrow()
                }
                habitId != null -> {
                    val level = requireNotNull(session.habitLevel) { "Habit level missing" }
                    habits.completeLevel(habitId, epochDay, level).getOrThrow()
                    activity.recordCompletion(StreakEventType.HABIT_LEVEL, habitId.value, epochDay, level).getOrThrow()
                }
                else -> return@withLock Result.success(false)
            }
            sessions.resolveTarget(sessionId, FocusTargetDecision.CONFIRMED)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun dismiss(sessionId: String): Result<Boolean> = decisionMutex.withLock {
        sessions.resolveTarget(sessionId, FocusTargetDecision.DISMISSED)
    }
}
