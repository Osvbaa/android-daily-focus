package com.example.dailyfocus.features.tasks.ui.state

import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectMilestone
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class TaskEditorPhase { Loading, Editing, Saving, LoadError, Missing }
enum class DraftPersistenceState { NotPersisted, Persisting, Persisted, Error }

/** State shared by creation and editing; persistence is owned by repositories. */
data class TaskEditorUiState(
    val taskId: TaskId? = null,
    val draft: TaskDraft = TaskDraft(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val showDiscardConfirmation: Boolean = false,
    val titleError: Boolean = false,
    val errorMessage: String? = null,
    val phase: TaskEditorPhase = if (isLoading) TaskEditorPhase.Loading else TaskEditorPhase.Editing,
    val baseRevision: Long? = null,
    val draftPersistence: DraftPersistenceState = DraftPersistenceState.NotPersisted,
    val conflict: Boolean = false,
    val availableProjects: ImmutableList<Project> = persistentListOf(),
    val availableMilestones: ImmutableList<ProjectMilestone> = persistentListOf(),
)

sealed interface TaskEditorEvent {
    sealed interface DraftChange : TaskEditorEvent
    data class TitleChanged(val value: String) : DraftChange
    data class DescriptionChanged(val value: String) : DraftChange
    data class DueDateChanged(val epochDays: Long?) : DraftChange
    data class PriorityChanged(val priority: Priority) : DraftChange
    data class EstimatedDurationMinutesChanged(val minutes: Long?) : DraftChange
    data class ProjectChanged(val projectId: com.example.dailyfocus.core.model.ProjectId?) : DraftChange
    data class MilestoneChanged(val milestoneId: com.example.dailyfocus.core.model.MilestoneId?) : DraftChange

    sealed interface SubtaskChange : TaskEditorEvent
    data object AddSubtask : SubtaskChange
    data class SubtaskChanged(val index: Int, val value: String) : SubtaskChange
    data class ToggleSubtask(val index: Int) : SubtaskChange
    data class DeleteSubtask(val index: Int) : SubtaskChange
    data class MoveSubtask(val from: Int, val to: Int) : SubtaskChange
    data object Save : TaskEditorEvent
    data object Back : TaskEditorEvent
    data object ContinueEditing : TaskEditorEvent
    data object Discard : TaskEditorEvent
    data object Retry : TaskEditorEvent
    data object ReloadRemote : TaskEditorEvent
}

sealed interface TaskEditorEffect {
    data object NavigateBack : TaskEditorEffect
    data object RequestTitleFocus : TaskEditorEffect
    data object RequestDraftRetry : TaskEditorEffect
}
