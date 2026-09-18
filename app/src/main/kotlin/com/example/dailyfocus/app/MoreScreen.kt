package com.example.dailyfocus.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.calendar.navigation.CalendarRoute
import com.example.dailyfocus.features.focustimer.navigation.FocusTimerRoute
import com.example.dailyfocus.features.habits.navigation.HabitsRoute
import com.example.dailyfocus.features.projects.navigation.ProjectsRoute

@Composable
fun MoreScreen(onNavigate: (NavKey) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Explorar", style = MaterialTheme.typography.headlineMedium)
                Text("Organiza lo que importa y mantén el ritmo", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item { DestinationCard("Calendario", "Ve las tareas de hoy", Icons.Default.CalendarMonth, CalendarRoute, onNavigate) }
        item { DestinationCard("Proyectos", "Da forma a tus metas", Icons.Default.Folder, ProjectsRoute, onNavigate) }
        item { DestinationCard("Hábitos", "Construye constancia", Icons.Default.Repeat, HabitsRoute, onNavigate) }
        item { DestinationCard("Focus", "Dedica tiempo sin distracciones", Icons.Default.Timer, FocusTimerRoute(), onNavigate) }
    }
}

@Composable
private fun DestinationCard(label: String, description: String, icon: ImageVector, route: NavKey, onNavigate: (NavKey) -> Unit) {
    Card(onClick = { onNavigate(route) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}
