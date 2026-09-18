package com.example.dailyfocus.features.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.features.notes.ui.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NoteListViewModel @Inject constructor(
    private val repository: NoteRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(NoteListUiState())
    val uiState: StateFlow<NoteListUiState> = mutableState.asStateFlow()
    private val effectsChannel = Channel<NoteListEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    init { observeNotes() }

    fun onEvent(event: NoteListEvent) {
        when (event) {
            is NoteListEvent.QueryChanged -> mutableState.update { it.copy(query = event.value) }
            NoteListEvent.CreateRequested -> effectsChannel.trySend(NoteListEffect.NavigateToEditor())
            is NoteListEvent.NoteSelected -> effectsChannel.trySend(NoteListEffect.NavigateToEditor(event.noteId))
            is NoteListEvent.DeleteRequested -> viewModelScope.launch { repository.deleteNote(event.noteId) }
            NoteListEvent.Retry -> observeNotes()
        }
    }

    private fun observeNotes() {
        viewModelScope.launch {
            repository.observeAllNotes()
                .catch { error ->
                    mutableState.update { it.copy(isLoading = false, errorMessage = error.message ?: "No se pudieron cargar las notas") }
                }
                .collect { notes ->
                    mutableState.update { it.copy(isLoading = false, notes = notes.toPersistentList(), errorMessage = null) }
                }
        }
    }
}
