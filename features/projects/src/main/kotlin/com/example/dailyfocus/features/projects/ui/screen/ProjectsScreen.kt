package com.example.dailyfocus.features.projects.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.features.projects.ui.state.ProjectListEvent
import com.example.dailyfocus.features.projects.ui.state.ProjectListUiState

@Composable
fun ProjectsScreen(
    state: ProjectListUiState,
    onEvent: (ProjectListEvent) -> Unit,
    modifier: Modifier = Modifier,
    onOpenTask: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Proyectos", style = MaterialTheme.typography.headlineMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(state.name, { onEvent(ProjectListEvent.NameChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(state.description, { onEvent(ProjectListEvent.DescriptionChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Descripción") })
                OutlinedTextField(state.milestonesText, { onEvent(ProjectListEvent.MilestonesChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Hitos, uno por línea") })
                Button(onClick = { onEvent(ProjectListEvent.Create) }, enabled = state.name.isNotBlank()) { Text("Crear proyecto") }
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        state.selectedProject?.let { details ->
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(details.project.name, style = MaterialTheme.typography.titleLarge)
                        details.milestones.forEach { milestone ->
                            Text("${milestone.position + 1}. ${milestone.title} (${milestone.status})")
                        }
                        if (details.tasks.isEmpty()) Text("Aún no hay tareas en este proyecto")
                        details.tasks.forEach { task ->
                            OutlinedButton(onClick = { onOpenTask(task.id.value) }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (task.isCompleted) "✓ ${task.title}" else "○ ${task.title}")
                            }
                        }
                        OutlinedButton(onClick = { onEvent(ProjectListEvent.ClearSelection) }) { Text("Cerrar detalle") }
                    }
                }
            }
        }
        items(state.projects, key = { it.id.value }) { project ->
            Card(Modifier.fillMaxWidth().clickable { onEvent(ProjectListEvent.Select(project.id.value)) }) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(project.name, style = MaterialTheme.typography.titleMedium)
                        if (project.description.isNotBlank()) Text(project.description, style = MaterialTheme.typography.bodyMedium)
                    }
                    OutlinedButton(onClick = { onEvent(ProjectListEvent.Archive(project.id.value)) }) { Text("Archivar") }
                }
            }
        }
    }
}
