package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.dailyfocus.core.model.Priority

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["project_id"]),
        Index(value = ["milestone_id"])
    ]
)
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String? = null,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "due_date_epoch_days")
    val dueDateEpochDays: Long? = null,
    val priority: Priority,
    @ColumnInfo(name = "estimated_duration_seconds") val estimatedDurationSeconds: Long? = null,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    val revision: Long = 0L,
    @ColumnInfo(name = "project_id") val projectId: String? = null,
    @ColumnInfo(name = "milestone_id") val milestoneId: String? = null,
)
