package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.dailyfocus.core.database.model.TaskEntity
import com.example.dailyfocus.core.database.model.TaskNoteCrossRef
import com.example.dailyfocus.core.database.model.SubtaskEntity
import com.example.dailyfocus.core.database.model.TaskRow
import com.example.dailyfocus.core.database.model.TaskUndoEntity
import com.example.dailyfocus.core.database.model.TaskUndoSubtaskEntity
import com.example.dailyfocus.core.model.TaskCompletionSnapshot
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.SubtaskId
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow

private const val TASK_ROWS = "SELECT t.id AS task_id, t.title AS task_title, " +
    "t.description AS task_description, t.is_completed AS task_is_completed, " +
    "t.due_date_epoch_days AS task_due_date_epoch_days, t.priority AS task_priority, " +
    "t.estimated_duration_seconds AS task_estimated_duration_seconds, " +
    "t.created_at_millis AS task_created_at_millis, t.revision AS task_revision, " +
    "t.project_id AS project_id, t.milestone_id AS milestone_id, " +
    "(SELECT note_id FROM task_note_cross_ref WHERE task_id = t.id ORDER BY note_id LIMIT 1) AS linked_note_id, " +
    "s.id AS subtask_id, s.title AS subtask_title, s.is_completed AS subtask_is_completed, " +
    "s.position AS subtask_position FROM tasks t LEFT JOIN subtasks s ON t.id = s.task_id "

@Dao
interface TaskDao {

    @Query(TASK_ROWS + "ORDER BY t.created_at_millis DESC, t.id ASC, s.position ASC")
    fun observeAllTaskRows(): Flow<List<TaskRow>>

    @Query(TASK_ROWS + "WHERE t.due_date_epoch_days = :epochDays ORDER BY t.created_at_millis DESC, t.id ASC, s.position ASC")
    fun observeTaskRowsForDay(epochDays: Long): Flow<List<TaskRow>>

    @Query(TASK_ROWS + "WHERE t.id = :id ORDER BY s.position ASC")
    suspend fun getTaskRowsById(id: String): List<TaskRow>

    @Query(TASK_ROWS + "WHERE t.id = :id ORDER BY s.position ASC")
    fun observeTaskRowsById(id: String): Flow<List<TaskRow>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM subtasks WHERE id = :id")
    suspend fun getSubtaskById(id: String): SubtaskEntity?

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Upsert
    suspend fun upsertSubtasks(subtasks: List<SubtaskEntity>)

    @Query("DELETE FROM subtasks WHERE task_id = :taskId")
    suspend fun deleteSubtasks(taskId: String)

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId ORDER BY position ASC")
    suspend fun getSubtasksForTask(taskId: String): List<SubtaskEntity>

    @Transaction
    suspend fun saveAggregate(task: TaskEntity, subtasks: List<SubtaskEntity>) {
        require(task.title.isNotBlank()) { "Task title must not be blank" }
        require(subtasks.map { it.id }.distinct().size == subtasks.size) { "Duplicate subtask IDs" }
        subtasks.forEach { subtask ->
            require(subtask.taskId == task.id) { "Subtask parent does not match" }
            val stored = getSubtaskById(subtask.id)
            require(stored == null || stored.taskId == task.id) { "Subtask belongs to another task" }
        }
        upsertTask(task)
        deleteSubtasks(task.id)
        if (subtasks.isNotEmpty()) upsertSubtasks(subtasks)
    }

    /** Read-before-write metadata and the aggregate update share the same transaction. */
    @Transaction
    suspend fun saveTaskDraft(
        id: String,
        draft: TaskDraft,
        isNew: Boolean,
        createdAtMillis: Long,
        expectedRevision: Long? = null,
    ) {
        val previous = getTaskById(id)
        check(if (isNew) previous == null else previous != null) { "Task identity no longer matches" }
        if (expectedRevision != null) {
            check(previous?.revision == expectedRevision) { "Task changed while it was being edited" }
        }
        val task = TaskEntity(
            id = id,
            title = draft.title.trim(),
            description = draft.description.trim().ifBlank { null },
            isCompleted = previous?.isCompleted ?: false,
            dueDateEpochDays = draft.dueDateEpochDays,
            priority = draft.priority,
            estimatedDurationSeconds = draft.estimatedDurationSeconds,
            createdAtMillis = previous?.createdAtMillis ?: createdAtMillis,
            revision = (previous?.revision ?: 0L) + 1L,
            projectId = draft.projectId?.value,
            milestoneId = draft.milestoneId?.value,
        )
        val subtasks = draft.subtasks.mapIndexed { index, child ->
            SubtaskEntity(child.id.value, id, child.title, child.isCompleted, index)
        }
        saveAggregate(task, subtasks)
    }

    @Query("UPDATE subtasks SET is_completed = 1 WHERE task_id = :taskId")
    suspend fun completeSubtasks(taskId: String)

    @Transaction
    suspend fun completeAggregate(taskId: String) {
        updateTaskStatus(taskId, true)
        completeSubtasks(taskId)
    }

    @Transaction
    suspend fun completeWithSnapshot(id: String, includeSubtasks: Boolean): TaskCompletionSnapshot {
        val task = checkNotNull(getTaskById(id)) { "Task not found" }
        val children = getSubtasksForTask(id)
        check(includeSubtasks || children.none { !it.isCompleted }) { "Pending subtasks require confirmation" }
        val snapshot = TaskCompletionSnapshot(
            TaskId(id), task.isCompleted,
            children.map { SubtaskId(it.id) to it.isCompleted }.toPersistentList(),
            expectedRevision = task.revision + 1L,
            previousRevision = task.revision,
        )
        updateTaskStatus(id, true)
        incrementRevision(id)
        if (includeSubtasks) completeSubtasks(id)
        return snapshot
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUndoOperation(operation: TaskUndoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUndoSubtasks(subtasks: List<TaskUndoSubtaskEntity>)

    /** Completion and its Undo record are committed as one aggregate transaction. */
    @Transaction
    suspend fun completeWithUndo(
        id: String,
        includeSubtasks: Boolean,
        operation: TaskUndoEntity,
        subtasks: List<TaskUndoSubtaskEntity>,
    ): TaskCompletionSnapshot {
        val snapshot = completeWithSnapshot(id, includeSubtasks)
        check(operation.taskId == id) { "Undo task does not match completion" }
        check(operation.expectedRevision == snapshot.expectedRevision) { "Undo revision mismatch" }
        insertUndoOperation(operation)
        if (subtasks.isNotEmpty()) insertUndoSubtasks(subtasks)
        return snapshot.copy(
            operationId = operation.operationId,
            createdElapsedMillis = operation.createdElapsedMillis,
            expiresAtElapsedMillis = operation.expiresAtElapsedMillis,
        )
    }

    @Transaction
    suspend fun restoreCompletion(snapshot: TaskCompletionSnapshot) {
        val id = snapshot.taskId.value
        checkNotNull(getTaskById(id)) { "Task not found" }
        val children = getSubtasksForTask(id)
        val values = snapshot.subtaskCompletion
        if (snapshot.expectedRevision > 0L) {
            check(taskRevision(id) == snapshot.expectedRevision) { "Task changed after completion" }
        }
        check(values.map { it.first }.distinct().size == values.size) { "Duplicate snapshot IDs" }
        check(children.map { it.id }.toSet() == values.map { it.first.value }.toSet()) {
            "Subtask structure changed"
        }
        updateTaskStatus(id, snapshot.taskWasCompleted)
        values.forEach { (childId, completed) -> updateSubtaskStatus(childId.value, completed) }
        if (snapshot.operationId != null) incrementRevision(id)
        else setRevision(id, snapshot.previousRevision)
    }

    @Query("UPDATE subtasks SET is_completed = :completed WHERE id = :id")
    suspend fun updateSubtaskStatus(id: String, completed: Boolean)

    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :id")
    suspend fun updateTaskStatus(id: String, isCompleted: Boolean)

    @Query("UPDATE tasks SET revision = revision + 1 WHERE id = :id")
    suspend fun incrementRevision(id: String)

    @Query("SELECT revision FROM tasks WHERE id = :id")
    suspend fun taskRevision(id: String): Long

    @Query("UPDATE tasks SET revision = :revision WHERE id = :id")
    suspend fun setRevision(id: String, revision: Long)

    @Query("UPDATE tasks SET due_date_epoch_days = :epochDays, revision = revision + 1 WHERE id = :id")
    suspend fun updateDueDate(id: String, epochDays: Long?): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskNoteCrossRef(crossRef: TaskNoteCrossRef)

    @Transaction
    suspend fun insertTaskWithNoteLink(task: TaskEntity, noteId: String) {
        upsertTask(task)
        insertTaskNoteCrossRef(TaskNoteCrossRef(taskId = task.id, noteId = noteId))
    }
}
