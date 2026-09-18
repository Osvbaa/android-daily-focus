package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.analytics.*
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.common.time.MonotonicClock
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.TaskUndoResult
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.data.repository.FocusSessionRepository
import com.example.dailyfocus.core.model.*
import com.example.dailyfocus.features.tasks.domain.TaskReschedulePolicy
import com.example.dailyfocus.features.tasks.ui.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val analytics: AnalyticsTracker,
    private val dates: DateProvider,
    private val wallClock: WallClock,
    private val monotonicClock: MonotonicClock,
    private val savedStateHandle: SavedStateHandle,
    private val projectRepository: ProjectRepository,
    private val activityRepository: ActivityRepository,
    private val focusSessionRepository: FocusSessionRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(TaskListUiState(captureTitle = savedStateHandle[CAPTURE] ?: ""))
    val uiState: StateFlow<TaskListUiState> = mutableState.asStateFlow()
    private val effectChannel = Channel<TaskListEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var undoSnapshot: TaskCompletionSnapshot? = null
    private val undoJobs = mutableMapOf<String, Job>()
    private var operationSequence: Long = 0L

    init {
        observeTasks()
        viewModelScope.launch {
            repository.observeUndoOperations().collect { operations ->
                mutableState.update { it.copy(undoOperations = operations.toPersistentList()) }
            }
        }
    }
    /** Exhaustive UDF reducer for the task surface's public intents. */
    @Suppress("CyclomaticComplexMethod")
    fun onEvent(event: TaskListEvent) { when (event) {
        is TaskListEvent.CaptureChanged -> { savedStateHandle[CAPTURE] = event.value; mutableState.update { it.copy(captureTitle = event.value, captureExpanded = true) } }
        TaskListEvent.CaptureFocused -> mutableState.update { it.copy(captureExpanded = true) }
        TaskListEvent.CaptureSubmitted -> createTask()
        TaskListEvent.CreateRequested -> effectChannel.trySend(TaskListEffect.NavigateToEditor())
        TaskListEvent.ReminderRequested -> effectChannel.trySend(TaskListEffect.NavigateToReminder)
        is TaskListEvent.QueryChanged -> mutableState.update { it.copy(query = event.value) }
        TaskListEvent.ToggleCompletedVisibility -> mutableState.update { it.copy(showCompleted = !it.showCompleted) }
        is TaskListEvent.TaskSelected -> effectChannel.trySend(TaskListEffect.NavigateToEditor(event.id.value))
        is TaskListEvent.SourceNoteRequested -> effectChannel.trySend(TaskListEffect.NavigateToSourceNote(event.noteId))
        is TaskListEvent.CompleteRequested -> requestCompletion(event.id, false)
        is TaskListEvent.FocusRequested -> requestFocus(event.id)
        is TaskListEvent.CompleteAll -> requestCompletion(event.id, true)
        TaskListEvent.UndoCompletion -> undo()
        is TaskListEvent.UndoRequested -> undo(event.operationId)
        is TaskListEvent.Reschedule -> reschedule(event.id, event.preset)
        is TaskListEvent.DeleteRequested -> mutableState.update { it.copy(deleteConfirmation = event.id) }
        TaskListEvent.DeleteDismissed -> mutableState.update { it.copy(deleteConfirmation = null) }
        TaskListEvent.DeleteConfirmed -> deleteConfirmed()
        TaskListEvent.Retry -> observeTasks()
    } }

    private fun observeTasks() { viewModelScope.launch { combine(repository.observeTasks(), projectRepository.observeProjects(), focusSessionRepository.observeCompletedTaskIds()) { tasks, projects, completedFocusTasks ->
        Triple(tasks, projects.associate { it.id.value to it.name }, completedFocusTasks)
    }.catch { error -> mutableState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudieron cargar las tareas") } }.collect { (tasks, projects, completedFocusTasks) ->
        mutableState.update { it.copy(isLoading = false, tasks = tasks.toPersistentList(), projectNames = projects.toPersistentMap(), focusCompletedTaskIds = completedFocusTasks.toPersistentSet(), errorMessage = null) }
    } } }
    private fun createTask() { val title = uiState.value.captureTitle; if (title.isBlank()) { effectChannel.trySend(TaskListEffect.RequestCaptureFocus); return }; viewModelScope.launch {
        val started = monotonicClock.elapsedRealtimeMillis(); val result = repository.saveTask(null, TaskDraft(title = title, dueDateEpochDays = dates.today().toEpochDay()))
        if (result is TaskMutationResult.Success) { analytics.track(AnalyticsEvent.TaskCreated(TaskOrigin.MANUAL_QUICK_INPUT, InteractionSurface.APP_SCREEN, true, false, monotonicClock.elapsedRealtimeMillis() - started)); savedStateHandle[CAPTURE] = ""; mutableState.update { it.copy(captureTitle = "", captureExpanded = false) } } else showError(result)
    } }
    private fun requestFocus(id: TaskId) {
        val task = uiState.value.tasks.firstOrNull { it.id == id } ?: return
        effectChannel.trySend(TaskListEffect.NavigateToFocus(id.value, task.estimatedDurationSeconds ?: DEFAULT_FOCUS_SECONDS))
    }
    private fun requestCompletion(id: TaskId, includeChildren: Boolean) {
        val task = uiState.value.tasks.firstOrNull { it.id == id } ?: return
        if (!includeChildren && task.subtasks.any { !it.isCompleted }) {
            mutableState.update { it.copy(pendingSubtaskResolution = id) }
            return
        }
        viewModelScope.launch {
            val operationId = "${id.value}-${monotonicClock.elapsedRealtimeMillis()}-${++operationSequence}"
            repository.completeTaskWithUndo(id, includeChildren, operationId, monotonicClock.elapsedRealtimeMillis(), UNDO_MS)
                .onSuccess { operation ->
                    undoSnapshot = TaskCompletionSnapshot(
                        taskId = operation.taskId,
                        taskWasCompleted = operation.taskWasCompleted,
                        subtaskCompletion = operation.subtaskCompletion,
                        operationId = operation.operationId,
                        createdElapsedMillis = operation.createdElapsedMillis,
                        expiresAtElapsedMillis = operation.expiresAtElapsedMillis,
                        expectedRevision = operation.expectedRevision,
                    )
                    mutableState.update { it.copy(pendingSubtaskResolution = null) }
                    effectChannel.send(TaskListEffect.ShowUndo("Tarea completada", operation.operationId))
                    undoJobs[operation.operationId]?.cancel()
                    undoJobs[operation.operationId] = launch {
                        delay(UNDO_MS)
                        repository.consumeExpiredUndo(monotonicClock.elapsedRealtimeMillis())
                            .filter { it.operationId == operation.operationId }
                            .forEach { completed ->
                                val taskAtCompletion = repository.getTask(completed.taskId) ?: return@forEach
                                taskAtCompletion.projectId?.let { projectId ->
                                    projectRepository.reconcileProgress(projectId).getOrNull().orEmpty().forEach { milestoneId ->
                                        activityRepository.recordCompletion(
                                            type = StreakEventType.PROJECT_MILESTONE,
                                            sourceId = milestoneId.value,
                                            epochDay = dates.today().toEpochDay(),
                                        )
                                    }
                                }
                                activityRepository.recordCompletion(
                                    type = if (taskAtCompletion.projectId == null) StreakEventType.TASK else StreakEventType.PROJECT_TASK,
                                    sourceId = taskAtCompletion.id.value,
                                    epochDay = dates.today().toEpochDay(),
                                )
                                analytics.track(
                                    AnalyticsEvent.TaskCompleted(
                                        InteractionSurface.APP_SCREEN,
                                        ((wallClock.currentTimeMillis() - taskAtCompletion.createdAtMillis) / 3_600_000L)
                                            .coerceAtLeast(0).toInt(),
                                        taskAtCompletion.dueDateEpochDays?.let { it < dates.today().toEpochDay() } == true,
                                    ),
                                )
                            }
                        undoJobs.remove(operation.operationId)
                    }
                }
                .onFailure { mutableState.update { state -> state.copy(errorMessage = it.message) } }
        }
    }

    private fun undo(operationId: String? = null) {
        val id = operationId ?: uiState.value.undoOperations.lastOrNull()?.operationId ?: undoSnapshot?.operationId
        if (id == null) return
        undoJobs.remove(id)?.cancel()
        viewModelScope.launch {
            when (val result = repository.restoreUndo(id, monotonicClock.elapsedRealtimeMillis())) {
                is TaskUndoResult.Success -> mutableState.update { it.copy(errorMessage = null) }
                TaskUndoResult.Expired -> mutableState.update { it.copy(errorMessage = "La ventana de Deshacer expiró") }
                TaskUndoResult.Conflict -> mutableState.update { it.copy(errorMessage = "La tarea cambió; no se pudo deshacer") }
                TaskUndoResult.NotFound -> Unit
                is TaskUndoResult.Failure -> mutableState.update { it.copy(errorMessage = result.cause.message) }
            }
            undoSnapshot = null
        }
    }
    private fun reschedule(id: TaskId, preset: ReschedulePreset) { viewModelScope.launch { val result = repository.rescheduleTask(id, TaskReschedulePolicy.dueDate(preset, dates.today())); if (result is TaskMutationResult.Success) analytics.track(AnalyticsEvent.TaskRescheduled(InteractionSurface.APP_SCREEN_RADIAL_MENU)) else showError(result) } }
    private fun deleteConfirmed() { val id = uiState.value.deleteConfirmation ?: return; viewModelScope.launch { val result = repository.deleteTask(id); mutableState.update { it.copy(deleteConfirmation = null) }; if (result is TaskMutationResult.Success) analytics.track(AnalyticsEvent.TaskDeleted(InteractionSurface.APP_SCREEN)) else showError(result) } }
    private fun showError(result: TaskMutationResult) { if (result is TaskMutationResult.Failure) mutableState.update { it.copy(errorMessage = result.cause.message ?: "La operación no se pudo completar") } }
    companion object {
        const val UNDO_MS = 4_000L
        private const val DEFAULT_FOCUS_SECONDS = 25 * 60L
        private const val CAPTURE = "task_capture_draft"
    }
}
