package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.TaskRevisionConflictException
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskDraftRecord
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.features.tasks.ui.state.DraftPersistenceState
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEffect
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorPhase
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskEditorViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val ids: IdGenerator,
    private val savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
) : ViewModel() {
    private val routeId = savedStateHandle.get<String>(TASK_ID)?.let(::TaskId)
    private val draftKey = routeId?.value ?: NEW_DRAFT_KEY
    private val mutableState = MutableStateFlow(
        TaskEditorUiState(taskId = routeId, isLoading = routeId != null),
    )
    val uiState: StateFlow<TaskEditorUiState> = mutableState.asStateFlow()
    private val effectChannel = Channel<TaskEditorEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private var draftSaveJob: Job? = null
    private var draftRevision: Long = 0L
    private var milestoneJob: Job? = null

    init {
        restoreOrLoad()
        viewModelScope.launch {
            projectRepository.observeProjects().collect { projects ->
                mutableState.update { it.copy(availableProjects = projects.toPersistentList()) }
                loadMilestones(uiState.value.draft.projectId)
            }
        }
    }

    fun onEvent(event: TaskEditorEvent) {
        when (event) {
            is TaskEditorEvent.DraftChange -> if (canEdit()) updateDraft(event)
            is TaskEditorEvent.SubtaskChange -> if (canEdit()) updateSubtasks(event)
            TaskEditorEvent.Save -> save()
            TaskEditorEvent.Back -> requestBack()
            TaskEditorEvent.ContinueEditing -> mutableState.update { it.copy(showDiscardConfirmation = false) }
            TaskEditorEvent.Discard -> discard()
            TaskEditorEvent.Retry -> restoreOrLoad()
            TaskEditorEvent.ReloadRemote -> reloadRemote()
        }
    }

    private fun canEdit() = !uiState.value.isLoading && !uiState.value.isSaving && !uiState.value.conflict

    private fun updateDraft(event: TaskEditorEvent.DraftChange) {
        when (event) {
            is TaskEditorEvent.TitleChanged -> updateDraft { copy(title = event.value) }
            is TaskEditorEvent.DescriptionChanged -> updateDraft { copy(description = event.value) }
            is TaskEditorEvent.DueDateChanged -> updateDraft { copy(dueDateEpochDays = event.epochDays) }
            is TaskEditorEvent.PriorityChanged -> updateDraft { copy(priority = event.priority) }
            is TaskEditorEvent.EstimatedDurationMinutesChanged -> {
                if (event.minutes != null && event.minutes !in 1..480) {
                    mutableState.update { it.copy(errorMessage = "La duración debe estar entre 1 y 480 minutos") }
                    return
                }
                val seconds = event.minutes?.times(60L)
                updateDraft { copy(estimatedDurationSeconds = seconds) }
            }
            is TaskEditorEvent.ProjectChanged -> {
                updateDraft { copy(projectId = event.projectId, milestoneId = null) }
                loadMilestones(event.projectId)
            }
            is TaskEditorEvent.MilestoneChanged -> updateDraft { copy(milestoneId = event.milestoneId) }
        }
    }

    private fun updateSubtasks(event: TaskEditorEvent.SubtaskChange) {
        when (event) {
            TaskEditorEvent.AddSubtask -> updateDraft {
                copy(
                    subtasks = (subtasks + Subtask(
                        id = SubtaskId(ids.nextId()),
                        taskId = routeId ?: TaskId(DRAFT_TASK_ID),
                        title = "",
                        position = subtasks.size,
                    )).toPersistentList(),
                )
            }
            is TaskEditorEvent.SubtaskChanged -> updateSubtask(event.index) { copy(title = event.value) }
            is TaskEditorEvent.ToggleSubtask -> updateSubtask(event.index) { copy(isCompleted = !isCompleted) }
            is TaskEditorEvent.DeleteSubtask -> updateDraft {
                copy(
                    subtasks = subtasks.filterIndexed { index, _ -> index != event.index }
                        .mapIndexed { index, item -> item.copy(position = index) }
                        .toPersistentList(),
                )
            }
            is TaskEditorEvent.MoveSubtask -> move(event.from, event.to)
        }
    }

    private fun restoreOrLoad() {
        mutableState.update {
            it.copy(
                isLoading = routeId != null,
                phase = if (routeId != null) TaskEditorPhase.Loading else TaskEditorPhase.Editing,
                errorMessage = null,
                conflict = false,
            )
        }
        viewModelScope.launch {
            runCatching {
                val storedTask = routeId?.let { repository.getTask(it) }
                val durableDraft = repository.getDraft(draftKey)
                val fallbackDraft = restoreHandleDraft()
                val discardPending = savedStateHandle.get<Boolean>(DISCARD_PENDING) == true
                when {
                    discardPending -> {
                        clearPersistedHandle()
                        mutableState.value = TaskEditorUiState(taskId = routeId, baseRevision = storedTask?.revision)
                    }
                    durableDraft != null -> loadedDraft(durableDraft, storedTask)
                    fallbackDraft != null -> {
                        val record = TaskDraftRecord(
                            key = draftKey,
                            taskId = routeId,
                            draft = fallbackDraft,
                            baseRevision = storedTask?.revision,
                            draftRevision = 1L,
                        )
                        loadedDraft(record, storedTask)
                        persistDraft(record)
                    }
                    storedTask != null -> {
                        draftRevision = 0L
                        mutableState.value = TaskEditorUiState(
                            taskId = routeId,
                            draft = TaskDraft(
                                title = storedTask.title,
                                description = storedTask.description.orEmpty(),
                                dueDateEpochDays = storedTask.dueDateEpochDays,
                                priority = storedTask.priority,
                                estimatedDurationSeconds = storedTask.estimatedDurationSeconds,
                                projectId = storedTask.projectId,
                                milestoneId = storedTask.milestoneId,
                                subtasks = storedTask.subtasks,
                            ),
                            baseRevision = storedTask.revision,
                        )
                    }
                    routeId != null -> mutableState.update {
                        it.copy(
                            isLoading = false,
                            phase = TaskEditorPhase.Missing,
                            errorMessage = "Tarea no encontrada",
                        )
                    }
                    else -> effectChannel.trySend(TaskEditorEffect.RequestTitleFocus)
                }
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        phase = TaskEditorPhase.LoadError,
                        errorMessage = error.message ?: "No se pudo cargar el editor",
                    )
                }
            }
        }
    }

    private fun loadedDraft(record: TaskDraftRecord, storedTask: Task?) {
        draftRevision = record.draftRevision
        val conflict = storedTask != null && record.baseRevision != null && storedTask.revision != record.baseRevision
        mutableState.value = TaskEditorUiState(
            taskId = routeId,
            draft = record.draft,
            isLoading = false,
            isDirty = true,
            phase = TaskEditorPhase.Editing,
            baseRevision = record.baseRevision ?: storedTask?.revision,
            draftPersistence = DraftPersistenceState.Persisted,
            conflict = conflict,
            errorMessage = if (conflict) "La tarea cambió en otra superficie" else null,
        )
    }

    private fun updateDraft(transform: TaskDraft.() -> TaskDraft) {
        val current = uiState.value.draft
        val next = current.transform()
        if (next == current) return
        val revision = draftRevision + 1L
        draftRevision = revision
        mutableState.update {
            it.copy(
                draft = next,
                isDirty = true,
                titleError = false,
                errorMessage = null,
                phase = TaskEditorPhase.Editing,
                draftPersistence = DraftPersistenceState.Persisting,
            )
        }
        persistDraft(
            TaskDraftRecord(
                key = draftKey,
                taskId = routeId,
                draft = next,
                baseRevision = uiState.value.baseRevision,
                draftRevision = revision,
            ),
        )
    }

    private fun updateSubtask(index: Int, transform: Subtask.() -> Subtask) {
        if (index !in uiState.value.draft.subtasks.indices) return
        updateDraft {
            copy(
                subtasks = subtasks.mapIndexed { i, item -> if (i == index) item.transform() else item }
                    .toPersistentList(),
            )
        }
    }

    private fun move(from: Int, to: Int) {
        val subtasks = uiState.value.draft.subtasks
        if (from !in subtasks.indices || to !in subtasks.indices || from == to) return
        updateDraft {
            val items = subtasks.toMutableList()
            val moved = items.removeAt(from)
            items.add(to, moved)
            copy(subtasks = items.mapIndexed { index, item -> item.copy(position = index) }.toPersistentList())
        }
    }

    private fun save() {
        val state = uiState.value
        if (!state.canSave()) return
        if (state.draft.title.isBlank()) {
            mutableState.update { it.copy(titleError = true) }
            effectChannel.trySend(TaskEditorEffect.RequestTitleFocus)
            return
        }
        viewModelScope.launch {
            draftSaveJob?.join()
            mutableState.update { it.copy(isSaving = true, phase = TaskEditorPhase.Saving, errorMessage = null) }
            when (val result = repository.saveTask(routeId, state.draft, state.baseRevision)) {
                is TaskMutationResult.Success -> {
                    repository.deleteDraft(draftKey)
                    clearPersistedHandle()
                    mutableState.update { it.copy(isSaving = false, isDirty = false, phase = TaskEditorPhase.Editing) }
                    effectChannel.send(TaskEditorEffect.NavigateBack)
                }
                is TaskMutationResult.Failure -> {
                    val conflict = result.cause is TaskRevisionConflictException
                    mutableState.update {
                        it.copy(
                            isSaving = false,
                            phase = TaskEditorPhase.Editing,
                            conflict = conflict,
                            errorMessage = result.cause.message ?: "No se pudo guardar",
                            draftPersistence = DraftPersistenceState.Persisted,
                        )
                    }
                }
            }
        }
    }

    private fun TaskEditorUiState.canSave(): Boolean =
        !isLoading && !isSaving && phase != TaskEditorPhase.Missing && !conflict

    private fun requestBack() {
        val state = uiState.value
        if (state.isSaving || state.isLoading) return
        if (state.isDirty) mutableState.update { it.copy(showDiscardConfirmation = true) }
        else effectChannel.trySend(TaskEditorEffect.NavigateBack)
    }

    private fun reloadRemote() {
        if (!uiState.value.conflict) return
        clearPersistedHandle()
        viewModelScope.launch {
            repository.deleteDraft(draftKey)
            restoreOrLoad()
        }
    }

    private fun discard() {
        savedStateHandle[DISCARD_PENDING] = true
        clearPersistedHandle()
        viewModelScope.launch {
            draftSaveJob?.join()
            when (val result = repository.deleteDraft(draftKey)) {
                is TaskMutationResult.Success -> {
                    effectChannel.send(TaskEditorEffect.NavigateBack)
                }
                is TaskMutationResult.Failure -> mutableState.update {
                    it.copy(errorMessage = result.cause.message ?: "No se pudo descartar el borrador")
                }
            }
        }
    }

    private fun persistDraft(record: TaskDraftRecord) {
        savedStateHandle[TITLE] = record.draft.title
        savedStateHandle[DESCRIPTION] = record.draft.description
        record.draft.dueDateEpochDays?.let { savedStateHandle[DUE_DATE] = it } ?: savedStateHandle.remove<Long>(DUE_DATE)
        savedStateHandle[PRIORITY] = record.draft.priority.name
        record.draft.estimatedDurationSeconds?.let { savedStateHandle[ESTIMATED_DURATION_SECONDS] = it }
            ?: savedStateHandle.remove<Long>(ESTIMATED_DURATION_SECONDS)
        record.draft.projectId?.let { savedStateHandle[PROJECT_ID] = it.value } ?: savedStateHandle.remove<String>(PROJECT_ID)
        record.draft.milestoneId?.let { savedStateHandle[MILESTONE_ID] = it.value } ?: savedStateHandle.remove<String>(MILESTONE_ID)
        savedStateHandle[SUBTASK_IDS] = ArrayList(record.draft.subtasks.map { it.id.value })
        savedStateHandle[SUBTASK_TITLES] = ArrayList(record.draft.subtasks.map { it.title })
        savedStateHandle[SUBTASK_COMPLETION] = record.draft.subtasks.map { it.isCompleted }.toBooleanArray()
        draftSaveJob?.cancel()
        draftSaveJob = viewModelScope.launch {
            when (repository.saveDraft(record)) {
                is TaskMutationResult.Success -> mutableState.update {
                    if (it.draft == record.draft) it.copy(draftPersistence = DraftPersistenceState.Persisted) else it
                }
                is TaskMutationResult.Failure -> mutableState.update {
                    if (it.draft == record.draft) it.copy(draftPersistence = DraftPersistenceState.Error) else it
                }
            }
        }
    }

    private fun restoreHandleDraft(): TaskDraft? {
        val title = savedStateHandle.get<String>(TITLE) ?: return null
        val ids = savedStateHandle.get<ArrayList<String>>(SUBTASK_IDS).orEmpty()
        val titles = savedStateHandle.get<ArrayList<String>>(SUBTASK_TITLES).orEmpty()
        val completion = savedStateHandle.get<BooleanArray>(SUBTASK_COMPLETION) ?: BooleanArray(0)
        val parentId = routeId ?: TaskId(DRAFT_TASK_ID)
        val subtasks = ids.indices.mapNotNull { index ->
            val subtaskTitle = titles.getOrNull(index) ?: return@mapNotNull null
            Subtask(
                id = SubtaskId(ids[index]),
                taskId = parentId,
                title = subtaskTitle,
                isCompleted = completion.getOrElse(index) { false },
                position = index,
            )
        }.toPersistentList()
        return TaskDraft(
            title = title,
            description = savedStateHandle[DESCRIPTION] ?: "",
            dueDateEpochDays = savedStateHandle[DUE_DATE],
            priority = savedStateHandle.get<String>(PRIORITY)?.let { runCatching { Priority.valueOf(it) }.getOrNull() }
                ?: Priority.NONE,
            estimatedDurationSeconds = savedStateHandle[ESTIMATED_DURATION_SECONDS],
            projectId = savedStateHandle.get<String>(PROJECT_ID)?.let(::ProjectId),
            milestoneId = savedStateHandle.get<String>(MILESTONE_ID)?.let(::MilestoneId),
            subtasks = subtasks,
        )
    }

    private fun clearPersistedHandle() {
        savedStateHandle.remove<String>(TITLE)
        savedStateHandle.remove<String>(DESCRIPTION)
        savedStateHandle.remove<Long>(DUE_DATE)
        savedStateHandle.remove<String>(PRIORITY)
        savedStateHandle.remove<Long>(ESTIMATED_DURATION_SECONDS)
        savedStateHandle.remove<String>(PROJECT_ID)
        savedStateHandle.remove<String>(MILESTONE_ID)
        savedStateHandle.remove<ArrayList<String>>(SUBTASK_IDS)
        savedStateHandle.remove<ArrayList<String>>(SUBTASK_TITLES)
        savedStateHandle.remove<BooleanArray>(SUBTASK_COMPLETION)
    }

    companion object {
        private const val TASK_ID = "taskId"
        private const val NEW_DRAFT_KEY = "new-task"
        private const val DRAFT_TASK_ID = "draft"
        private const val TITLE = "editor_title"
        private const val DESCRIPTION = "editor_description"
        private const val DUE_DATE = "editor_due_date"
        private const val PRIORITY = "editor_priority"
        private const val ESTIMATED_DURATION_SECONDS = "editor_estimated_duration_seconds"
        private const val PROJECT_ID = "editor_project_id"
        private const val MILESTONE_ID = "editor_milestone_id"
        private const val SUBTASK_IDS = "editor_subtask_ids"
        private const val SUBTASK_TITLES = "editor_subtask_titles"
        private const val SUBTASK_COMPLETION = "editor_subtask_completion"
        private const val DISCARD_PENDING = "editor_discard_pending"
    }

    private fun loadMilestones(projectId: com.example.dailyfocus.core.model.ProjectId?) {
        milestoneJob?.cancel()
        if (projectId == null) {
            mutableState.update { it.copy(availableMilestones = persistentListOf()) }
            return
        }
        milestoneJob = viewModelScope.launch {
            projectRepository.observeProject(projectId).collect { details ->
                mutableState.update { it.copy(availableMilestones = details?.milestones ?: persistentListOf()) }
            }
        }
    }
}
