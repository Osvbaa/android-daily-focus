package com.example.dailyfocus.ui.screens

import android.preference.PreferenceActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
            Task(title = "Hacer ejercicio", isCompleted = true)
        )
    }

    Scaffold (modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues = innerPadding),
            contentPadding = PaddingValues(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            item {
                HeaderSection()
                Spacer(modifier = Modifier.height(height = 16.dp))
            }

            items(
                items = tasks,
                key = { task -> task.id },
                contentType = { "task_item "}
            ) { task ->
                TaskCard(
                    task = task,
                    onCheckedChange = { isChecked ->
                        val index = tasks.indexOfFirst { it.id == task.id }
                        if (index != -1) {
                            tasks[index] = tasks[index].copy(isCompleted = isChecked)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "Daily Focus",
            style = MaterialTheme.typography.headlineLarge
        )
        Text(
            text = "Hoy es ${LocalDate.now()}",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}