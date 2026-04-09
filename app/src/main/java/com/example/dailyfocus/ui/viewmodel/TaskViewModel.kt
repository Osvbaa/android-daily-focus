package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyfocus.data.model.DashboardState
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.data.model.Task
import com.example.dailyfocus.data.repository.TaskRepository
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class TaskViewModel(
    private val repository: TaskRepository // <--- RECIBE la herramienta
) : ViewModel() {
    // usamos el Stream del repositorio
    val tasks: StateFlow<PersistentList<Task>> = repository.getTasksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = persistentListOf()
        )

    // El canal de alertas (SharedFlow para las alertas en el snackbar)
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

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
    // Actualizamos el estado de la tarea
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = task.isCompleted.not())
            repository.updateTask(updatedTask)
        }
    }
    private var lastDeletedTask: Task? = null
    private var lastDeletedIndex: Int = -1
    fun deleteTask(taskId: String) {
        val currentTasks = tasks.value
        val index = currentTasks.indexOfFirst { it.id == taskId }

        if (index != -1) {
            // Guardamos la tarea y su posición antes de borrar
            lastDeletedTask = currentTasks[index]
            lastDeletedIndex = index

            viewModelScope.launch {
                repository.deleteTask(taskId)
                _uiEvent.emit(value = "Tarea eliminada")
            }
        }
    }
    fun undoDelete() {
        val taskToRestore = lastDeletedTask ?: return
        val indexToRestore = lastDeletedIndex

        viewModelScope.launch {
            repository.restoreTask(indexToRestore, taskToRestore)
            // Limpiamos las referencias después de restaurar
            lastDeletedTask = null
            lastDeletedIndex = -1
        }
    }
}