package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo

/** Flat projection used to keep task/subtask joins explicit across module boundaries. */
data class TaskRow(
    @ColumnInfo(name = "task_id") val taskId: String,
    @ColumnInfo(name = "task_title") val taskTitle: String,
    @ColumnInfo(name = "task_description") val taskDescription: String?,
    @ColumnInfo(name = "task_is_completed") val taskIsCompleted: Boolean,
    @ColumnInfo(name = "task_due_date_epoch_days") val taskDueDateEpochDays: Long?,
    @ColumnInfo(name = "task_priority") val taskPriority: String,
    @ColumnInfo(name = "task_estimated_duration_seconds") val taskEstimatedDurationSeconds: Long?,
    @ColumnInfo(name = "task_created_at_millis") val taskCreatedAtMillis: Long,
    @ColumnInfo(name = "task_revision") val taskRevision: Long,
    @ColumnInfo(name = "subtask_id") val subtaskId: String?,
    @ColumnInfo(name = "subtask_title") val subtaskTitle: String?,
    @ColumnInfo(name = "subtask_is_completed") val subtaskIsCompleted: Boolean?,
    @ColumnInfo(name = "subtask_position") val subtaskPosition: Int?,
    @ColumnInfo(name = "linked_note_id") val linkedNoteId: String? = null,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "milestone_id") val milestoneId: String? = null,
)
