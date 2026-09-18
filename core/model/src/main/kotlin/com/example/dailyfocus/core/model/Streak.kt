package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

/** Any meaningful focus action contributes one event to the user's streak. */
@Serializable
enum class StreakEventType {
    TASK,
    PROJECT_TASK,
    PROJECT_MILESTONE,
    HABIT_LEVEL,
    POMODORO,
}

@Serializable
data class StreakEvent(
    val type: StreakEventType,
    val epochDay: Long,
    val sourceId: String? = null,
)
