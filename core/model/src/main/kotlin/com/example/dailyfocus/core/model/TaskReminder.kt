package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@Serializable
data class TaskReminderSettings(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0,
    val lastPublishedEpochDay: Long? = null,
) {
    init {
        require(hour in 0..23) { "Hour must be between 0 and 23" }
        require(minute in 0..59) { "Minute must be between 0 and 59" }
    }
}
