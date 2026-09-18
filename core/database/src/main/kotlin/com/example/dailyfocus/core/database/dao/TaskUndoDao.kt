package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dailyfocus.core.database.model.TaskUndoEntity
import com.example.dailyfocus.core.database.model.TaskUndoSubtaskEntity
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.TaskUndoOperation
import kotlinx.collections.immutable.toPersistentList

@Dao
interface TaskUndoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: TaskUndoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<TaskUndoSubtaskEntity>)

    @Query("SELECT * FROM task_undo_operations WHERE operation_id = :operationId")
    suspend fun getOperation(operationId: String): TaskUndoEntity?

    @Query("SELECT * FROM task_undo_subtasks WHERE operation_id = :operationId")
    suspend fun getSubtasks(operationId: String): List<TaskUndoSubtaskEntity>

    @Query("DELETE FROM task_undo_subtasks WHERE operation_id = :operationId")
    suspend fun deleteSubtasks(operationId: String)

    @Query("DELETE FROM task_undo_operations WHERE operation_id = :operationId")
    suspend fun deleteOperation(operationId: String): Int

    @Query("SELECT * FROM task_undo_operations WHERE expires_at_elapsed_millis <= :now OR created_elapsed_millis > :now ORDER BY expires_at_elapsed_millis ASC")
    suspend fun expired(now: Long): List<TaskUndoEntity>

    @Query("SELECT * FROM task_undo_operations ORDER BY created_elapsed_millis ASC")
    suspend fun all(): List<TaskUndoEntity>

    @Transaction
    suspend fun getRecord(operationId: String): TaskUndoOperation? {
        val entity = getOperation(operationId) ?: return null
        return TaskUndoOperation(
            operationId = entity.operationId,
            taskId = TaskId(entity.taskId),
            taskWasCompleted = entity.taskWasCompleted,
            subtaskCompletion = getSubtasks(operationId)
                .map { SubtaskId(it.subtaskId) to it.wasCompleted }
                .toPersistentList(),
            createdElapsedMillis = entity.createdElapsedMillis,
            expiresAtElapsedMillis = entity.expiresAtElapsedMillis,
            expectedRevision = entity.expectedRevision,
        )
    }

    @Transaction
    suspend fun insertRecord(operation: TaskUndoOperation) {
        insertOperation(
            TaskUndoEntity(
                operationId = operation.operationId,
                taskId = operation.taskId.value,
                taskWasCompleted = operation.taskWasCompleted,
                createdElapsedMillis = operation.createdElapsedMillis,
                expiresAtElapsedMillis = operation.expiresAtElapsedMillis,
                expectedRevision = operation.expectedRevision,
            ),
        )
        deleteSubtasks(operation.operationId)
        if (operation.subtaskCompletion.isNotEmpty()) {
            insertSubtasks(operation.subtaskCompletion.map { (id, completed) ->
                TaskUndoSubtaskEntity(operation.operationId, id.value, completed)
            })
        }
    }
}
