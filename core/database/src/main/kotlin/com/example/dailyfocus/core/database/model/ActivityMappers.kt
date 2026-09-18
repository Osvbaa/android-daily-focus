package com.example.dailyfocus.core.database.model

import com.example.dailyfocus.core.model.ActivityEvent
import com.example.dailyfocus.core.model.StreakEventType

fun ActivityEventEntity.asExternalModel() = ActivityEvent(
    idempotencyKey = idempotencyKey,
    type = runCatching { StreakEventType.valueOf(type) }.getOrDefault(StreakEventType.TASK),
    sourceId = sourceId,
    epochDay = epochDay,
    points = points,
    occurredAtMillis = occurredAtMillis,
    rulesVersion = rulesVersion,
)

fun ActivityEvent.asEntity(category: String) = ActivityEventEntity(
    idempotencyKey = idempotencyKey,
    type = type.name,
    sourceId = sourceId,
    epochDay = epochDay,
    category = category,
    points = points,
    occurredAtMillis = occurredAtMillis,
    rulesVersion = rulesVersion,
)
