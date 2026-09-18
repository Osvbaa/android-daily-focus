package com.example.dailyfocus.features.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.features.dashboard.ui.state.DashboardUiState
import com.example.dailyfocus.features.dashboard.ui.state.StatUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TaskRepository,
    dates: DateProvider,
    activityRepository: ActivityRepository,
) : ViewModel() {
    val uiState: StateFlow<DashboardUiState> = combine(
        repository.observeTasksForDay(dates.today().toEpochDay()),
        activityRepository.observeSummary(dates.today().toEpochDay()),
    ) { tasks, activity ->
            val total = tasks.size
            val completed = tasks.count { it.isCompleted }
            val pending = total - completed
            val progress = if (total == 0) 0f else completed.toFloat() / total
            DashboardUiState(
                stats = persistentListOf(
                    StatUiState("total", "Total tareas", total),
                    StatUiState("completed", "Completadas", completed),
                    StatUiState("pending", "Pendientes", pending),
                ),
                totalTasks = total,
                tasksCompleted = completed,
                tasksPending = pending,
                progress = progress,
                progressText = "${(progress * 100).toInt()}%",
                currentStreakDays = activity.currentStreakDays,
                longestStreakDays = activity.longestStreakDays,
                totalPoints = activity.totalPoints,
                todayPoints = activity.todayPoints,
                message = when {
                    total == 0 -> "Empieza agregando una tarea"
                    completed == total -> "Todas las tareas completadas"
                    else -> "Casi llegas a tu meta de hoy"
                },
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
}
