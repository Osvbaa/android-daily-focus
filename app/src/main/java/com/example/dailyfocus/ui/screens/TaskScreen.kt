package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.ui.components.TaskCard
import java.time.format.DateTimeFormatter
import com.example.dailyfocus.data.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlin.collections.groupBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    tasks: MutableList<Task>,
    onDeleteTask: (Task) -> Unit,
    modifier : Modifier = Modifier
) {

    val groupedTasks by remember {
        derivedStateOf {
            tasks.groupBy { task ->
                // Formateamos la fecha a String para usarla como separador visual
                task.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
                .mapValues { it.value.toImmutableList() } // Convierte cada elemento de la lista a un ImmutableList
                .toImmutableMap() // Convierte el mapa resultante
        }
    }

    HeaderSection(
        groupedTasks = groupedTasks,
        onTaskCheckedChange = { task, isChecked ->
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = tasks[index].copy(isCompleted = isChecked)
            }
        },
        onDelete = onDeleteTask,
        modifier = modifier.padding(all = 12.dp)
    )
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeaderSection(
    groupedTasks : ImmutableMap<String, ImmutableList<Task>>,
    onTaskCheckedChange : (Task, Boolean) -> Unit,
    onDelete : (Task) -> Unit,
    modifier : Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(height = 12.dp))
            Text(
                text = "Daily Focus",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(height = 16.dp))
        }

        groupedTasks.forEach { (dateHeader, tasksForDate) ->
            stickyHeader(
                key = "header_$dateHeader", // Identidad única para la Slot Table
                contentType = "header_type" // Molde para reciclaje en memoria
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(all = 8.dp)
                ) {
                    Text(
                        text = dateHeader,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            items(
                items = tasksForDate,
                key = { task -> task.id },
                contentType = { "task_item" }
            ) { task ->
                TaskCardSwipe(
                    task = task,
                    onTaskCheckedChange = onTaskCheckedChange,
                    onDelete = onDelete,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}
@Composable
fun TaskCardSwipe(
    task : Task,
    onTaskCheckedChange : (Task, Boolean) -> Unit,
    onDelete : (Task) -> Unit,
    modifier : Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    // Observamos el cambio de estado para ejecutar la acción de borrado
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete(task)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { DismissBackground(dismissState) },
        enableDismissFromEndToStart = true,
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        TaskCard(
            task = task,
            onCheckedChange = { newState ->
                onTaskCheckedChange(task, newState)
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
        Color.Red
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(all = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
    }
}