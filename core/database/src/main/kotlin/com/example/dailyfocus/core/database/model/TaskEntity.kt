package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.dailyfocus.core.model.Priority

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String?,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean,
    @ColumnInfo(name = "due_date_epoch_days")
    val dueDateEpochDays: Long?,
    val priority: Priority,
    @ColumnInfo(name = "created_at_millis")
    val createdAtMillis: Long,
    val revision: Long = 0L,
)
