package com.example.dailyfocus.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.ui.components.TaskCard
import java.time.format.DateTimeFormatter
import com.example.dailyfocus.data.model.Task
import kotlin.collections.groupBy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(modifier : Modifier = Modifier) {
    val tasks = remember {
        mutableStateListOf(
            Task(title = "Aprender LazyLayouts", description = "Entender keys"),
            Task(title = "Refactorizar Daily Focus", isCompleted = true),
            Task(title = "Entender Inmutabilidad", description = "Usar @Immutable"),
            Task(title = "Aprender Inglés", description = "Practicar nuevo vocabulario"),
            Task(title = "Practicar speaking", description = "Hablar por 5 minutos en inglés"),
            Task(title = "Aprender a utilizar LazyLists"),
            Task(title = "Hacer tarea", isCompleted = true),
            Task(title = "Estudiar Android", description = "Practicar conceptos nuevos"),
            Task(title = "Leer", description = "Leer libros por 5 minutos en inglés"),
            Task(title = "Trabajar sobre Daily Focus"),
            Task(title = "Limpiar el cuarto", isCompleted = true)
        )
    }

    val groupedTasks by remember {
        derivedStateOf {
            tasks.groupBy { task ->
                // Formateamos la fecha a String para usarla como separador visual
                task.createdAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
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
        modifier = Modifier.padding(all = 12.dp)
    )

}
@Composable
private fun HeaderSection(
    groupedTasks: Map<String, List<Task>>,
    onTaskCheckedChange: (Task, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(height = 12.dp))
            Text(text = "Daily Focus", style = MaterialTheme.typography.headlineLarge)
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
                        .background(color = MaterialTheme.colorScheme.primary)
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
                TaskCard(
                    task = task,
                    onCheckedChange = { newState ->
                        onTaskCheckedChange(task, newState)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskScreenPreview() {
    TaskScreen()
}