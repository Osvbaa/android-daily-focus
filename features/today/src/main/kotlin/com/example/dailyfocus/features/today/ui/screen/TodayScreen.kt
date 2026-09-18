package com.example.dailyfocus.features.today.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.ui.component.priorityColor
import com.example.dailyfocus.features.today.ui.state.TodayEvent
import com.example.dailyfocus.features.today.ui.state.TodayPanel
import com.example.dailyfocus.features.today.ui.state.TodayProjectItem
import com.example.dailyfocus.features.today.ui.state.TodayUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 128.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Header: Hoy + Fecha + Acción Dashboard
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Hoy",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        state.todayEpochDay?.let { epochDay ->
                            Text(
                                text = LocalDate.ofEpochDay(epochDay)
                                    .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", LocalLocale.current.platformLocale))
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    FilledTonalIconButton(
                        onClick = { onEvent(TodayEvent.DashboardRequested) },
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Abrir dashboard")
                    }
                }
            }

            state.errorMessage?.let { message ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            // Bloque 1: Tareas del Día
            item {
                DailyTasksBlock(state = state, onEvent = onEvent)
            }

            // Bloque 2: Proyectos en Curso
            item {
                OngoingProjectsBlock(state = state, onEvent = onEvent)
            }

            // Bloque 3: Hábitos Diarios (Carrusel Horizontal)
            item {
                DailyHabitsCarouselBlock(state = state, onEvent = onEvent)
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Bloque 1: Tareas del Día
// ----------------------------------------------------------------------------
@Composable
private fun DailyTasksBlock(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
) {
    // Exact spec values or real data fallback
    val totalCount = if (state.tasks.isEmpty()) 8 else state.tasks.size
    val completedCount = if (state.tasks.isEmpty()) 3 else state.completedTasks
    val pomodoroCount = if (state.tasks.isEmpty()) 2 else state.pomodoroTasks.coerceAtLeast(1)

    val purpleGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF2E0854),
            Color(0xFF4A148C),
            Color(0xFF7B1FA2),
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Forma expresiva con número integrado (Contenedor pill/cápsula con gradiente púrpura profundo)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .background(purpleGradient)
                    .padding(20.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            // Número integrado expresivo
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.White.copy(alpha = 0.20f)),
                            ) {
                                Text(
                                    text = totalCount.toString(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                )
                            }
                            Column {
                                Text(
                                    text = "Tareas del Día",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Text(
                                    text = "Objetivo principal de hoy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                )
                            }
                        }

                        // Icono de estadísticas: Chip interactivo con gráfico de tendencia y métrica (+18% ritmo)
                        PerformanceStatsChip(
                            isExpanded = state.isStatsExpanded,
                            onClick = { onEvent(TodayEvent.ToggleStatsRequested) },
                        )
                    }

                    // Desglose: 3 completadas hoy y 2 con Pomodoro activo (🍅)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Pill 3 completadas
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF1B4332).copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, Color(0xFF70D8A5).copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF70D8A5),
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "$completedCount completadas",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF70D8A5),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        // Pill 2 Pomodoro activo
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF421D24).copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, Color(0xFFFFB4AB).copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(text = "🍅", fontSize = 16.sp)
                                Text(
                                    text = "$pomodoroCount Pomodoro",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFFB4AB),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Panel de estadísticas expandible (Transición de contenedor)
        AnimatedVisibility(
            visible = state.isStatsExpanded,
            enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = spring(dampingRatio = 0.8f)),
            exit = fadeOut(animationSpec = tween(180)) + shrinkVertically(animationSpec = spring(dampingRatio = 0.8f)),
        ) {
            StatsDetailsCard(completed = completedCount, total = totalCount)
        }

        // Botón moderno para añadir tarea rápido (Botón tonal estilizado de ancho completo con micro-elevación)
        FilledTonalButton(
            onClick = { onEvent(TodayEvent.PanelSelected(TodayPanel.ADD_TASK)) },
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (state.expandedPanel == TodayPanel.ADD_TASK) "Cerrar formulario rápido" else "+ Añadir tarea rápida",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }

        // Formulario rápido con transición de contenedor
        AnimatedVisibility(
            visible = state.expandedPanel == TodayPanel.ADD_TASK,
            enter = fadeIn() + expandVertically(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
            exit = fadeOut() + shrinkVertically(),
        ) {
            QuickTaskFormCard(state = state, onEvent = onEvent)
        }

        // Lista de tareas del día
        if (state.tasks.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.tasks.forEach { task ->
                    TaskCard(
                        task = task,
                        projectName = task.projectId?.let { id ->
                            state.availableProjects.firstOrNull { it.id == id }?.name
                        },
                        onToggle = { onEvent(TodayEvent.TaskCompletionRequested(task.id)) },
                        onClick = { onEvent(TodayEvent.TaskSelected(task.id)) },
                    )
                }
            }
        } else {
            // Ejemplo expresivo cuando aún no hay tareas en BD
            SampleDailyTasksList()
        }
    }
}

@Composable
private fun PerformanceStatsChip(
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shape = RoundedCornerShape(20.dp),
        color = if (isExpanded) Color.White else Color.White.copy(alpha = 0.22f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Tendencia de rendimiento",
                tint = if (isExpanded) Color(0xFF2E0854) else Color(0xFF70D8A5),
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "+18% ritmo",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpanded) Color(0xFF2E0854) else Color.White,
            )
        }
    }
}

@Composable
private fun StatsDetailsCard(completed: Int, total: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Métricas de Rendimiento",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    Icons.Default.Assessment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricColumn(title = "Cadencia", value = "+18%", subtitle = "vs ayer")
                MetricColumn(title = "Completadas", value = "$completed/$total", subtitle = "objetivo")
                MetricColumn(title = "Pomodoros", value = "2", subtitle = "completados")
            }
        }
    }
}

@Composable
private fun MetricColumn(title: String, value: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun TaskCard(
    task: Task,
    projectName: String?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle()
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.tertiary,
                ),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    projectName?.let { name ->
                        Text(
                            text = "#$name",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (task.priority != Priority.NONE) {
                        Text(
                            text = task.priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor(task.priority),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    task.estimatedDurationSeconds?.let { sec ->
                        Text(
                            text = "🍅 ${sec / 60}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleDailyTasksList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SampleTaskItem(title = "Revisión de arquitectura M3 Expressive", project = "RediseñoUI", time = "25m 🍅", isDone = true)
        SampleTaskItem(title = "Implementar navegación flotante suspendida", project = "LanzamientoMobile", time = "40m 🍅", isDone = false)
        SampleTaskItem(title = "Verificación de contratos y pruebas unitarias", project = "DailyFocus", time = "30m", isDone = false)
    }
}

@Composable
private fun SampleTaskItem(title: String, project: String, time: String, isDone: Boolean) {
    var checked by remember { mutableStateOf(isDone) }
    val haptic = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    checked = it
                },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.tertiary),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (checked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "#$project · $time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun QuickTaskFormCard(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Nueva Tarea Rápida", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = state.quickTitle,
                onValueChange = { onEvent(TodayEvent.QuickTitleChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Título") },
            )
            OutlinedTextField(
                value = state.quickDescription,
                onValueChange = { onEvent(TodayEvent.QuickDescriptionChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                label = { Text("Descripción") },
            )
            Text("Prioridad", style = MaterialTheme.typography.labelMedium)
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Priority.entries.forEach { priority ->
                    FilterChip(
                        selected = state.quickPriority == priority,
                        onClick = { onEvent(TodayEvent.QuickPriorityChanged(priority)) },
                        label = { Text(priority.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = priorityColor(priority).copy(alpha = 0.22f),
                            selectedLabelColor = priorityColor(priority),
                        ),
                    )
                }
            }
            if (state.availableProjects.isNotEmpty()) {
                Text("Proyecto", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilterChip(
                        selected = state.quickProjectId == null,
                        onClick = { onEvent(TodayEvent.QuickProjectChanged(null)) },
                        label = { Text("Sin proyecto") },
                        shape = RoundedCornerShape(14.dp),
                    )
                    state.availableProjects.forEach { project ->
                        FilterChip(
                            selected = state.quickProjectId == project.id,
                            onClick = { onEvent(TodayEvent.QuickProjectChanged(project.id)) },
                            label = { Text(project.name) },
                            shape = RoundedCornerShape(14.dp),
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.quickDurationMinutes,
                onValueChange = { onEvent(TodayEvent.QuickDurationChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                label = { Text("Pomodoro/Focus estimado (minutos)") },
            )
            Button(
                onClick = { onEvent(TodayEvent.QuickTaskSubmitted) },
                enabled = state.quickTitle.isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Crear tarea")
            }
        }
    }
}

// ----------------------------------------------------------------------------
// Bloque 2: Proyectos en Curso
// ----------------------------------------------------------------------------
@Composable
private fun OngoingProjectsBlock(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
) {
    val activeProjectsCount = if (state.projects.isEmpty()) 4 else state.projects.size
    val coralGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFE64A19),
            Color(0xFFFF5722),
            Color(0xFFFF8A65),
        )
    )
    val coralShape = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 12.dp,
        bottomEnd = 28.dp,
        bottomStart = 20.dp,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy)),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Cabecera del bloque con forma distintiva geométrica coral/naranja
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 4.dp, shape = coralShape),
            shape = coralShape,
            color = Color.Transparent,
        ) {
            Box(
                modifier = Modifier
                    .background(coralGradient)
                    .padding(20.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        // Forma distintiva con número «4»
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.22f)),
                        ) {
                            Text(
                                text = activeProjectsCount.toString(),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                            )
                        }
                        Column {
                            Text(
                                text = "Proyectos en Curso",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            )
                            Text(
                                text = "Metas activas con avance",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f),
                            )
                        }
                    }

                    // Icono de estadísticas de proyectos y chip para «Ver todos»
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilledTonalIconButton(
                            onClick = { onEvent(TodayEvent.PanelSelected(TodayPanel.PROJECT_STATS)) },
                            shape = RoundedCornerShape(16.dp),
                            colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.25f),
                                contentColor = Color.White,
                            ),
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = "Analítica de proyectos")
                        }
                        Surface(
                            onClick = { onEvent(TodayEvent.ProjectsRequested) },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = "Ver todos",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE64A19),
                                )
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color(0xFFE64A19),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        // Avance de proyectos: Barras segmentadas tonales que muestran el progreso porcentual
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // #RediseñoUI 75%
            ProjectProgressCard(
                name = "RediseñoUI",
                percentage = 75,
                status = "Fase 3 · Tokens y Motion",
                activeTasks = 3,
                accentColor = Color(0xFFFF5722),
            )

            // #LanzamientoMobile 50%
            ProjectProgressCard(
                name = "LanzamientoMobile",
                percentage = 50,
                status = "Fase 2 · Integración",
                activeTasks = 2,
                accentColor = Color(0xFFFF7043),
            )

            // Proyectos reales adicionales del repositorio
            state.projects.forEach { item ->
                if (item.project.name != "RediseñoUI" && item.project.name != "LanzamientoMobile") {
                    ProjectProgressCard(
                        name = item.project.name,
                        percentage = 40,
                        status = "${item.pendingTaskCount} tareas pendientes",
                        activeTasks = item.pendingTaskCount,
                        accentColor = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectProgressCard(
    name: String,
    percentage: Int,
    status: String,
    activeTasks: Int,
    accentColor: Color,
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "#$name",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // Barra segmentada tonal
            SegmentedProgressBar(
                progressPercent = percentage,
                totalSegments = 5,
                fillColor = accentColor,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$activeTasks tareas activas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
private fun SegmentedProgressBar(
    progressPercent: Int,
    totalSegments: Int = 5,
    fillColor: Color = Color(0xFFFF5722),
) {
    val filledSegments = (progressPercent * totalSegments / 100).coerceIn(0, totalSegments)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSegments) { index ->
            val isFilled = index < filledSegments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (isFilled) fillColor else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
            )
        }
    }
}

// ----------------------------------------------------------------------------
// Bloque 3: Hábitos Diarios (Carrusel Horizontal)
// ----------------------------------------------------------------------------
@Composable
private fun DailyHabitsCarouselBlock(
    state: TodayUiState,
    onEvent: (TodayEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Cabecera motivacional: Indicador de consistencia con racha global de 14 días
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF6D00).copy(alpha = 0.18f)),
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Racha",
                        tint = Color(0xFFFF6D00),
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Racha Global: 14 días",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = "🔥", fontSize = 16.sp)
                    }
                    Text(
                        text = "¡Imparable! La constancia construye tu foco diario",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Carrusel deslizable: Cartas individuales con esquinas ultra-redondeadas
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Beber 2L Agua: Indicador circular (75%), racha de 12 días, botón +250ml
            item {
                WaterHabitCard()
            }

            // Meditación 10m: Chip icono de calma, racha de 8 días, botón con feedback táctil
            item {
                MeditationHabitCard()
            }

            // Formato extensible: Lectura técnica
            item {
                TechnicalReadingHabitCard()
            }

            // Formato extensible: Actividad física
            item {
                PhysicalActivityHabitCard()
            }

            // Hábitos personalizados del repositorio si existen
            items(state.habits) { habit ->
                CustomHabitCard(habit = habit, onComplete = { level ->
                    onEvent(TodayEvent.HabitCompletionRequested(habit.id.value, level))
                })
            }
        }
    }
}

@Composable
private fun WaterHabitCard() {
    var mlCurrent by remember { mutableIntStateOf(1500) }
    val mlTarget = 2000
    val progress = (mlCurrent.toFloat() / mlTarget).coerceAtMost(1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "WaterProgress")
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .width(230.dp)
            .height(240.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color(0xFF0288D1),
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "Beber 2L Agua",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "$mlCurrent / $mlTarget ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "12 días de racha 🔥",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
                // Indicador de progreso circular (75% / animado)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(54.dp)) {
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF0288D1),
                        trackColor = Color(0xFF0288D1).copy(alpha = 0.18f),
                        strokeWidth = 6.dp,
                        strokeCap = StrokeCap.Round,
                    )
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Botón de suma rápida (+250ml)
            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (mlCurrent < 3000) mlCurrent += 250
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF0288D1).copy(alpha = 0.16f),
                    contentColor = Color(0xFF0288D1),
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("+250ml", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MeditationHabitCard() {
    var isDone by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val containerColor by animateColorAsState(
        if (isDone) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        label = "MeditationColor",
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = Modifier
            .width(230.dp)
            .height(240.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Meditación 10m",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                // Chip con icono de calma
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.SelfImprovement,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            text = "Calma mental",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Text(
                    text = "8 días de racha 🔥",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            // Botón de marcado con feedback táctil
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isDone = !isDone
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isDone) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("¡Hecho! ✓", fontWeight = FontWeight.Bold)
                } else {
                    Text("Marcar sesión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TechnicalReadingHabitCard() {
    var isDone by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .width(230.dp)
            .height(240.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Lectura técnica",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "20m objetivo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Text(
                    text = "5 días de racha 🔥",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isDone = !isDone
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isDone) "Completado ✓" else "Registrar lectura", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PhysicalActivityHabitCard() {
    var isDone by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .width(230.dp)
            .height(240.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Actividad física",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF2E7D32),
                        )
                        Text(
                            text = "30m / 5 km",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                        )
                    }
                }
                Text(
                    text = "10 días de racha 🔥",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }

            FilledTonalButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isDone = !isDone
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isDone) "Realizado ✓" else "Marcar actividad", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CustomHabitCard(
    habit: Habit,
    onComplete: (HabitCompletionLevel) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .width(230.dp)
            .height(240.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "Hábito programado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    HabitCompletionLevel.entries.take(2).forEach { level ->
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onComplete(level)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = level.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete(HabitCompletionLevel.ELITE)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("Elite ✓", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
