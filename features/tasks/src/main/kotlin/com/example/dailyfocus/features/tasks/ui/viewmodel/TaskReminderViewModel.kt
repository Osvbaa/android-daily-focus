package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.model.TaskReminderSettings
import com.example.dailyfocus.features.tasks.ui.state.TaskReminderEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskReminderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskReminderViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TaskReminderUiState())
    val uiState: StateFlow<TaskReminderUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeReminderSettings().collect { settings -> _state.update { it.copy(settings = settings) } }
        }
    }

    fun onEvent(event: TaskReminderEvent) {
        val current = uiState.value.settings
        val next = when (event) {
            is TaskReminderEvent.SetEnabled -> current.copy(enabled = event.enabled)
            is TaskReminderEvent.SetTime -> runCatching { current.copy(hour = event.hour, minute = event.minute) }
                .getOrElse { current }
            TaskReminderEvent.Retry -> return
        }
        persist(next)
    }

    private fun persist(settings: TaskReminderSettings) {
        _state.update { it.copy(settings = settings, isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = repository.saveReminderSettings(settings)) {
                is TaskMutationResult.Success -> _state.update { it.copy(isSaving = false) }
                is TaskMutationResult.Failure -> _state.update {
                    it.copy(isSaving = false, errorMessage = result.cause.message ?: "No se pudo guardar el recordatorio")
                }
            }
        }
    }
}
