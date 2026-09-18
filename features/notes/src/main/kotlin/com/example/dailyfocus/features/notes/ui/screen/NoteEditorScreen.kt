package com.example.dailyfocus.features.notes.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.features.notes.ui.state.NoteEditorEvent
import com.example.dailyfocus.features.notes.ui.state.NoteEditorPhase
import com.example.dailyfocus.features.notes.ui.state.NoteEditorUiState

@Composable
fun NoteEditorScreen(state: NoteEditorUiState, onEvent: (NoteEditorEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp)) {
        Text("Editor de notas", style = MaterialTheme.typography.headlineMedium)
        if (state.phase == NoteEditorPhase.Missing) Text("La nota ya no existe")
        if (state.errorMessage != null) Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
        OutlinedTextField(
            value = state.title,
            onValueChange = { onEvent(NoteEditorEvent.TitleChanged(it)) },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            label = { Text("Título") },
            singleLine = true,
        )
        OutlinedTextField(
            value = state.content,
            onValueChange = { onEvent(NoteEditorEvent.ContentChanged(it)) },
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 12.dp),
            label = { Text("Contenido Markdown") },
        )
        Button(onClick = { onEvent(NoteEditorEvent.Save) }, enabled = !state.isLoading, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text(if (state.phase == NoteEditorPhase.Saving) "Guardando…" else "Guardar")
        }
        Button(onClick = { onEvent(NoteEditorEvent.ExtractTasks) }, enabled = !state.isExtracting && state.content.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text(if (state.isExtracting) "Analizando localmente…" else "Extraer tareas con IA local")
        }
        state.suggestions.forEach { suggestion ->
            androidx.compose.foundation.layout.Row {
                Checkbox(checked = suggestion.id in state.selectedSuggestionIds, onCheckedChange = { onEvent(NoteEditorEvent.SuggestionSelected(suggestion.id, it)) })
                Text(suggestion.title, modifier = Modifier.padding(top = 12.dp))
            }
        }
        if (state.suggestions.isNotEmpty()) Button(onClick = { onEvent(NoteEditorEvent.ConfirmSuggestions) }, modifier = Modifier.fillMaxWidth()) { Text("Agregar tareas seleccionadas") }
        Button(onClick = { onEvent(NoteEditorEvent.Back) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("Volver") }
    }
}
