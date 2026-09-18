package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_undo_operations")
data class TaskUndoEntity(
    @PrimaryKey @ColumnInfo(name = "operation_id") val operationId: String,
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "task_was_completed") val taskWasCompleted: Boolean,
    @ColumnInfo(name = "created_elapsed_millis") val createdElapsedMillis: Long,
    @ColumnInfo(name = "expires_at_elapsed_millis") val expiresAtElapsedMillis: Long,
    @ColumnInfo(name = "expected_revision") val expectedRevision: Long,
)
