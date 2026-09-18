package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_drafts")
data class TaskDraftEntity(
    @PrimaryKey val key: String,
    @ColumnInfo(name = "task_id") val taskId: String?,
    val title: String,
    val description: String,
    @ColumnInfo(name = "due_date_epoch_days") val dueDateEpochDays: Long?,
    val priority: String,
    @ColumnInfo(name = "estimated_duration_seconds") val estimatedDurationSeconds: Long? = null,
    @ColumnInfo(name = "base_revision") val baseRevision: Long?,
    @ColumnInfo(name = "draft_revision") val draftRevision: Long,
    @ColumnInfo(name = "updated_at_millis") val updatedAtMillis: Long,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "milestone_id") val milestoneId: String? = null,
)
