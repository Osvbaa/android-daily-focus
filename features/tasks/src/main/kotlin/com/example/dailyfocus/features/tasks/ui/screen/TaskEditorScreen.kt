package com.example.dailyfocus.features.tasks.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.ui.component.priorityColor
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.features.tasks.ui.state.DraftPersistenceState
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("CyclomaticComplexMethod")
fun TaskEditorScreen(
    state: TaskEditorUiState,
    onEvent: (TaskEditorEvent) -> Unit,
    showBack: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val controlsEnabled = !state.isLoading && !state.isSaving && !state.conflict && state.phase != com.example.dailyfocus.features.tasks.ui.state.TaskEditorPhase.Missing
    BackHandler { onEvent(TaskEditorEvent.Back) }
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(if (state.taskId == null) "Nueva tarea" else "Editar tarea") },
                navigationIcon = if (showBack) {
                    { IconButton(onClick = { onEvent(TaskEditorEvent.Back) }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar") } }
                } else {
                    {}
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().consumeWindowInsets(padding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.conflict) item {
                    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("La tarea cambió fuera de este editor", color = MaterialTheme.colorScheme.onErrorContainer)
                            Text("Recarga la versión publicada para resolver el conflicto.", color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = { onEvent(TaskEditorEvent.ReloadRemote) }) { Text("Recargar versión publicada") }
                        }
                    }
                }
                if (state.isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
                if (!state.isLoading) item {
                    val label = when (state.draftPersistence) {
                        DraftPersistenceState.Persisting -> "Guardando borrador…"
                        DraftPersistenceState.Persisted -> "Borrador guardado"
                        DraftPersistenceState.Error -> "No se pudo guardar el borrador"
                        DraftPersistenceState.NotPersisted -> ""
                    }
                    if (label.isNotEmpty()) Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.draftPersistence == DraftPersistenceState.Error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.phase == com.example.dailyfocus.features.tasks.ui.state.TaskEditorPhase.Missing) item {
                    Text(state.errorMessage ?: "Tarea no encontrada", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { onEvent(TaskEditorEvent.Retry) }) { Text("Reintentar") }
                }
                item {
                    ProjectSection(
                        state = state,
                        onProjectChanged = { onEvent(TaskEditorEvent.ProjectChanged(it)) },
                        onMilestoneChanged = { onEvent(TaskEditorEvent.MilestoneChanged(it)) },
                        enabled = controlsEnabled,
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.draft.title,
                        onValueChange = { onEvent(TaskEditorEvent.TitleChanged(it)) },
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Título de la tarea" },
                        label = { Text("Título") },
                        isError = state.titleError,
                        supportingText = { if (state.titleError) Text("Escribe un título") },
                        singleLine = true,
                        enabled = controlsEnabled,
                    )
                }
                item {
                    PlanningSection(
                        dueDateEpochDays = state.draft.dueDateEpochDays,
                        priority = state.draft.priority,
                        estimatedDurationSeconds = state.draft.estimatedDurationSeconds,
                        onChooseDate = { showDatePicker = true },
                        onClearDate = { onEvent(TaskEditorEvent.DueDateChanged(null)) },
                        onPriorityChanged = { onEvent(TaskEditorEvent.PriorityChanged(it)) },
                        onEstimatedDurationMinutesChanged = { onEvent(TaskEditorEvent.EstimatedDurationMinutesChanged(it)) },
                        enabled = controlsEnabled,
                    )
                }
                item {
                    OutlinedTextField(
                        value = state.draft.description,
                        onValueChange = { onEvent(TaskEditorEvent.DescriptionChanged(it)) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        label = { Text("Descripción") },
                        enabled = controlsEnabled,
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Subtareas", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { onEvent(TaskEditorEvent.AddSubtask) }, enabled = controlsEnabled) {
                            Icon(Icons.Default.Add, "Añadir subtarea")
                        }
                    }
                }
                itemsIndexed(state.draft.subtasks, key = { _, item -> item.id.value }) { index, subtask ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = subtask.isCompleted,
                            onCheckedChange = { onEvent(TaskEditorEvent.ToggleSubtask(index)) },
                            enabled = controlsEnabled,
                        )
                        OutlinedTextField(
                            value = subtask.title,
                            onValueChange = { onEvent(TaskEditorEvent.SubtaskChanged(index, it)) },
                            modifier = Modifier.weight(1f),
                            label = { Text("Subtarea ${index + 1}") },
                            singleLine = true,
                            enabled = controlsEnabled,
                        )
                        Column {
                            IconButton(onClick = { onEvent(TaskEditorEvent.MoveSubtask(index, index - 1)) }, enabled = index > 0 && controlsEnabled) {
                                Icon(Icons.Default.KeyboardArrowUp, "Mover arriba")
                            }
                            IconButton(onClick = { onEvent(TaskEditorEvent.MoveSubtask(index, index + 1)) }, enabled = index < state.draft.subtasks.lastIndex && controlsEnabled) {
                                Icon(Icons.Default.KeyboardArrowDown, "Mover abajo")
                            }
                        }
                        IconButton(onClick = { onEvent(TaskEditorEvent.DeleteSubtask(index)) }, enabled = controlsEnabled) {
                            Icon(Icons.Default.Delete, "Eliminar subtarea")
                        }
                    }
                }
                state.errorMessage?.let { message -> item { Text(message, color = MaterialTheme.colorScheme.error) } }
                item {
                    Button(
                        onClick = { onEvent(TaskEditorEvent.Save) },
                        enabled = controlsEnabled,
                        modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Guardar tarea" },
                    ) { Text(if (state.isSaving) "Guardando…" else "Guardar") }
                }
            }
        }
    }
    if (showDatePicker) {
        val datePickerState = androidx.compose.material3.rememberDatePickerState(
            initialSelectedDateMillis = state.draft.dueDateEpochDays?.toUtcMidnightMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.toEpochDaysAtUtc()?.let { onEvent(TaskEditorEvent.DueDateChanged(it)) }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } },
        ) { DatePicker(state = datePickerState) }
    }
    if (state.showDiscardConfirmation) {
        AlertDialog(
            onDismissRequest = { onEvent(TaskEditorEvent.ContinueEditing) },
            title = { Text("Descartar cambios") },
            text = { Text("El borrador tiene cambios sin guardar.") },
            confirmButton = { TextButton(onClick = { onEvent(TaskEditorEvent.Discard) }) { Text("Descartar") } },
            dismissButton = { TextButton(onClick = { onEvent(TaskEditorEvent.ContinueEditing) }) { Text("Continuar editando") } },
        )
    }
}

@Composable
private fun ProjectSection(
    state: TaskEditorUiState,
    onProjectChanged: (ProjectId?) -> Unit,
    onMilestoneChanged: (MilestoneId?) -> Unit,
    enabled: Boolean,
) {
    var projectMenu by remember { mutableStateOf(false) }
    var milestoneMenu by remember { mutableStateOf(false) }
    val selectedProject = state.availableProjects.firstOrNull { it.id == state.draft.projectId }
    val selectedMilestone = state.availableMilestones.firstOrNull { it.id == state.draft.milestoneId }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Proyecto", style = MaterialTheme.typography.titleMedium)
        Box {
            OutlinedButton(onClick = { projectMenu = true }, enabled = enabled) {
                Text(selectedProject?.name ?: "Sin proyecto")
            }
            DropdownMenu(expanded = projectMenu, onDismissRequest = { projectMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Sin proyecto") },
                    onClick = { projectMenu = false; onProjectChanged(null) },
                )
                state.availableProjects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = { projectMenu = false; onProjectChanged(project.id) },
                    )
                }
            }
        }
        if (state.draft.projectId != null && state.availableMilestones.isNotEmpty()) {
            Box {
                OutlinedButton(onClick = { milestoneMenu = true }, enabled = enabled) {
                    Text(selectedMilestone?.title ?: "Sin hito")
                }
                DropdownMenu(expanded = milestoneMenu, onDismissRequest = { milestoneMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Sin hito") },
                        onClick = { milestoneMenu = false; onMilestoneChanged(null) },
                    )
                    state.availableMilestones.forEach { milestone ->
                        DropdownMenuItem(
                            text = { Text(milestone.title) },
                            onClick = { milestoneMenu = false; onMilestoneChanged(milestone.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanningSection(
    dueDateEpochDays: Long?,
    priority: Priority,
    estimatedDurationSeconds: Long?,
    onChooseDate: () -> Unit,
    onClearDate: () -> Unit,
    onPriorityChanged: (Priority) -> Unit,
    onEstimatedDurationMinutesChanged: (Long?) -> Unit,
    enabled: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Planificación", style = MaterialTheme.typography.titleMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onChooseDate, enabled = enabled) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                Text(dueDateEpochDays?.let(::formatDate) ?: "Elegir fecha", modifier = Modifier.padding(start = 8.dp))
            }
            if (dueDateEpochDays != null) TextButton(onClick = onClearDate, enabled = enabled) { Text("Quitar") }
        }
        Text("Prioridad", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.entries.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { onPriorityChanged(option) },
                    label = { Text(option.label()) },
                    enabled = enabled,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priorityColor(option).copy(alpha = 0.18f),
                        selectedLabelColor = priorityColor(option),
                    ),
                )
            }
        }
        OutlinedTextField(
            value = estimatedDurationSeconds?.div(60L)?.toString().orEmpty(),
            onValueChange = { value ->
                onEstimatedDurationMinutesChanged(value.toLongOrNull())
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Estimación de foco (minutos, opcional)") },
            supportingText = { Text("Focus propondrá 25 minutos si lo dejas vacío") },
            singleLine = true,
            enabled = enabled,
        )
    }
}

private fun Long.toUtcMidnightMillis(): Long = LocalDate.ofEpochDay(this).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
private fun Long.toEpochDaysAtUtc(): Long = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()
private fun formatDate(epochDays: Long): String = LocalDate.ofEpochDay(epochDays).toString()
private fun Priority.label(): String = when (this) {
    Priority.NONE -> "Sin prioridad"
    Priority.LOW -> "Baja"
    Priority.NORMAL -> "Normal"
    Priority.HIGH -> "Alta"
    Priority.URGENT -> "Urgente"
}
