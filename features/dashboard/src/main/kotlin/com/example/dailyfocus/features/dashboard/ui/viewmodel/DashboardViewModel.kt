package com.example.dailyfocus.features.dashboard.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.features.dashboard.ui.state.DashboardUiState
import com.example.dailyfocus.features.dashboard.ui.state.StatUiState
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.common.time.DateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    repository: TaskRepository,
    dates: DateProvider,
) : ViewModel() {
    /** Mi Día is a civil-date projection; it never imports the Tasks feature. */
    val tasks = repository.observeTasksForDay(dates.today().toEpochDay())
        .map { it.toPersistentList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = persistentListOf()
        )

    // Calculamos el progreso diario
    val stats: StateFlow<DashboardUiState> = tasks.map { currentTasks ->
        val total = currentTasks.size
        if (total == 0) return@map DashboardUiState(message = "¡Empieza agregando una tarea!")
        val completed = currentTasks.count { it.isCompleted }
        val progress = completed.toFloat() / total
        val pending = total - completed

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
