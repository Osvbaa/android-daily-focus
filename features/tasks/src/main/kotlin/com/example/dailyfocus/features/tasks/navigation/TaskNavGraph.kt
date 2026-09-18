package com.example.dailyfocus.features.tasks.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.tasks.ui.screen.TaskEditorScreen
import com.example.dailyfocus.features.tasks.ui.screen.TaskListScreen
import com.example.dailyfocus.features.tasks.ui.screen.TaskSourceNoteScreen
import com.example.dailyfocus.features.tasks.ui.screen.TaskReminderScreen
import com.example.dailyfocus.features.tasks.ui.viewmodel.TaskEditorViewModel
import com.example.dailyfocus.features.tasks.ui.viewmodel.TaskListViewModel
import com.example.dailyfocus.features.tasks.ui.viewmodel.TaskSourceNoteViewModel
import com.example.dailyfocus.features.tasks.ui.viewmodel.TaskReminderViewModel
import com.example.dailyfocus.features.tasks.ui.state.TaskSourceNoteEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskReminderEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEffect
import com.example.dailyfocus.features.tasks.ui.state.TaskListEffect
import com.example.dailyfocus.features.tasks.ui.state.TaskListEvent

/**
 * Resolves the feature's small route set. The branching is intentionally kept at this
 * boundary so feature navigation remains self-contained and routes carry primitives only.
 */
@Suppress("CyclomaticComplexMethod")
fun taskNavGraph(
    key: NavKey,
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState,
    onStartFocus: (taskId: String, durationSeconds: Long) -> Unit,
): NavEntry<NavKey>? = when (key) {
    is TaskRoute ->
        NavEntry(key) {
            val viewModel: TaskListViewModel = hiltViewModel(key = "task-list")
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            // Escuchamos los eventos (SharedFlow)
            LaunchedEffect(key1 = Unit) {
                viewModel.effects.collect { effect -> when (effect) {
                    is TaskListEffect.NavigateToEditor -> backStack.add(TaskEditorRoute(effect.taskId))
                    TaskListEffect.NavigateToReminder -> backStack.add(TaskReminderRoute)
                    is TaskListEffect.NavigateToSourceNote -> backStack.add(TaskSourceNoteRoute(effect.noteId))
                    is TaskListEffect.NavigateToFocus -> onStartFocus(effect.taskId, effect.durationSeconds)
                    is TaskListEffect.ShowUndo -> if (snackbarHostState.showSnackbar(effect.message, "Deshacer") == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                        viewModel.onEvent(TaskListEvent.UndoRequested(effect.operationId))
                    }
                    TaskListEffect.PerformHaptic, TaskListEffect.RequestCaptureFocus -> Unit
                }
                }
            }

            TaskListScreen(
                state = state,
                onEvent = viewModel::onEvent,
            )
        }

    is TaskEditorRoute ->
        NavEntry(key) {
            val viewModel: TaskEditorViewModel = hiltViewModel(key = "task-editor-${key.taskId ?: "new"}")
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) { viewModel.effects.collect { if (it == TaskEditorEffect.NavigateBack && backStack.size > 1) backStack.removeAt(backStack.size - 1) } }

            TaskEditorScreen(
                state = uiState,
                onEvent = viewModel::onEvent,
            )
        }
    is TaskSourceNoteRoute ->
        NavEntry(key) {
            val viewModel: TaskSourceNoteViewModel = hiltViewModel(key = "task-source-note-${key.noteId}")
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(key.noteId) { viewModel.load(key.noteId) }
            TaskSourceNoteScreen(
                state = state,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
                onRetry = { viewModel.onEvent(TaskSourceNoteEvent.Retry) },
            )
        }
    is TaskReminderRoute ->
        NavEntry(key) {
            val viewModel: TaskReminderViewModel = hiltViewModel(key = "task-reminder")
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            TaskReminderScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBack = { if (backStack.size > 1) backStack.removeAt(backStack.size - 1) },
            )
        }
    else -> null
}
