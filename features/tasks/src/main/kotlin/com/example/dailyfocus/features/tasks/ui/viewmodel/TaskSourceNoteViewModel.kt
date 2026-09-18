package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.features.tasks.ui.state.TaskSourceNoteEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskSourceNoteUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskSourceNoteViewModel @Inject constructor(
    private val notes: NoteRepository,
) : ViewModel() {
    private var noteId: String? = null
    private var loadJob: Job? = null
    private val _state = MutableStateFlow(TaskSourceNoteUiState())
    val uiState: StateFlow<TaskSourceNoteUiState> = _state.asStateFlow()
    private val _effects = Channel<com.example.dailyfocus.features.tasks.ui.state.TaskSourceNoteEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load(id: String) {
        if (noteId == id && loadJob?.isActive == true) return
        noteId = id
        loadJob?.cancel()
        _state.value = TaskSourceNoteUiState()
        loadJob = viewModelScope.launch {
            runCatching { notes.getNote(id) }
                .onSuccess { note -> _state.value = TaskSourceNoteUiState(isLoading = false, note = note, missing = note == null) }
                .onFailure { error -> _state.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo leer la nota") } }
        }
    }

    fun onEvent(event: TaskSourceNoteEvent) {
        when (event) {
            TaskSourceNoteEvent.Retry -> noteId?.let(::load)
        }
    }
}
