package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "task_undo_subtasks", primaryKeys = ["operation_id", "subtask_id"])
data class TaskUndoSubtaskEntity(
    @ColumnInfo(name = "operation_id") val operationId: String,
    @ColumnInfo(name = "subtask_id") val subtaskId: String,
    @ColumnInfo(name = "was_completed") val wasCompleted: Boolean,
)
