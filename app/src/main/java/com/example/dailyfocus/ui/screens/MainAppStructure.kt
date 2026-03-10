package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.dailyfocus.data.model.Task
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
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

    val snackbarHostState = remember { SnackbarHostState() }

    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(value = "Tasks") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "Tasks",
                    onClick = { currentScreen = "Tasks" },
                    icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Tasks")},
                    label = { Text(text = "Tasks") },
                )

                NavigationBarItem(
                    selected = currentScreen == "Dashboard",
                    onClick = { currentScreen = "Dashboard" },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Estadísticas") },
                    label = { Text("Statistics") }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "Tasks" -> TaskScreen(
                    tasks = tasks,
                    onDeleteTask = { taskToDelete ->
                        val index = tasks.indexOfFirst { it.id == taskToDelete.id } // Guardo el índice exacto antes de eliminar (para el Deshacer)

                        if (index != -1) {
                            val removedTask = tasks[index]
                            tasks.removeAt(index = index)

                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Tarea eliminada",
                                    actionLabel = "Deshacer"
                                )

                                if (result == SnackbarResult.ActionPerformed) {
                                    tasks.add(index, element = removedTask)
                                }
                            }
                        }
                    }
                )
                "Dashboard" -> DashboardMainScreen()
            }
        }
    }
}
