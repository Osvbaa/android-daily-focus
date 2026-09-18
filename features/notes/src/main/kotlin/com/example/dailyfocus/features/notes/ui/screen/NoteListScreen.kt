package com.example.dailyfocus.features.notes.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.core.model.Note
import com.example.dailyfocus.features.notes.ui.state.NoteListEvent
import com.example.dailyfocus.features.notes.ui.state.NoteListUiState

@Composable
fun NoteListScreen(state: NoteListUiState, onEvent: (NoteListEvent) -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = { FloatingActionButton(onClick = { onEvent(NoteListEvent.CreateRequested) }) { androidx.compose.material3.Icon(Icons.Default.Add, "Crear nota") } },
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Notas", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(NoteListEvent.QueryChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Buscar notas") },
                singleLine = true,
            )
            when {
                state.isLoading -> CircularProgressIndicator()
                state.errorMessage != null -> {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { onEvent(NoteListEvent.Retry) }) { Text("Reintentar") }
                }
                state.notes.isEmpty() -> Text("Escribe tu primera idea")
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.notes.filter { note ->
                        state.query.isBlank() || note.title.contains(state.query, true) || note.content.contains(state.query, true)
                    }, key = Note::id) { note ->
                        Card(onClick = { onEvent(NoteListEvent.NoteSelected(note.id)) }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(note.title.ifBlank { "Sin título" }, style = MaterialTheme.typography.titleMedium)
                                Text(note.content.take(120), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
