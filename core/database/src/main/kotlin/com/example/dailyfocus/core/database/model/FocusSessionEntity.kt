package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "focus_sessions",
    indices = [Index(value = ["started_at_millis"])],
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "task_id") val taskId: String?,
    @ColumnInfo(name = "habit_id") val habitId: String?,
    @ColumnInfo(name = "habit_level") val habitLevel: String?,
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long,
    @ColumnInfo(name = "remaining_seconds") val remainingSeconds: Long,
    @ColumnInfo(name = "started_at_millis") val startedAtMillis: Long,
    @ColumnInfo(name = "deadline_at_millis") val deadlineAtMillis: Long? = null,
    @ColumnInfo(name = "completed_at_millis") val completedAtMillis: Long?,
    val status: String,
    @ColumnInfo(name = "target_decision") val targetDecision: String = "NONE",
)
