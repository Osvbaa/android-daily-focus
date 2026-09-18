package com.example.dailyfocus.core.database.model

import androidx.room.Entity

@Entity(tableName = "task_reminder_settings")
data class ReminderSettingsEntity(
    @androidx.room.PrimaryKey val id: Int = 0,
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val lastPublishedEpochDay: Long? = null,
)
