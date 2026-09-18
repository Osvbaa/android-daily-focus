package com.example.dailyfocus.features.notes.ui.state

import com.example.dailyfocus.core.model.Note
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class NoteListUiState(
    val isLoading: Boolean = true,
    val notes: ImmutableList<Note> = persistentListOf(),
    val query: String = "",
    val errorMessage: String? = null,
)

sealed interface NoteListEvent {
    data class QueryChanged(val value: String) : NoteListEvent
    data object CreateRequested : NoteListEvent
    data class NoteSelected(val noteId: String) : NoteListEvent
    data class DeleteRequested(val noteId: String) : NoteListEvent
    data object Retry : NoteListEvent
}

sealed interface NoteListEffect {
    data class NavigateToEditor(val noteId: String? = null) : NoteListEffect
    data object RequestRetry : NoteListEffect
}
