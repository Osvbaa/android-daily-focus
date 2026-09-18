package com.example.dailyfocus.core.common.analytics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any>

    data class TaskCreated(
        val origin: TaskOrigin,
        val surface: InteractionSurface,
        val hasDueDate: Boolean,
        val isPrioritySet: Boolean,
        val durationMs: Long
    ) : AnalyticsEvent {
        override val name = "task_created"
        override val params = mapOf(
            "origin" to origin.name,
            "surface" to surface.name,
            "has_due_date" to hasDueDate,
            "is_priority_set" to isPrioritySet,
            "duration_ms" to durationMs
        )
    }

    data class TaskCompleted(
        val surface: InteractionSurface,
        val taskLifetimeHours: Int,
        val wasOverdue: Boolean
    ) : AnalyticsEvent {
        override val name = "task_completed"
        override val params = mapOf(
            "surface" to surface.name,
            "task_lifetime_hours" to taskLifetimeHours,
            "was_overdue" to wasOverdue
        )
    }

    data class TaskRescheduled(val surface: InteractionSurface) : AnalyticsEvent {
        override val name = "task_rescheduled"
        override val params = mapOf("surface" to surface.name)
    }

    data class TaskDeleted(val surface: InteractionSurface) : AnalyticsEvent {
        override val name = "task_deleted"
        override val params = mapOf("surface" to surface.name)
    }
}

enum class TaskOrigin {
    MANUAL_QUICK_INPUT,
    MANUAL_FULL_EDITOR,
    AI_NOTE,
    NOTE_TEXT_SELECTION
}

enum class InteractionSurface {
    APP_SCREEN,
    APP_SCREEN_RADIAL_MENU,
    APP_SCREEN_DATE_PICKER,
    APP_SCREEN_SWIPE,
    APP_SCREEN_DETAIL,
    NOTIFICATION_ACTION,
    QUICK_WIDGET
}

/**
 * Gestor de eventos diferidos con soporte para cancelación por reversión (Undo de 4 segundos).
 */
class DelayedAnalyticsDispatcher(
    private val tracker: AnalyticsTracker,
    private val scope: CoroutineScope
) {
    private val pendingCompletions = ConcurrentHashMap<String, Job>()

    fun scheduleTaskCompletion(
        taskId: String,
        event: AnalyticsEvent.TaskCompleted,
        undoWindowMillis: Long = 4000L
    ) {
        cancelPendingCompletion(taskId)
        val job = scope.launch {
            delay(undoWindowMillis)
            tracker.track(event)
            pendingCompletions.remove(taskId)
        }
        pendingCompletions[taskId] = job
    }

    fun cancelPendingCompletion(taskId: String) {
        pendingCompletions.remove(taskId)?.cancel()
    }
}
