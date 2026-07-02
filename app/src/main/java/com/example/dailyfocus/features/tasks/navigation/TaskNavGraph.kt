package com.example.dailyfocus.features.tasks.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.tasks.ui.detail.navigation.TaskDetailRoute
import com.example.dailyfocus.features.tasks.ui.detail.TaskDetailScreen
import com.example.dailyfocus.features.tasks.ui.detail.TaskDetailViewModel
import com.example.dailyfocus.features.tasks.ui.screen.TaskScreen
import com.example.dailyfocus.features.tasks.ui.viewmodel.TaskViewModel
import com.example.dailyfocus.app.ui.AppViewModelProvider

fun taskNavGraph(
    key: NavKey,
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState
): NavEntry<NavKey>? = when (key) {
    is TaskRoute ->
        NavEntry(key) {
            val viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val groupedTasks by viewModel.groupedTasks.collectAsStateWithLifecycle()

            // 2. ESCUCHAMOS LOS EVENTOS (SharedFlow)
            // Cuando el Chef diga "Tarea eliminada", el Mesero muestra el Snackbar
            LaunchedEffect(key1 = Unit) {
                viewModel.uiEvent.collect { message ->
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = "Deshacer"
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoDelete()
                    }
                }
            }

            TaskScreen(
                onDeleteTask = { taskToDelete ->
                    viewModel.deleteTask(taskId = taskToDelete.id)
                },
                onTaskClick = { clickedTaskId ->
                    backStack.add(TaskDetailRoute(taskId = clickedTaskId))
                },
                onCheckedChange = { task ->
                    viewModel.toggleTaskCompletion(task = task)
                },
                groupedTasks = groupedTasks
            )
        }

    is TaskDetailRoute ->
        NavEntry(key) {
            val viewModel: TaskDetailViewModel = viewModel(
                key = key.taskId,
                factory = AppViewModelProvider.factoryForTaskDetail(key.taskId)
            )

            LaunchedEffect(key1 = key.taskId) {
                viewModel.refresh()
            }
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val canSave by viewModel.isEntryValid.collectAsStateWithLifecycle()

            TaskDetailScreen(
                title = uiState.title,
                description = uiState.description,
                isCompleted = uiState.isCompleted,
                isSaveEnabled = canSave,
                onValueChangeTitle = { newTitle ->
                    viewModel.onValueChangeTitle(newTitle = newTitle)
                },
                onValueChangeDescription = { newDescription ->
                    viewModel.onValueChangeDescription(newDescription = newDescription)
                },
                onToggleCompleted = { isCompleted ->
                    viewModel.onToggleCompleted(completed = isCompleted)
                },
                onSaveTaskChanges = {
                    if (canSave) {
                        viewModel.onSaveTaskChanges()
                        backStack.removeAt(backStack.size - 1)
                    }
                },
                onNavigateBack = {
                    if (backStack.size > 1) backStack.removeAt(backStack.size - 1)
                }
            )
        }
    else -> error("Unknown key $key")
}