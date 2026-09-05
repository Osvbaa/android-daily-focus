package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.features.tasks.domain.model.Task
import com.example.dailyfocus.features.tasks.ui.state.TaskUiState
import com.example.dailyfocus.features.tasks.domain.repository.TaskRepository
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class TaskViewModel(
    private val repository: TaskRepository // <--- Dependencia inyectada
) : ViewModel() {
    // usamos el Stream del repositorio
    val tasks: StateFlow<PersistentList<Task>> = repository.getTasksStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentListOf()
        )

    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Actualizamos el estado de la tarea completado/no completado
    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = task.isCompleted.not())
            repository.updateTask(updatedTask)
        }
    }
    private var lastDeletedTask: Task? = null
    private var lastDeletedIndex: Int = -1
    // Borramos la tarea
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

    // Deshacer la eliminación
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

    // Agrupamos las tareas por fecha
    val groupedTasks: StateFlow<PersistentMap<String, PersistentList<TaskUiState>>> = tasks
        .map { tasks ->
            tasks.map { task ->
                TaskUiState(
                    task = task,
                    formattedTime = task.createdAt.format(DateTimeFormatter.ofPattern("HH:mm"))
                )
            }
                .groupBy { item ->
                    // Aquí agrupamos y formateamos la cabecera
                    item.task.createdAt.format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
                        .replaceFirstChar { it.uppercase() } // ¡Incluso la mayúscula viene de aquí!
                }
                .mapValues { it.value.toPersistentList() }
                .toPersistentMap()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = persistentMapOf()
        )
}