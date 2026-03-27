package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.ui.components.StatCard

@Composable
fun DashboardMainScreen(
    stats: List<StatItem>,
    modifier: Modifier = Modifier
) {
    val total = stats.find { statTitle -> statTitle.title == "Total tareas" }?.value?.toIntOrNull() ?: 1
    val completed = stats.find { statTitle -> statTitle.title == "Completadas" }?.value?.toIntOrNull() ?: 0
    val progress = if (total > 0) completed.toFloat() / total else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(height = 24.dp))
            Text(
                text = "Resumen de Actividad",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = Bold)
            )
        }

        // Gráfico de progreso general
        item {
            MainProgressCard(progress = progress)
        }

        // Cuadrícula de estadisticas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                stats.filter { statTitle -> statTitle.title != "Total tareas" }.forEach { stat ->
                    StatCard(stat = stat, modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            StatCard(stat = stats.first { statTitle -> statTitle.title == "Total tareas" })
        }
    }
}

@Composable
fun MainProgressCard(progress: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(modifier = Modifier.padding(all = 24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Progreso Diario",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Chip de porcentaje
                SuggestionChip(
                    onClick = { },
                    label = { Text("${(progress * 100).toInt()}%") },
                    shape = CircleShape,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        labelColor = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(height = 16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            Text(
                text = if (progress >= 1f) "¡Todas las tareas completadas! 🎉" else "Casi llegas a tu meta de hoy",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
