package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskCompletionSnapshot
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskDraftRecord
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.TaskUndoOperation
import com.example.dailyfocus.core.data.repository.TaskUndoResult
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FakeTaskRepository(var latencyMillis: Long = 0) : TaskRepository {
    private val state = MutableStateFlow<Map<TaskId, Task>>(emptyMap())
    private val mutationMutex = Mutex()
    private val drafts = mutableMapOf<String, TaskDraftRecord>()
    private val undoOperations = mutableMapOf<String, TaskUndoOperation>()
    private val undoState = MutableStateFlow<List<TaskUndoOperation>>(emptyList())
    private val reminderState = MutableStateFlow(com.example.dailyfocus.core.model.TaskReminderSettings())
    var failure: Throwable? = null
    var readFailure: Throwable? = null
    var readLatencyMillis: Long = 0
    private var nextId = 0

    /** Test setup only; call before starting concurrent operations. */
    fun seed(vararg tasks: Task) {
        require(tasks.map { it.id }.distinct().size == tasks.size)
        state.value = tasks.associateBy { it.id }
    }

    override fun observeTasks(): Flow<List<Task>> = state.map { tasks ->
        beforeRead()
        tasks.values.sortedWith(compareByDescending<Task> { it.createdAtMillis }.thenBy { it.id.value })
    }

    override fun observeTasksForDay(epochDays: Long) = observeTasks().map { tasks ->
        tasks.filter { it.dueDateEpochDays == epochDays }
    }

    override fun observeTask(id: TaskId) = state.map { tasks -> beforeRead(); tasks[id] }

    override suspend fun getTask(id: TaskId): Task? {
        beforeRead()
        return state.value[id]
    }

    override suspend fun saveTask(id: TaskId?, draft: TaskDraft): TaskMutationResult = saveTask(id, draft, null)

    override suspend fun saveTask(id: TaskId?, draft: TaskDraft, expectedRevision: Long?): TaskMutationResult = mutation {
        require(draft.title.isNotBlank()) { "Task title must not be blank" }
        require(draft.subtasks.map { it.id }.distinct().size == draft.subtasks.size) { "Duplicate subtask IDs" }
        val taskId = id ?: nextTaskId()
        val previous = state.value[taskId]
        check(id == null || previous != null) { "Task not found" }
        if (expectedRevision != null) {
            check(previous?.revision == expectedRevision) { "Task changed while it was being edited" }
        }
        val otherChildIds = state.value.values.filter { it.id != taskId }
            .flatMap { task -> task.subtasks.map { it.id } }.toSet()
        require(draft.subtasks.none { it.id in otherChildIds }) { "Subtask belongs to another task" }
        val task = Task(
            id = taskId,
            title = draft.title.trim(),
            description = draft.description.trim().ifBlank { null },
            isCompleted = previous?.isCompleted ?: false,
            priority = draft.priority,
            linkedNoteId = previous?.linkedNoteId,
            createdAtMillis = previous?.createdAtMillis ?: 0,
            dueDateEpochDays = draft.dueDateEpochDays,
            subtasks = draft.subtasks.mapIndexed { index, child ->
                child.copy(taskId = taskId, position = index)
            }.toPersistentList(),
            revision = (previous?.revision ?: 0L) + 1L,
        )
        state.value = state.value + (taskId to task)
        taskId
    }

    override suspend fun getDraft(key: String): TaskDraftRecord? = mutationMutex.withLock {
        delay(latencyMillis)
        drafts[key]
    }

    override suspend fun saveDraft(record: TaskDraftRecord): TaskMutationResult = mutation {
        drafts[record.key] = record.copy(
            draft = record.draft.copy(
                title = record.draft.title.trim(),
                description = record.draft.description.trim(),
                subtasks = record.draft.subtasks.mapIndexed { index, subtask ->
                    subtask.copy(position = index)
                }.toPersistentList(),
            ),
        )
        TaskId(record.taskId?.value ?: record.key)
    }

    override suspend fun deleteDraft(key: String): TaskMutationResult = mutation {
        drafts.remove(key)
        TaskId(key)
    }

    override suspend fun completeTask(id: TaskId, includeSubtasks: Boolean): Result<TaskCompletionSnapshot> =
        attempt {
            val task = checkNotNull(state.value[id]) { "Task not found" }
            check(includeSubtasks || task.subtasks.none { !it.isCompleted }) { "Pending subtasks require confirmation" }
            val snapshot = TaskCompletionSnapshot(
                id, task.isCompleted, task.subtasks.map { it.id to it.isCompleted }.toPersistentList(),
                previousRevision = task.revision,
            )
            val children = if (includeSubtasks) {
                task.subtasks.map { it.copy(isCompleted = true) }.toPersistentList()
            } else {
                task.subtasks
            }
            val updated = task.copy(isCompleted = true, subtasks = children, revision = task.revision + 1L)
            state.value = state.value + (id to updated)
            snapshot.copy(expectedRevision = updated.revision)
        }

    override suspend fun completeTaskWithUndo(
        id: TaskId,
        includeSubtasks: Boolean,
        operationId: String,
        nowElapsedMillis: Long,
        windowMillis: Long,
    ): Result<TaskUndoOperation> = attempt {
        val task = checkNotNull(state.value[id]) { "Task not found" }
        check(includeSubtasks || task.subtasks.none { !it.isCompleted }) { "Pending subtasks require confirmation" }
        val operation = TaskUndoOperation(
            operationId = operationId,
            taskId = id,
            taskWasCompleted = task.isCompleted,
            subtaskCompletion = task.subtasks.map { it.id to it.isCompleted }.toPersistentList(),
            createdElapsedMillis = nowElapsedMillis,
            expiresAtElapsedMillis = nowElapsedMillis + windowMillis,
            expectedRevision = task.revision + 1L,
        )
        val children = if (includeSubtasks) task.subtasks.map { it.copy(isCompleted = true) }.toPersistentList() else task.subtasks
        state.value = state.value + (id to task.copy(isCompleted = true, subtasks = children, revision = task.revision + 1L))
        undoOperations[operationId] = operation
        undoState.value = undoOperations.values.toList()
        operation
    }

    override suspend fun restoreUndo(operationId: String, nowElapsedMillis: Long): TaskUndoResult = mutationMutex.withLock {
        delay(latencyMillis)
        failure?.let { throw it }
        val operation = undoOperations[operationId] ?: return@withLock TaskUndoResult.NotFound
        if (nowElapsedMillis >= operation.expiresAtElapsedMillis) {
            undoOperations.remove(operationId)
            undoState.value = undoOperations.values.toList()
            return@withLock TaskUndoResult.Expired
        }
        val task = state.value[operation.taskId] ?: return@withLock TaskUndoResult.NotFound
        if (task.revision != operation.expectedRevision) return@withLock TaskUndoResult.Conflict
        val values = operation.subtaskCompletion.toMap()
        if (task.subtasks.map { it.id }.toSet() != values.keys) return@withLock TaskUndoResult.Conflict
        state.value = state.value + (operation.taskId to task.copy(
            isCompleted = operation.taskWasCompleted,
            subtasks = task.subtasks.map { it.copy(isCompleted = values.getValue(it.id)) }.toPersistentList(),
            revision = task.revision + 1L,
        ))
        undoOperations.remove(operationId)
        undoState.value = undoOperations.values.toList()
        TaskUndoResult.Success(operation.taskId)
    }

    override fun observeUndoOperations(): Flow<List<TaskUndoOperation>> = undoState

    override suspend fun consumeExpiredUndo(nowElapsedMillis: Long): List<TaskUndoOperation> = mutationMutex.withLock {
        val expired = undoOperations.values.filter {
            it.expiresAtElapsedMillis <= nowElapsedMillis || it.createdElapsedMillis > nowElapsedMillis
        }
        expired.forEach { undoOperations.remove(it.operationId) }
        undoState.value = undoOperations.values.toList()
        expired
    }

    override fun observeReminderSettings(): Flow<com.example.dailyfocus.core.model.TaskReminderSettings> = reminderState

    override suspend fun getReminderSettings() = reminderState.value

    override suspend fun saveReminderSettings(settings: com.example.dailyfocus.core.model.TaskReminderSettings): TaskMutationResult = mutation {
        reminderState.value = settings
        TaskId("reminder")
    }

    override suspend fun restoreCompletion(snapshot: TaskCompletionSnapshot) = mutation {
        val id = snapshot.taskId
        val task = checkNotNull(state.value[id]) { "Task not found" }
        if (snapshot.expectedRevision > 0L) check(task.revision == snapshot.expectedRevision) { "Task changed after completion" }
        val values = snapshot.subtaskCompletion
        check(values.map { it.first }.distinct().size == values.size) { "Duplicate snapshot IDs" }
        check(task.subtasks.map { it.id }.toSet() == values.map { it.first }.toSet()) { "Subtask structure changed" }
        val completion = values.toMap()
        state.value = state.value + (id to task.copy(
            isCompleted = snapshot.taskWasCompleted,
            subtasks = task.subtasks.map { it.copy(isCompleted = completion.getValue(it.id)) }.toPersistentList(),
            revision = if (snapshot.operationId != null) task.revision + 1L
            else snapshot.previousRevision,
        ))
        id
    }

    override suspend fun rescheduleTask(id: TaskId, dueDateEpochDays: Long) = mutation {
        val task = checkNotNull(state.value[id]) { "Task not found" }
        state.value = state.value + (id to task.copy(dueDateEpochDays = dueDateEpochDays, revision = task.revision + 1L))
        id
    }

    override suspend fun deleteTask(id: TaskId) = mutation {
        check(state.value.containsKey(id)) { "Task not found" }
        state.value = state.value - id
        id
    }

    private fun nextTaskId(): TaskId {
        var id: TaskId
        do {
            id = TaskId("fake-${++nextId}")
        } while (id in state.value)
        return id
    }

    private suspend fun beforeRead() {
        delay(readLatencyMillis)
        readFailure?.let { throw it }
    }

    private suspend fun mutation(block: () -> TaskId): TaskMutationResult = attempt(block).fold(
        onSuccess = { TaskMutationResult.Success(it) },
        onFailure = { TaskMutationResult.Failure(it) },
    )

    private suspend fun <T> attempt(block: () -> T): Result<T> = try {
        delay(latencyMillis)
        mutationMutex.withLock {
            failure?.let { throw it }
            Result.success(block())
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (error: Exception) {
        Result.failure(error)
    }
}
