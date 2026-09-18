package com.example.dailyfocus.features.calendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.features.calendar.ui.state.CalendarUiState

@Composable
fun CalendarScreen(state: CalendarUiState, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Agenda de hoy", style = MaterialTheme.typography.headlineMedium) }
        item { Text("${state.tasks.size} tareas", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (state.tasks.isEmpty()) item { Text("No hay tareas con fecha para hoy") }
        items(state.tasks, key = { it.id.value }) { task ->
            Card {
                Text(
                    if (task.isCompleted) "✓ ${task.title}" else "○ ${task.title}",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}
