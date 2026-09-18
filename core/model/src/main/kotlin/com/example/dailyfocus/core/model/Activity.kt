package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

/** A durable, idempotent record of a meaningful action in the focus loop. */
@Serializable
data class ActivityEvent(
    val idempotencyKey: String,
    val type: StreakEventType,
    val sourceId: String? = null,
    val epochDay: Long,
    val points: Int,
    val occurredAtMillis: Long,
    val rulesVersion: Int = 1,
)

@Serializable
data class ActivitySummary(
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalPoints: Int = 0,
    val todayPoints: Int = 0,
)

