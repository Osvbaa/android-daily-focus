package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.data.model.Task
import com.example.dailyfocus.data.model.TaskDetailUiState
import com.example.dailyfocus.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskDetailViewModel(
    private val repository: TaskRepository,
    private val taskId: String
) : ViewModel() {

    // 1. Estado Privado / Exposición Inmutable (Encapsulamiento)
    private val _uiState = MutableStateFlow(TaskDetailUiState(title = "", description = "", isCompleted = false))
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    // Canal de eventos únicos (Snackbars, Navegación)
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var originalTask: Task? = null

    fun refresh() {
        viewModelScope.launch {
            // Buscamos la tarea en el stream del repositorio
            val task = repository.getTasksStream()
                .map { list -> list.find { it.id == taskId } }
                .first { it != null }

            task?.let {
                originalTask = it
                _uiState.update { currentState ->
                    currentState.copy(
                        title = it.title,
                        description = it.description ?: "",
                        isCompleted = it.isCompleted
                    )
                }
            }
        }
    }

    init {
        refresh()
    }

    // 2. Funciones de actualización (Lógica de UI)
    fun onValueChangeTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun onValueChangeDescription(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onToggleCompleted(completed: Boolean) {
        _uiState.update { it.copy(isCompleted = completed) }
    }

    // 3. Validación reactiva (Lógica de Negocio básica)
    val isEntryValid: StateFlow<Boolean> = _uiState
        .map { it.title.isNotBlank() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // 4. Acción de persistencia
    fun onSaveTaskChanges() {
        val currentValues = _uiState.value
        val taskToSave = originalTask?.copy(
            title = currentValues.title,
            description = currentValues.description,
            isCompleted = currentValues.isCompleted
        ) ?: return

        viewModelScope.launch {
            repository.updateTask(taskToSave)
            _uiEvent.emit("¡Cambios guardados con éxito!")
        }
    }
}
