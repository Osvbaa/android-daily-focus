package com.example.dailyfocus.features.tasks.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.features.tasks.ui.state.TaskReminderEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskReminderUiState
import com.example.dailyfocus.features.tasks.reminder.TaskReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskReminderScreen(
    state: TaskReminderUiState,
    onEvent: (TaskReminderEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    LaunchedEffect(state.settings.enabled, state.settings.hour, state.settings.minute) {
        val scheduler = TaskReminderScheduler(context)
        scheduler.sync(state.settings)
    }
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Recordatorio diario") }) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().widthIn(max = 640.dp).padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("Pendientes de hoy", style = MaterialTheme.typography.titleLarge)
                    Text("Un resumen aproximado, sin avisos vacíos.", style = MaterialTheme.typography.bodyMedium)
                }
                Switch(checked = state.settings.enabled, onCheckedChange = { onEvent(TaskReminderEvent.SetEnabled(it)) })
            }
            OutlinedTextField(
                value = "%02d:%02d".format(state.settings.hour, state.settings.minute),
                onValueChange = { text ->
                    val parts = text.split(":")
                    if (parts.size == 2) {
                        val hour = parts[0].toIntOrNull()
                        val minute = parts[1].toIntOrNull()
                        if (hour != null && minute != null) onEvent(TaskReminderEvent.SetTime(hour, minute))
                    }
                },
                enabled = state.settings.enabled,
                label = { Text("Hora local") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FilterChip(
                selected = state.settings.enabled,
                onClick = { onEvent(TaskReminderEvent.SetEnabled(!state.settings.enabled)) },
                label = { Text(if (state.settings.enabled) "Activo" else "Desactivado") },
            )
            state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Listo") }
            TextButton(onClick = { onEvent(TaskReminderEvent.Retry) }) { Text("Volver a cargar") }
        }
    }
}
