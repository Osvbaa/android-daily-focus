package com.example.dailyfocus.core.database.model

import com.example.dailyfocus.core.model.FocusSession
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.TaskId

fun FocusSessionEntity.asExternalModel() = FocusSession(
    id = id,
    taskId = taskId?.let(::TaskId),
    habitId = habitId?.let(::HabitId),
    habitLevel = habitLevel?.let { runCatching { HabitCompletionLevel.valueOf(it) }.getOrNull() },
    durationSeconds = durationSeconds,
    remainingSeconds = remainingSeconds,
    startedAtMillis = startedAtMillis,
    deadlineAtMillis = deadlineAtMillis,
    completedAtMillis = completedAtMillis,
    status = runCatching { FocusSessionStatus.valueOf(status) }.getOrDefault(FocusSessionStatus.CANCELED),
    targetDecision = runCatching { FocusTargetDecision.valueOf(targetDecision) }.getOrDefault(FocusTargetDecision.NONE),
)

fun FocusSession.asEntity() = FocusSessionEntity(
    id = id,
    taskId = taskId?.value,
    habitId = habitId?.value,
    habitLevel = habitLevel?.name,
    durationSeconds = durationSeconds,
    remainingSeconds = remainingSeconds,
    startedAtMillis = startedAtMillis,
    deadlineAtMillis = deadlineAtMillis,
    completedAtMillis = completedAtMillis,
    status = status.name,
    targetDecision = targetDecision.name,
)
