package com.example.dailyfocus.features.tasks.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.core.model.ReschedulePreset
import com.example.dailyfocus.features.tasks.ui.components.TaskCard
import com.example.dailyfocus.features.tasks.ui.state.TaskListEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskListUiState

@Composable
fun TaskListScreen(
    state: TaskListUiState,
    onEvent: (TaskListEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTasks = state.tasks.filter { task -> task.isVisible(state.showCompleted, state.query) }
    Scaffold(modifier = modifier, contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Mis tareas", style = MaterialTheme.typography.headlineMedium)
                        TextButton(onClick = { onEvent(TaskListEvent.ReminderRequested) }) {
                            Icon(Icons.Default.Notifications, contentDescription = null)
                            Text("Recordatorio", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    TextButton(onClick = { onEvent(TaskListEvent.CreateRequested) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Nueva tarea", modifier = Modifier.padding(start = 8.dp))
                    }
                    OutlinedTextField(
                        value = state.captureTitle,
                        onValueChange = { onEvent(TaskListEvent.CaptureChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Añadir una tarea para hoy") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onEvent(TaskListEvent.CaptureSubmitted) }),
                    )
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = { onEvent(TaskListEvent.QueryChanged(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        placeholder = { Text("Buscar en tus tareas") },
                        singleLine = true,
                    )
                    TextButton(onClick = { onEvent(TaskListEvent.ToggleCompletedVisibility) }) {
                        Text(if (state.showCompleted) "Ocultar completadas" else "Mostrar completadas")
                    }
                }
            }
            if (state.isLoading) item(span = { GridItemSpan(maxLineSpan) }) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            state.errorMessage?.let { message ->
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onEvent(TaskListEvent.Retry) }) { Text("Reintentar") }
                    }
                }
            }
            if (!state.isLoading && visibleTasks.isEmpty() && state.errorMessage == null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("Día libre de pendientes", style = MaterialTheme.typography.titleMedium)
                        Text("Captura una tarea para empezar.", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { onEvent(TaskListEvent.CreateRequested) }) { Text("Crear tarea") }
                    }
                }
            }
            items(visibleTasks, key = { it.id.value }) { task ->
                var menu by remember { mutableStateOf(false) }
                Box {
                    TaskCard(
                        task = task,
                        projectName = task.projectId?.value?.let(state.projectNames::get),
                        requiresSubtaskReview = task.id in state.focusCompletedTaskIds && !task.isCompleted && task.subtasks.any { !it.isCompleted },
                        onCheckedChange = { onEvent(TaskListEvent.CompleteRequested(task.id)) },
                        onTaskClick = { onEvent(TaskListEvent.TaskSelected(task.id)) },
                        onMoreClick = { menu = true },
                        onFocusClick = { onEvent(TaskListEvent.FocusRequested(task.id)) },
                        onSourceNoteClick = task.linkedNoteId?.let { noteId -> { onEvent(TaskListEvent.SourceNoteRequested(noteId)) } },
                    )
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        ReschedulePreset.entries.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.label()) },
                                onClick = {
                                    menu = false
                                    onEvent(TaskListEvent.Reschedule(task.id, preset))
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                            onClick = {
                                menu = false
                                onEvent(TaskListEvent.DeleteRequested(task.id))
                            },
                        )
                    }
                }
            }
        }
    }
    state.pendingSubtaskResolution?.let { id ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Hay subtareas pendientes") },
            text = { Text("Revisa las subtareas o completa todo el grupo.") },
            confirmButton = { TextButton(onClick = { onEvent(TaskListEvent.CompleteAll(id)) }) { Text("Completar todas") } },
            dismissButton = { TextButton(onClick = { onEvent(TaskListEvent.TaskSelected(id)) }) { Text("Ver subtareas") } },
        )
    }
    state.deleteConfirmation?.let {
        AlertDialog(
            onDismissRequest = { onEvent(TaskListEvent.DeleteDismissed) },
            title = { Text("Eliminar tarea") },
            text = { Text("Esta acción es permanente y también elimina sus subtareas.") },
            confirmButton = { TextButton(onClick = { onEvent(TaskListEvent.DeleteConfirmed) }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { onEvent(TaskListEvent.DeleteDismissed) }) { Text("Cancelar") } },
        )
    }
}

private fun com.example.dailyfocus.core.model.Task.isVisible(showCompleted: Boolean, query: String): Boolean =
    (showCompleted || !isCompleted) && (query.isBlank() || title.contains(query, ignoreCase = true))

private fun ReschedulePreset.label(): String = when (this) {
    ReschedulePreset.TODAY -> "Hoy"
    ReschedulePreset.TOMORROW -> "Mañana"
    ReschedulePreset.THIS_WEEKEND -> "Este fin de semana"
    ReschedulePreset.NEXT_WEEK -> "La próxima semana"
}
