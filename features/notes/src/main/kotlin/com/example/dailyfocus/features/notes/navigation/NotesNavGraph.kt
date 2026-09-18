package com.example.dailyfocus.features.notes.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.notes.ui.screen.NoteEditorScreen
import com.example.dailyfocus.features.notes.ui.screen.NoteListScreen
import com.example.dailyfocus.features.notes.ui.state.NoteEditorEffect
import com.example.dailyfocus.features.notes.ui.state.NoteListEffect
import com.example.dailyfocus.features.notes.ui.viewmodel.NoteEditorViewModel
import com.example.dailyfocus.features.notes.ui.viewmodel.NoteListViewModel

fun notesNavGraph(key: NavKey, backStack: NavBackStack<NavKey>): NavEntry<NavKey>? = when (key) {
    NoteListRoute -> NavEntry(key) {
        val viewModel: NoteListViewModel = hiltViewModel(key = "note-list")
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.effects.collect { effect: NoteListEffect -> if (effect is NoteListEffect.NavigateToEditor) backStack.add(NoteEditorRoute(effect.noteId)) } }
        NoteListScreen(state, viewModel::onEvent)
    }
    is NoteEditorRoute -> NavEntry(key) {
        val viewModel: NoteEditorViewModel = hiltViewModel(key = "note-editor-${key.noteId ?: "new"}")
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) { viewModel.effects.collect { effect: NoteEditorEffect -> if (effect == NoteEditorEffect.NavigateBack && backStack.size > 1) backStack.removeAt(backStack.size - 1) } }
        NoteEditorScreen(state, viewModel::onEvent)
    }
    else -> null
}
