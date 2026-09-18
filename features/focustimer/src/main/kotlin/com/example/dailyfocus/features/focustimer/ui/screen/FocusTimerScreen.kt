package com.example.dailyfocus.features.focustimer.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.core.model.FocusTimerPhase
import com.example.dailyfocus.features.focustimer.ui.state.FocusTimerEvent
import com.example.dailyfocus.features.focustimer.ui.state.FocusTimerUiState

@Composable
fun FocusTimerScreen(state: FocusTimerUiState, onEvent: (FocusTimerEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Text("Tiempo de enfoque", style = MaterialTheme.typography.headlineMedium)
        Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { state.timer.remainingSeconds.toFloat() / state.timer.durationSeconds.coerceAtLeast(1) },
                    modifier = Modifier.size(240.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeWidth = 12.dp,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%02d:%02d".format(state.minutes, state.seconds), style = MaterialTheme.typography.displayLarge)
                    Text(when (state.phase) {
                        FocusTimerPhase.RUNNING -> "En curso"
                        FocusTimerPhase.PAUSED -> "En pausa"
                        FocusTimerPhase.COMPLETED -> "Sesión terminada"
                        FocusTimerPhase.IDLE -> "Listo para empezar"
                    }, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Text("${state.timer.completedSessions} sesiones completadas", style = MaterialTheme.typography.bodyMedium)
        val target = state.timer.linkedTaskId?.let { "Tarea vinculada: ${it.value}" }
            ?: state.timer.linkedHabitId?.let { "Habito vinculado: ${it.value}" }
        target?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state.phase) {
                FocusTimerPhase.RUNNING -> Button(onClick = { onEvent(FocusTimerEvent.Pause) }) { Text("Pausar") }
                else -> Button(onClick = { onEvent(FocusTimerEvent.Start) }) { Text(if (state.phase == FocusTimerPhase.PAUSED) "Continuar" else "Iniciar") }
            }
            OutlinedButton(onClick = { onEvent(FocusTimerEvent.Reset) }) { Text("Reiniciar") }
        }
    }
}
