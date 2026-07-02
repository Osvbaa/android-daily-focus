package com.example.dailyfocus.features.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.features.dashboard.ui.state.StatUiState
import com.example.dailyfocus.features.tasks.domain.repository.TaskRepository
import com.example.dailyfocus.features.dashboard.ui.state.DashboardUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    repository: TaskRepository
) : ViewModel() {
    val tasks = repository.getTasksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = persistentListOf()
        )

    // Calculamos el progreso diario
    val stats: StateFlow<DashboardUiState> = tasks.map { currentTasks ->
        val total = currentTasks.size // O(1)
        if (total == 0) return@map DashboardUiState(message = "¡Empieza agregando una tarea!")
        val completed = currentTasks.count { it.isCompleted } // O(n)
        val progress = completed.toFloat() / total // O(1)
        val pending = total - completed  // O(1)

        DashboardUiState(
            stats = persistentListOf(
                StatUiState(title = "Total tareas", value = total),
                StatUiState(title = "Completadas", value = completed),
                StatUiState(title = "Pendientes", value = pending)
            ),
            totalTasks = total,
            tasksCompleted = completed,
            tasksPending = pending,
            progress = progress,
            progressText = "${(progress * 100).toInt()}%",
            message = if (completed == total) "¡Todas las tareas completadas! 🎉" else "Casi llegas a tu meta de hoy"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DashboardUiState(message = "")
    )
}