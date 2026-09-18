package com.example.dailyfocus.features.tasks.ui.state

import com.example.dailyfocus.core.model.TaskReminderSettings

data class TaskReminderUiState(
    val settings: TaskReminderSettings = TaskReminderSettings(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface TaskReminderEvent {
    data class SetEnabled(val enabled: Boolean) : TaskReminderEvent
    data class SetTime(val hour: Int, val minute: Int) : TaskReminderEvent
    data object Retry : TaskReminderEvent
}
