package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "task_draft_subtasks", primaryKeys = ["draft_key", "id"])
data class TaskDraftSubtaskEntity(
    @ColumnInfo(name = "draft_key") val draftKey: String,
    val id: String,
    val title: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    val position: Int,
)
