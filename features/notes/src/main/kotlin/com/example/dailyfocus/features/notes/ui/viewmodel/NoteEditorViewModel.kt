package com.example.dailyfocus.features.notes.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.ai.AiExtractionEngine
import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.AiExtractionResult
import com.example.dailyfocus.core.model.NoteExtractionInput
import com.example.dailyfocus.features.notes.ui.state.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val taskRepository: TaskRepository,
    private val aiEngine: AiExtractionEngine,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val noteId: String? = savedStateHandle[NOTE_ID]
    private val mutableState = MutableStateFlow(NoteEditorUiState(noteId = noteId, isLoading = noteId != null))
    val uiState: StateFlow<NoteEditorUiState> = mutableState.asStateFlow()
    private val effectsChannel = Channel<NoteEditorEffect>(Channel.BUFFERED)
    val effects = effectsChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            aiEngine.observeAvailability().first().let { availability ->
                mutableState.update { it.copy(aiAvailability = availability) }
            }
        }
        if (noteId != null) load(noteId) else restoreDraft()
    }

    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TitleChanged -> updateDraft { it.copy(title = event.value) }
            is NoteEditorEvent.ContentChanged -> updateDraft { it.copy(content = event.value) }
            NoteEditorEvent.Save -> save()
            NoteEditorEvent.Back -> if (uiState.value.isDirty) save(navigateBack = true) else emitBack()
            NoteEditorEvent.Delete -> delete()
            NoteEditorEvent.Retry -> noteId?.let(::load)
            NoteEditorEvent.ExtractTasks -> extractTasks()
            is NoteEditorEvent.SuggestionSelected -> mutableState.update { state -> state.copy(selectedSuggestionIds = (if (event.selected) state.selectedSuggestionIds + event.suggestionId else state.selectedSuggestionIds - event.suggestionId).toPersistentSet()) }
            NoteEditorEvent.ConfirmSuggestions -> confirmSuggestions()
        }
    }

    private fun confirmSuggestions() = viewModelScope.launch {
        val current = uiState.value
        val selected = current.suggestions.filter { it.id in current.selectedSuggestionIds }
        if (selected.isEmpty() || current.phase == NoteEditorPhase.Saving) return@launch

        mutableState.update { it.copy(phase = NoteEditorPhase.Saving, errorMessage = null) }
        val id = try {
            if (current.noteId == null || current.isDirty) {
                repository.upsertNote(current.noteId, current.title, current.content)
            } else {
                current.noteId
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            mutableState.update {
                it.copy(phase = NoteEditorPhase.Error, errorMessage = error.message ?: "No se pudo guardar la nota")
            }
            return@launch
        }
        mutableState.update { it.copy(noteId = id, isDirty = false, phase = NoteEditorPhase.Editing) }
        savedStateHandle[NOTE_ID] = id
        savedStateHandle[DRAFT_EXISTS] = false

        for (suggestion in selected) {
            when (val result = taskRepository.createTaskFromNote(suggestion.title, id)) {
                is TaskMutationResult.Success -> mutableState.update { state ->
                    state.copy(
                        suggestions = state.suggestions.filterNot { it.id == suggestion.id }.toPersistentList(),
                        selectedSuggestionIds = (state.selectedSuggestionIds - suggestion.id).toPersistentSet(),
                    )
                }
                is TaskMutationResult.Failure -> {
                    mutableState.update { state ->
                        state.copy(errorMessage = result.cause.message ?: "No se pudo crear la tarea")
                    }
                    return@launch
                }
            }
        }
        mutableState.update {
            it.copy(suggestions = kotlinx.collections.immutable.persistentListOf(), selectedSuggestionIds = kotlinx.collections.immutable.persistentSetOf())
        }
    }

    private fun extractTasks() = viewModelScope.launch {
        val content = uiState.value.content
        mutableState.update { it.copy(isExtracting = true, errorMessage = null) }
        when (val result = aiEngine.extractTasks(NoteExtractionInput(noteId ?: "draft", content))) {
            is AiExtractionResult.Success -> mutableState.update { it.copy(isExtracting = false, suggestions = result.suggestions.toPersistentList()) }
            AiExtractionResult.InsufficientText -> mutableState.update { it.copy(isExtracting = false, errorMessage = "Agrega más contenido para extraer tareas") }
            AiExtractionResult.NoActionableTasks -> mutableState.update { it.copy(isExtracting = false, errorMessage = "No se encontraron tareas accionables") }
            is AiExtractionResult.Failure -> mutableState.update { it.copy(isExtracting = false, errorMessage = "La extracción local no está disponible") }
        }
    }

    private fun load(id: String) = viewModelScope.launch {
        mutableState.update { it.copy(isLoading = true, phase = NoteEditorPhase.Loading, errorMessage = null) }
        val note = repository.getNote(id)
        if (note == null) {
            mutableState.update { it.copy(isLoading = false, phase = NoteEditorPhase.Missing) }
        } else {
            mutableState.value = note.toEditorState()
        }
    }

    private fun restoreDraft() {
        mutableState.update {
            it.copy(
                title = savedStateHandle[TITLE] ?: "",
                content = savedStateHandle[CONTENT] ?: "",
                isDirty = savedStateHandle[DRAFT_EXISTS] ?: false,
            )
        }
    }

    private fun updateDraft(update: (NoteEditorUiState) -> NoteEditorUiState) {
        mutableState.update { current ->
            val next = update(current).copy(isDirty = true, phase = NoteEditorPhase.Editing)
            savedStateHandle[TITLE] = next.title
            savedStateHandle[CONTENT] = next.content
            savedStateHandle[DRAFT_EXISTS] = true
            next
        }
    }

    private fun save(navigateBack: Boolean = false) = viewModelScope.launch {
        val current = uiState.value
        if (current.title.isBlank() && current.content.isBlank()) {
            if (navigateBack) emitBack()
            return@launch
        }
        mutableState.update { it.copy(isLoading = false, phase = NoteEditorPhase.Saving, errorMessage = null) }
        runCatching { repository.upsertNote(current.noteId, current.title, current.content) }
            .onSuccess { id ->
                mutableState.update { it.copy(noteId = id, isDirty = false, phase = NoteEditorPhase.Editing) }
                savedStateHandle[NOTE_ID] = id
                savedStateHandle[DRAFT_EXISTS] = false
                if (navigateBack) emitBack()
            }
            .onFailure { error ->
                mutableState.update { it.copy(phase = NoteEditorPhase.Error, errorMessage = error.message ?: "No se pudo guardar la nota") }
            }
    }

    private fun delete() = viewModelScope.launch {
        uiState.value.noteId?.let { repository.deleteNote(it) }
        emitBack()
    }

    private fun emitBack() { effectsChannel.trySend(NoteEditorEffect.NavigateBack) }

    companion object {
        const val NOTE_ID = "noteId"
        private const val TITLE = "note_editor_title"
        private const val CONTENT = "note_editor_content"
        private const val DRAFT_EXISTS = "note_editor_draft_exists"
    }
}
