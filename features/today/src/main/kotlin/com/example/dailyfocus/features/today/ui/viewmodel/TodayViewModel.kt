package com.example.dailyfocus.features.today.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.data.repository.HabitRepository
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.StreakEventType
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.isDueOn
import com.example.dailyfocus.features.today.ui.state.TodayEffect
import com.example.dailyfocus.features.today.ui.state.TodayEvent
import com.example.dailyfocus.features.today.ui.state.TodayPanel
import com.example.dailyfocus.features.today.ui.state.TodayProjectItem
import com.example.dailyfocus.features.today.ui.state.TodayUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The home surface deliberately depends only on repository contracts. It can link to a task
 * editor through [TodayEffect] without taking a dependency on the tasks feature.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val habitRepository: HabitRepository,
    private val activityRepository: ActivityRepository,
    private val dates: DateProvider,
) : ViewModel() {
    private val localState = MutableStateFlow(TodayUiState())
    private val effectChannel = Channel<TodayEffect>(Channel.BUFFERED)

    val effects = effectChannel.receiveAsFlow()
    val uiState: StateFlow<TodayUiState> = combine(
        taskRepository.observeTasks(),
        projectRepository.observeProjects(),
        habitRepository.observeHabits(),
        localState,
    ) { tasks, projects, habits, local ->
        val today = dates.today().toEpochDay()
        val actionableTasks = tasks
            .filter { task -> task.dueDateEpochDays?.let { it <= today } == true }
            .sortedWith(compareBy({ it.isCompleted }, { it.dueDateEpochDays }, { it.createdAtMillis }))
        val actionableProjects = projects
            .asSequence()
            .filterNot { it.isArchived }
            .map { project ->
                TodayProjectItem(
                    project = project,
                    pendingTaskCount = actionableTasks.count { !it.isCompleted && it.projectId == project.id },
                )
            }
            .filter { it.pendingTaskCount > 0 }
            .toList()
        local.copy(
            isLoading = false,
            todayEpochDay = today,
            tasks = actionableTasks.toPersistentList(),
            projects = actionableProjects.toPersistentList(),
            availableProjects = projects.filterNot { it.isArchived }.toPersistentList(),
            habits = habits.filter { !it.isArchived && it.isDueOn(today) }.toPersistentList(),
        )
    }.catch { error ->
        localState.update {
            it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo cargar Hoy")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayUiState())

    fun onEvent(event: TodayEvent) {
        when (event) {
            is TodayEvent.PanelSelected -> localState.update {
                it.copy(expandedPanel = if (it.expandedPanel == event.panel) TodayPanel.NONE else event.panel)
            }
            is TodayEvent.TaskCompletionRequested -> completeTask(event.id)
            is TodayEvent.TaskSelected -> effectChannel.trySend(TodayEffect.NavigateToTask(event.id.value))
            is TodayEvent.HabitCompletionRequested -> completeHabit(event.habitId, event.level)
            TodayEvent.DashboardRequested -> effectChannel.trySend(TodayEffect.NavigateToDashboard)
            TodayEvent.ProjectsRequested -> effectChannel.trySend(TodayEffect.NavigateToProjects)
            TodayEvent.ToggleStatsRequested -> localState.update { it.copy(isStatsExpanded = !it.isStatsExpanded) }
            else -> handleQuickTaskEvent(event)
        }
    }

    private fun handleQuickTaskEvent(event: TodayEvent) {
        when (event) {
            is TodayEvent.QuickTitleChanged -> localState.update { it.copy(quickTitle = event.value, errorMessage = null) }
            is TodayEvent.QuickDescriptionChanged -> localState.update { it.copy(quickDescription = event.value) }
            is TodayEvent.QuickPriorityChanged -> localState.update { it.copy(quickPriority = event.value) }
            is TodayEvent.QuickProjectChanged -> localState.update { it.copy(quickProjectId = event.value) }
            is TodayEvent.QuickDurationChanged -> localState.update { it.copy(quickDurationMinutes = event.value) }
            TodayEvent.QuickTaskSubmitted -> createQuickTask()
            else -> Unit
        }
    }

    private fun createQuickTask() {
        val state = localState.value
        val durationMinutes = state.quickDurationMinutes.toLongOrNull()
        if (state.quickTitle.isBlank()) {
            localState.update { it.copy(errorMessage = "Escribe un título para la tarea") }
            return
        }
        if (durationMinutes != null && durationMinutes !in 1..480) {
            localState.update { it.copy(errorMessage = "La duración debe estar entre 1 y 480 minutos") }
            return
        }
        viewModelScope.launch {
            when (val result = taskRepository.saveTask(
                id = null,
                draft = TaskDraft(
                    title = state.quickTitle,
                    description = state.quickDescription,
                    dueDateEpochDays = dates.today().toEpochDay(),
                    priority = state.quickPriority,
                    projectId = state.quickProjectId,
                    estimatedDurationSeconds = durationMinutes?.times(60),
                ),
            )) {
                is TaskMutationResult.Success -> localState.update {
                    it.copy(
                        quickTitle = "",
                        quickDescription = "",
                        quickPriority = com.example.dailyfocus.core.model.Priority.NONE,
                        quickProjectId = null,
                        quickDurationMinutes = "",
                        expandedPanel = TodayPanel.TASKS,
                        errorMessage = null,
                    )
                }
                is TaskMutationResult.Failure -> localState.update {
                    it.copy(errorMessage = result.cause.message ?: "No se pudo crear la tarea")
                }
            }
        }
    }

    private fun completeTask(id: TaskId) {
        viewModelScope.launch {
            taskRepository.completeTask(id, includeSubtasks = false)
                .onSuccess {
                    val task = taskRepository.getTask(id) ?: return@onSuccess
                    val epochDay = dates.today().toEpochDay()
                    task.projectId?.let { projectId ->
                        projectRepository.reconcileProgress(projectId).getOrNull().orEmpty().forEach { milestoneId ->
                            activityRepository.recordCompletion(
                                type = StreakEventType.PROJECT_MILESTONE,
                                sourceId = milestoneId.value,
                                epochDay = epochDay,
                            )
                        }
                    }
                    activityRepository.recordCompletion(
                        type = if (task.projectId == null) StreakEventType.TASK else StreakEventType.PROJECT_TASK,
                        sourceId = id.value,
                        epochDay = epochDay,
                    )
                }
                .onFailure { error ->
                    localState.update {
                        it.copy(errorMessage = error.message ?: "No se pudo completar la tarea; revisa sus subtareas")
                    }
                }
        }
    }

    private fun completeHabit(habitId: String, level: com.example.dailyfocus.core.model.HabitCompletionLevel) {
        viewModelScope.launch {
            val epochDay = dates.today().toEpochDay()
            habitRepository.completeLevel(HabitId(habitId), epochDay, level)
                .onSuccess {
                    activityRepository.recordCompletion(
                        type = StreakEventType.HABIT_LEVEL,
                        sourceId = habitId,
                        epochDay = epochDay,
                        habitLevel = level,
                    )
                }
                .onFailure { error ->
                    localState.update { it.copy(errorMessage = error.message ?: "No se pudo completar el hábito") }
                }
        }
    }
}
