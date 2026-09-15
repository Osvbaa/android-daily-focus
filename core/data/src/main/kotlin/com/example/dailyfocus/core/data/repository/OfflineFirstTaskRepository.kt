package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.dao.TaskDao
import com.example.dailyfocus.core.database.dao.TaskDraftDao
import com.example.dailyfocus.core.database.dao.TaskUndoDao
import com.example.dailyfocus.core.database.dao.ReminderSettingsDao
import com.example.dailyfocus.core.database.model.asExternalModelOrNull
import com.example.dailyfocus.core.database.model.asExternalModels
import com.example.dailyfocus.core.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OfflineFirstTaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val idGenerator: IdGenerator,
    private val wallClock: WallClock,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
    private val taskDraftDao: TaskDraftDao? = null,
    private val taskUndoDao: TaskUndoDao? = null,
    private val reminderSettingsDao: ReminderSettingsDao? = null,
) : TaskRepository {
    override fun observeTasks() = taskDao.observeAllTaskRows().map { rows -> rows.asExternalModels() }.flowOn(ioDispatcher)
    override fun observeTasksForDay(epochDays: Long) = taskDao.observeTaskRowsForDay(epochDays).map { rows -> rows.asExternalModels() }.flowOn(ioDispatcher)
    override fun observeTask(id: TaskId): Flow<Task?> = taskDao.observeTaskRowsById(id.value)
        .map { rows -> rows.asExternalModelOrNull() }.flowOn(ioDispatcher)
    override suspend fun getTask(id: TaskId) = withContext(ioDispatcher) {
        taskDao.getTaskRowsById(id.value).asExternalModelOrNull()
    }

    override suspend fun saveTask(id: TaskId?, draft: TaskDraft): TaskMutationResult = saveTask(id, draft, null)

    override suspend fun saveTask(id: TaskId?, draft: TaskDraft, expectedRevision: Long?): TaskMutationResult = mutate {
        val taskId = id ?: TaskId(idGenerator.nextId())
        taskDao.saveTaskDraft(taskId.value, draft, isNew = id == null, wallClock.currentTimeMillis(), expectedRevision)
        TaskMutationResult.Success(taskId)
    }

    override suspend fun getDraft(key: String): TaskDraftRecord? = withContext(ioDispatcher) {
        taskDraftDao?.getRecord(key)
    }

    override suspend fun saveDraft(record: TaskDraftRecord): TaskMutationResult = mutate {
        require(record.draft.title.length <= 10_000) { "Draft title is too long" }
        taskDraftDao?.saveRecord(record) ?: error("Draft persistence is not configured")
        TaskMutationResult.Success(record.taskId ?: TaskId(record.key))
    }

    override suspend fun deleteDraft(key: String): TaskMutationResult = mutate {
        taskDraftDao?.deleteDraft(key) ?: error("Draft persistence is not configured")
        TaskMutationResult.Success(TaskId(key))
    }

    override suspend fun completeTask(id: TaskId, includeSubtasks: Boolean): Result<TaskCompletionSnapshot> = withContext(ioDispatcher) {
        try {
            Result.success(taskDao.completeWithSnapshot(id.value, includeSubtasks))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    override suspend fun completeTaskWithUndo(
        id: TaskId,
        includeSubtasks: Boolean,
        operationId: String,
        nowElapsedMillis: Long,
        windowMillis: Long,
    ): Result<TaskUndoOperation> = withContext(ioDispatcher) {
        try {
            val operation = TaskUndoOperation(
                operationId = operationId,
                taskId = id,
                taskWasCompleted = false,
                subtaskCompletion = kotlinx.collections.immutable.persistentListOf(),
                createdElapsedMillis = nowElapsedMillis,
                expiresAtElapsedMillis = nowElapsedMillis + windowMillis,
                expectedRevision = 0L,
            )
            val task = checkNotNull(taskDao.getTaskById(id.value)) { "Task not found" }
            val children = taskDao.getSubtasksForTask(id.value)
            check(includeSubtasks || children.none { !it.isCompleted }) { "Pending subtasks require confirmation" }
            val expectedRevision = task.revision + 1L
            val snapshot = taskDao.completeWithUndo(
                id = id.value,
                includeSubtasks = includeSubtasks,
                operation = com.example.dailyfocus.core.database.model.TaskUndoEntity(
                    operationId = operationId,
                    taskId = id.value,
                    taskWasCompleted = task.isCompleted,
                    createdElapsedMillis = nowElapsedMillis,
                    expiresAtElapsedMillis = nowElapsedMillis + windowMillis,
                    expectedRevision = expectedRevision,
                ),
                subtasks = children.map { child ->
                    com.example.dailyfocus.core.database.model.TaskUndoSubtaskEntity(
                        operationId = operationId,
                        subtaskId = child.id,
                        wasCompleted = child.isCompleted,
                    )
                },
            )
            Result.success(
                operation.copy(
                    taskWasCompleted = snapshot.taskWasCompleted,
                    subtaskCompletion = snapshot.subtaskCompletion,
                    expectedRevision = snapshot.expectedRevision,
                ),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    /** Maps storage-level optimistic-concurrency exceptions to the repository contract. */
    @Suppress("SwallowedException")
    override suspend fun restoreUndo(operationId: String, nowElapsedMillis: Long): TaskUndoResult = withContext(ioDispatcher) {
        try {
            val dao = taskUndoDao ?: return@withContext super.restoreUndo(operationId, nowElapsedMillis)
            val operation = dao.getRecord(operationId) ?: return@withContext TaskUndoResult.NotFound
            if (nowElapsedMillis >= operation.expiresAtElapsedMillis) {
                dao.deleteOperation(operationId)
                dao.deleteSubtasks(operationId)
                return@withContext TaskUndoResult.Expired
            }
            val task = taskDao.getTaskById(operation.taskId.value) ?: return@withContext TaskUndoResult.NotFound
            if (task.revision != operation.expectedRevision) return@withContext TaskUndoResult.Conflict
            taskDao.restoreCompletion(
                TaskCompletionSnapshot(
                    taskId = operation.taskId,
                    taskWasCompleted = operation.taskWasCompleted,
                    subtaskCompletion = operation.subtaskCompletion,
                    operationId = operation.operationId,
                    createdElapsedMillis = operation.createdElapsedMillis,
                    expiresAtElapsedMillis = operation.expiresAtElapsedMillis,
                    expectedRevision = operation.expectedRevision,
                ),
            )
            dao.deleteOperation(operationId)
            dao.deleteSubtasks(operationId)
            TaskUndoResult.Success(operation.taskId)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: IllegalStateException) {
            TaskUndoResult.Conflict
        } catch (error: Exception) {
            TaskUndoResult.Failure(error)
        }
    }

    override fun observeUndoOperations(): Flow<List<TaskUndoOperation>> = flow {
        val dao = taskUndoDao
        if (dao == null) {
            emit(emptyList())
        } else {
            emit(withContext(ioDispatcher) { dao.all().mapNotNull { dao.getRecord(it.operationId) } })
        }
    }.flowOn(ioDispatcher)

    override suspend fun consumeExpiredUndo(nowElapsedMillis: Long): List<TaskUndoOperation> = withContext(ioDispatcher) {
        val dao = taskUndoDao ?: return@withContext emptyList()
        dao.expired(nowElapsedMillis).mapNotNull { entity ->
            val operation = dao.getRecord(entity.operationId)
            dao.deleteOperation(entity.operationId)
            dao.deleteSubtasks(entity.operationId)
            operation
        }
    }

    override fun observeReminderSettings(): Flow<TaskReminderSettings> {
        val dao = reminderSettingsDao ?: return kotlinx.coroutines.flow.flowOf(TaskReminderSettings())
        return dao.observe().map { it?.toModel() ?: TaskReminderSettings() }.flowOn(ioDispatcher)
    }

    override suspend fun getReminderSettings(): TaskReminderSettings = withContext(ioDispatcher) {
        reminderSettingsDao?.get()?.toModel() ?: TaskReminderSettings()
    }

    override suspend fun saveReminderSettings(settings: TaskReminderSettings): TaskMutationResult = mutate {
        reminderSettingsDao?.upsert(settings.toEntity()) ?: error("Reminder persistence is not configured")
        TaskMutationResult.Success(TaskId("reminder"))
    }

    private fun com.example.dailyfocus.core.database.model.ReminderSettingsEntity.toModel() = TaskReminderSettings(
        enabled = enabled,
        hour = hour,
        minute = minute,
        lastPublishedEpochDay = lastPublishedEpochDay,
    )

    private fun TaskReminderSettings.toEntity() = com.example.dailyfocus.core.database.model.ReminderSettingsEntity(
        enabled = enabled,
        hour = hour,
        minute = minute,
        lastPublishedEpochDay = lastPublishedEpochDay,
    )

    override suspend fun restoreCompletion(snapshot: TaskCompletionSnapshot) = mutate {
        taskDao.restoreCompletion(snapshot)
        TaskMutationResult.Success(snapshot.taskId)
    }
    override suspend fun rescheduleTask(id: TaskId, dueDateEpochDays: Long) = mutate {
        check(taskDao.updateDueDate(id.value, dueDateEpochDays) == 1) { "Task not found" }
        TaskMutationResult.Success(id)
    }
    override suspend fun deleteTask(id: TaskId) = mutate { taskDao.deleteTask(id.value); TaskMutationResult.Success(id) }

    private suspend fun mutate(block: suspend () -> TaskMutationResult): TaskMutationResult = withContext(ioDispatcher) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            TaskMutationResult.Failure(error)
        }
    }
}
