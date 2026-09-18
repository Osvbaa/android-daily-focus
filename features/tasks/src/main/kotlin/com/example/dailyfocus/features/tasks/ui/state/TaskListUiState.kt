package com.example.dailyfocus.features.tasks.ui.state

import com.example.dailyfocus.core.model.ReschedulePreset
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.TaskUndoOperation
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/** Immutable state for the task list and quick capture. */
data class TaskListUiState(
    val isLoading: Boolean = true,
    val tasks: ImmutableList<Task> = persistentListOf(),
    val projectNames: ImmutableMap<String, String> = persistentMapOf(),
    val focusCompletedTaskIds: ImmutableSet<TaskId> = persistentSetOf(),
    val captureTitle: String = "",
    val captureExpanded: Boolean = false,
    val query: String = "",
    val showCompleted: Boolean = true,
    val pendingSubtaskResolution: TaskId? = null,
    val deleteConfirmation: TaskId? = null,
    val undoOperations: ImmutableList<TaskUndoOperation> = persistentListOf(),
    val errorMessage: String? = null,
)

sealed interface TaskListEvent {
    data class CaptureChanged(val value: String) : TaskListEvent
    data object CaptureFocused : TaskListEvent
    data object CaptureSubmitted : TaskListEvent
    data object CreateRequested : TaskListEvent
    data object ReminderRequested : TaskListEvent
    data class QueryChanged(val value: String) : TaskListEvent
    data object ToggleCompletedVisibility : TaskListEvent
    data class TaskSelected(val id: TaskId) : TaskListEvent
    data class SourceNoteRequested(val noteId: String) : TaskListEvent
    data class CompleteRequested(val id: TaskId) : TaskListEvent
    data class FocusRequested(val id: TaskId) : TaskListEvent
    data class CompleteAll(val id: TaskId) : TaskListEvent
    data object UndoCompletion : TaskListEvent
    data class UndoRequested(val operationId: String) : TaskListEvent
    data class Reschedule(val id: TaskId, val preset: ReschedulePreset) : TaskListEvent
    data class DeleteRequested(val id: TaskId) : TaskListEvent
    data object DeleteDismissed : TaskListEvent
    data object DeleteConfirmed : TaskListEvent
    data object Retry : TaskListEvent
}

sealed interface TaskListEffect {
    data class NavigateToEditor(val taskId: String? = null) : TaskListEffect
    data object NavigateToReminder : TaskListEffect
    data class NavigateToSourceNote(val noteId: String) : TaskListEffect
    data class NavigateToFocus(val taskId: String, val durationSeconds: Long) : TaskListEffect
    data class ShowUndo(val message: String, val operationId: String) : TaskListEffect
    data object RequestCaptureFocus : TaskListEffect
    data object PerformHaptic : TaskListEffect
}
