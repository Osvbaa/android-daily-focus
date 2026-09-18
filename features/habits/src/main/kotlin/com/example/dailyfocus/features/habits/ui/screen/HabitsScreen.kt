package com.example.dailyfocus.features.habits.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.features.habits.ui.state.HabitListEvent
import com.example.dailyfocus.features.habits.ui.state.HabitListUiState

@Composable
fun HabitsScreen(state: HabitListUiState, onEvent: (HabitListEvent) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Hábitos", style = MaterialTheme.typography.headlineMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(state.name, { onEvent(HabitListEvent.NameChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Nombre") }, singleLine = true)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HabitFrequency.values().forEach { frequency ->
                        FilterChip(
                            selected = state.frequency == frequency,
                            onClick = { onEvent(HabitListEvent.FrequencyChanged(frequency)) },
                            label = { Text(frequencyLabel(frequency)) },
                        )
                    }
                }
                if (state.frequency == HabitFrequency.EVERY_N_DAYS) {
                    OutlinedTextField(state.intervalDays, { onEvent(HabitListEvent.IntervalChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Cada cuántos días") }, singleLine = true)
                }
                if (state.frequency == HabitFrequency.TIMES_PER_WEEK) {
                    OutlinedTextField(state.occurrencesPerWeek, { onEvent(HabitListEvent.OccurrencesChanged(it)) }, Modifier.fillMaxWidth(), label = { Text("Veces por semana") }, singleLine = true)
                }
                Button(onClick = { onEvent(HabitListEvent.Create) }, enabled = state.name.isNotBlank()) { Text("Crear hábito") }
                state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        }
        items(state.habits, key = { it.id.value }) { habit ->
            Card(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium)
                        Text(frequencyDescription(habit), style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = { onEvent(HabitListEvent.Archive(habit.id.value)) }) { Text("Archivar") }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HabitCompletionLevel.values().forEach { level ->
                        Button(onClick = { onEvent(HabitListEvent.Complete(habit.id.value, level)) }) { Text(level.name) }
                    }
                }
            }
            }
        }
    }
}

private fun frequencyLabel(frequency: HabitFrequency) = when (frequency) {
    HabitFrequency.DAILY -> "Diario"
    HabitFrequency.WEEKLY -> "Semanal"
    HabitFrequency.TIMES_PER_WEEK -> "X/semana"
    HabitFrequency.EVERY_N_DAYS -> "Cada X días"
}

private fun frequencyDescription(habit: com.example.dailyfocus.core.model.Habit) = when (habit.frequency) {
    HabitFrequency.DAILY -> "Diario"
    HabitFrequency.WEEKLY -> "Una vez por semana"
    HabitFrequency.TIMES_PER_WEEK -> "${habit.occurrencesPerWeek} veces por semana"
    HabitFrequency.EVERY_N_DAYS -> "Cada ${habit.intervalDays} días"
}
