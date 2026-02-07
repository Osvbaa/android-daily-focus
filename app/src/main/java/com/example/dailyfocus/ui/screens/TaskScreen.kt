package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.ui.components.TaskCard
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.example.dailyfocus.data.model.Task

@Composable
fun TaskScreen(modifier : Modifier = Modifier) {

    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    val listTasks = remember {
        mutableStateListOf(
            Task(isCompleted = false, title = "Task 1", description = "Description 1", date = LocalDateTime.now()),
            Task(isCompleted = false, title = "Task 2", description = null, date = LocalDateTime.now()),
            Task(isCompleted = false, title = "Task 3", description = "Description 3", date = LocalDateTime.now()),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 24.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Daily Focus",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(height = 16.dp))

        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            text = "Hola. Hoy es $currentDate",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(height = 40.dp))

        listTasks.forEachIndexed { index, task ->
            TaskCard(
                modifier = Modifier.padding(all = 8.dp),
                title = task.title,
                description = task.description,
                checked = task.isCompleted,
                onCheckedChange = { newState ->
                    listTasks[index] = task.copy(isCompleted = newState)
                }
            )
        }
    }
}