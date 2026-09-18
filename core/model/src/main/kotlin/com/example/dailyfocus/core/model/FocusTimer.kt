package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class FocusTimerPhase { IDLE, RUNNING, PAUSED, COMPLETED }

@Serializable
enum class FocusSessionStatus { RUNNING, PAUSED, COMPLETED, CANCELED }

@Serializable
enum class FocusTargetDecision { NONE, PENDING, CONFIRMED, DISMISSED }

@Serializable
data class FocusSession(
    val id: String,
    val taskId: TaskId? = null,
    val habitId: HabitId? = null,
    val habitLevel: HabitCompletionLevel? = null,
    val durationSeconds: Long,
    val remainingSeconds: Long = durationSeconds,
    val startedAtMillis: Long,
    /** Wall-clock deadline persisted once per user action; never updated on every tick. */
    val deadlineAtMillis: Long? = null,
    val completedAtMillis: Long? = null,
    val status: FocusSessionStatus = FocusSessionStatus.RUNNING,
    val targetDecision: FocusTargetDecision = FocusTargetDecision.NONE,
)

@Serializable
data class FocusTimerState(
    val phase: FocusTimerPhase = FocusTimerPhase.IDLE,
    val durationSeconds: Long = 25 * 60L,
    val remainingSeconds: Long = 25 * 60L,
    val completedSessions: Int = 0,
    /** Mutually exclusive optional target for automatic completion at timer end. */
    val linkedTaskId: TaskId? = null,
    val linkedHabitId: HabitId? = null,
    val linkedHabitLevel: HabitCompletionLevel? = null,
) {
    fun start(): FocusTimerState = when (phase) {
        FocusTimerPhase.IDLE, FocusTimerPhase.PAUSED -> copy(phase = FocusTimerPhase.RUNNING)
        else -> this
    }

    fun pause(): FocusTimerState = if (phase == FocusTimerPhase.RUNNING) copy(phase = FocusTimerPhase.PAUSED) else this

    fun reset(): FocusTimerState = copy(phase = FocusTimerPhase.IDLE, remainingSeconds = durationSeconds)

    fun tick(): FocusTimerState = when {
        phase != FocusTimerPhase.RUNNING -> this
        remainingSeconds > 1L -> copy(remainingSeconds = remainingSeconds - 1L)
        else -> copy(phase = FocusTimerPhase.COMPLETED, remainingSeconds = 0L, completedSessions = completedSessions + 1)
    }
}
