package com.example.dailyfocus.features.today.ui.state

import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

enum class TodayPanel { NONE, TASKS, ADD_TASK, TASK_STATS, PROJECTS, PROJECT_STATS, HABITS, ADD_HABIT, HABIT_STATS }

data class TodayProjectItem(
    val project: Project,
    val pendingTaskCount: Int,
)

data class TodayUiState(
    val isLoading: Boolean = true,
    val expandedPanel: TodayPanel = TodayPanel.TASKS,
    val todayEpochDay: Long? = null,
    val tasks: ImmutableList<Task> = persistentListOf(),
    val projects: ImmutableList<TodayProjectItem> = persistentListOf(),
    val availableProjects: ImmutableList<Project> = persistentListOf(),
    val habits: ImmutableList<Habit> = persistentListOf(),
    val quickTitle: String = "",
    val quickDescription: String = "",
    val quickPriority: Priority = Priority.NONE,
    val quickProjectId: ProjectId? = null,
    val quickDurationMinutes: String = "",
    val errorMessage: String? = null,
    val isStatsExpanded: Boolean = false,
) {
    val completedTasks: Int get() = tasks.count(Task::isCompleted)
    val pendingTasks: Int get() = tasks.count { !it.isCompleted }
    val pomodoroTasks: Int get() = tasks.count { (it.estimatedDurationSeconds ?: 0) > 0 }
}

sealed interface TodayEvent {
    data class PanelSelected(val panel: TodayPanel) : TodayEvent
    data class TaskCompletionRequested(val id: TaskId) : TodayEvent
    data class TaskSelected(val id: TaskId) : TodayEvent
    data class QuickTitleChanged(val value: String) : TodayEvent
    data class QuickDescriptionChanged(val value: String) : TodayEvent
    data class QuickPriorityChanged(val value: Priority) : TodayEvent
    data class QuickProjectChanged(val value: ProjectId?) : TodayEvent
    data class QuickDurationChanged(val value: String) : TodayEvent
    data object QuickTaskSubmitted : TodayEvent
    data class HabitCompletionRequested(val habitId: String, val level: HabitCompletionLevel) : TodayEvent
    data object DashboardRequested : TodayEvent
    data object ProjectsRequested : TodayEvent
    data object ToggleStatsRequested : TodayEvent
}

sealed interface TodayEffect {
    data class NavigateToTask(val taskId: String) : TodayEffect
    data object NavigateToDashboard : TodayEffect
    data object NavigateToProjects : TodayEffect
}
