package com.example.dailyfocus.features.notes.ui.state

import com.example.dailyfocus.core.model.Note
import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.TaskSuggestion
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

enum class NoteEditorPhase { Loading, Editing, Saving, Missing, Error }

data class NoteEditorUiState(
    val noteId: String? = null,
    val title: String = "",
    val content: String = "",
    val isLoading: Boolean = false,
    val isDirty: Boolean = false,
    val phase: NoteEditorPhase = NoteEditorPhase.Editing,
    val errorMessage: String? = null,
    val aiAvailability: AiAvailability = AiAvailability.Checking,
    val suggestions: ImmutableList<TaskSuggestion> = persistentListOf(),
    val isExtracting: Boolean = false,
    val selectedSuggestionIds: ImmutableSet<String> = persistentSetOf(),
)

sealed interface NoteEditorEvent {
    data class TitleChanged(val value: String) : NoteEditorEvent
    data class ContentChanged(val value: String) : NoteEditorEvent
    data object Save : NoteEditorEvent
    data object Back : NoteEditorEvent
    data object Delete : NoteEditorEvent
    data object Retry : NoteEditorEvent
    data object ExtractTasks : NoteEditorEvent
    data class SuggestionSelected(val suggestionId: String, val selected: Boolean) : NoteEditorEvent
    data object ConfirmSuggestions : NoteEditorEvent
}

sealed interface NoteEditorEffect {
    data object NavigateBack : NoteEditorEffect
    data object RequestRetry : NoteEditorEffect
}

fun Note.toEditorState() = NoteEditorUiState(
    noteId = id,
    title = title,
    content = content,
    isLoading = false,
    isDirty = false,
    phase = NoteEditorPhase.Editing,
)
