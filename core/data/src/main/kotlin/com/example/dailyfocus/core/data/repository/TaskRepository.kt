package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskCompletionSnapshot
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.TaskDraftRecord
import com.example.dailyfocus.core.model.TaskUndoOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

sealed interface TaskMutationResult {
    /** The mutation was applied and the aggregate can be addressed by this ID. */
    data class Success(val taskId: TaskId) : TaskMutationResult

    /** The mutation was rejected without exposing persistence-layer details to a feature. */
    data class Failure(val cause: Throwable) : TaskMutationResult
}

sealed interface TaskUndoResult {
    data class Success(val taskId: TaskId) : TaskUndoResult
    data object NotFound : TaskUndoResult
    data object Expired : TaskUndoResult
    data object Conflict : TaskUndoResult
    data class Failure(val cause: Throwable) : TaskUndoResult
}

class TaskRevisionConflictException(
    val expectedRevision: Long?,
    val actualRevision: Long,
) : IllegalStateException("Task changed while it was being edited")

/**
 * Persistence boundary for the task aggregate.
 *
 * A task and its subtasks are one aggregate at this boundary. In particular, [saveTask]
 * replaces the ordered subtask collection atomically; features never write Room entities
 * or DAOs directly.
 */
interface TaskRepository {
    /** Emits all tasks ordered by their persisted creation order. */
    fun observeTasks(): Flow<List<Task>>

    /** Emits only tasks whose due date matches [epochDays]. */
    fun observeTasksForDay(epochDays: Long): Flow<List<Task>>

    /** Observes one task, emitting null when it does not exist. */
    fun observeTask(id: TaskId): Flow<Task?>

    suspend fun getTask(id: TaskId): Task?

    /**
     * Creates or updates a task and its ordered subtasks in one persistence operation.
     * Rejects blank titles, duplicate subtask IDs and subtasks owned by another task.
     * A non-null ID updates an existing task only; it must never recreate a deleted task.
     * The metadata read and aggregate write are atomic. Text is trimmed and positions
     * are normalized to consecutive indices without changing subtask identity.
     */
    suspend fun saveTask(id: TaskId?, draft: TaskDraft): TaskMutationResult

    /** Optimistic-concurrency variant used by the editor. */
    suspend fun saveTask(
        id: TaskId?,
        draft: TaskDraft,
        expectedRevision: Long?,
    ): TaskMutationResult = saveTask(id, draft)

    suspend fun createTaskFromNote(title: String, noteId: String): TaskMutationResult

    /** Durable draft storage; drafts are never published tasks. */
    suspend fun getDraft(key: String): TaskDraftRecord? = null
    suspend fun saveDraft(record: TaskDraftRecord): TaskMutationResult = TaskMutationResult.Success(record.taskId ?: TaskId(record.key))
    suspend fun deleteDraft(key: String): TaskMutationResult = TaskMutationResult.Success(TaskId(key))

    /**
     * Marks a task complete and returns the previous completion state for Undo.
     * When [includeSubtasks] is true, all subtasks are completed in the same operation.
     * Otherwise pending subtasks reject the operation. Reading the snapshot and
     * completing the aggregate are atomic. Cancellation always propagates.
     */
    suspend fun completeTask(id: TaskId, includeSubtasks: Boolean): Result<TaskCompletionSnapshot>

    /** Completes and records an individual, durable Undo operation atomically when supported. */
    suspend fun completeTaskWithUndo(
        id: TaskId,
        includeSubtasks: Boolean,
        operationId: String,
        nowElapsedMillis: Long,
        windowMillis: Long,
    ): Result<TaskUndoOperation> = completeTask(id, includeSubtasks).map { snapshot ->
        TaskUndoOperation(
            operationId = operationId,
            taskId = snapshot.taskId,
            taskWasCompleted = snapshot.taskWasCompleted,
            subtaskCompletion = snapshot.subtaskCompletion,
            createdElapsedMillis = nowElapsedMillis,
            expiresAtElapsedMillis = nowElapsedMillis + windowMillis,
            expectedRevision = snapshot.expectedRevision,
        )
    }

    suspend fun restoreUndo(operationId: String, nowElapsedMillis: Long): TaskUndoResult {
        val operation = observeUndoOperations().first().firstOrNull { it.operationId == operationId }
        return when (operation) {
            null -> TaskUndoResult.NotFound
            else -> when {
                nowElapsedMillis >= operation.expiresAtElapsedMillis -> TaskUndoResult.Expired
                else -> restoreCompletion(operation.toCompletionSnapshot()).toUndoResult()
            }
        }
    }

    private fun TaskUndoOperation.toCompletionSnapshot() = TaskCompletionSnapshot(
        taskId = taskId,
        taskWasCompleted = taskWasCompleted,
        subtaskCompletion = subtaskCompletion,
        operationId = operationId,
        createdElapsedMillis = createdElapsedMillis,
        expiresAtElapsedMillis = expiresAtElapsedMillis,
        expectedRevision = expectedRevision,
    )

    private fun TaskMutationResult.toUndoResult(): TaskUndoResult = when (this) {
        is TaskMutationResult.Success -> TaskUndoResult.Success(taskId)
        is TaskMutationResult.Failure -> TaskUndoResult.Failure(cause)
    }

    fun observeUndoOperations(): Flow<List<TaskUndoOperation>> = flowOf(emptyList())

    /** Claims expired operations exactly once for delayed analytics dispatch. */
    suspend fun consumeExpiredUndo(nowElapsedMillis: Long): List<TaskUndoOperation> = emptyList()

    fun observeReminderSettings(): Flow<com.example.dailyfocus.core.model.TaskReminderSettings> =
        flowOf(com.example.dailyfocus.core.model.TaskReminderSettings())

    suspend fun getReminderSettings(): com.example.dailyfocus.core.model.TaskReminderSettings =
        com.example.dailyfocus.core.model.TaskReminderSettings()

    suspend fun saveReminderSettings(
        settings: com.example.dailyfocus.core.model.TaskReminderSettings,
    ): TaskMutationResult = TaskMutationResult.Success(TaskId("reminder"))

    /**
     * Restores completion values atomically, rejecting missing tasks or a changed
     * subtask ID set. Durable operation IDs and revision guards are introduced in H2/H4.
     */
    suspend fun restoreCompletion(snapshot: TaskCompletionSnapshot): TaskMutationResult

    suspend fun rescheduleTask(id: TaskId, dueDateEpochDays: Long): TaskMutationResult
    suspend fun deleteTask(id: TaskId): TaskMutationResult
}
