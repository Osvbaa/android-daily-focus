package com.example.dailyfocus.features.tasks.ui.state

import com.example.dailyfocus.core.model.Note

data class TaskSourceNoteUiState(
    val isLoading: Boolean = true,
    val note: Note? = null,
    val missing: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface TaskSourceNoteEvent { data object Retry : TaskSourceNoteEvent }
sealed interface TaskSourceNoteEffect { data object NavigateBack : TaskSourceNoteEffect }
