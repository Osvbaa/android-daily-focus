package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.data.model.DashboardState
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.data.repository.TaskRepository
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
    val stats: StateFlow<DashboardState> = tasks.map { currentTasks ->
        val total = currentTasks.size
        if (total == 0) return@map DashboardState(message = "¡Empieza agregando una tarea!")
        val completed = currentTasks.count { it.isCompleted }
        val progress = completed.toFloat() / total
        val pending = total - completed

        val tasksCompleted = currentTasks.count { task ->  task.isCompleted }
        val tasksPending = currentTasks.count { task ->  !task.isCompleted }

        DashboardState(
            stats = persistentListOf(
                StatItem(title = "Total tareas", value = total),
                StatItem(title = "Completadas", value = completed),
                StatItem(title = "Pendientes", value = pending)
            ),
            totalTasks = total,
            tasksCompleted = tasksCompleted,
            tasksPending = tasksPending,
            progress = progress,
            progressText = "${(progress * 100).toInt()}%",
            message = if (completed == total) "¡Todas las tareas completadas! 🎉" else "Casi llegas a tu meta de hoy"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DashboardState(message = "",)
    )
}