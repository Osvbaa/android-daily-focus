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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.data.model.Task
import com.example.dailyfocus.ui.navigation.TaskRoute
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dailyfocus.ui.navigation.DashboardRoute
import com.example.dailyfocus.ui.navigation.TaskDetailRoute

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

    val dashboardStats by remember {
        derivedStateOf {
            val total = tasks.size
            val completed = tasks.count { it.isCompleted }
            val pending = total - completed

            listOf(
                StatItem(title = "Total tareas", value = total.toString()),
                StatItem(title = "Completadas", value = completed.toString()),
                StatItem(title = "Pendientes", value = pending.toString())
            )
        }
    }

    val snackbarHostState = remember { SnackbarHostState() } // Estado para el Snackbar

    val coroutineScope = rememberCoroutineScope() // Alcance para las corrutinas

    /*
    // Estado para la pantalla actual utilizando texto (sin Navigation)
    var currentScreen by remember { mutableStateOf(value = "Tasks") }
    */

    // Estado para la pantalla actual utilizando Navigation
    val backStack = rememberNavBackStack(TaskRoute)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    // hasRoute verifica si el destino actual tiene la ruta especificada
                    selected = backStack.lastOrNull() is TaskRoute,
                    // utilizamos navigate en el navController para navegar a la ruta
                    onClick = {
                        if (backStack.lastOrNull() !is TaskRoute) {
                            backStack.clear()
                            backStack.add(TaskRoute)
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Tasks")},
                    label = { Text(text = "Tasks") },
                )

                NavigationBarItem(
                    selected = backStack.lastOrNull() is DashboardRoute,
                    onClick = {
                        if (backStack.lastOrNull() !is DashboardRoute) {
                            backStack.removeIf { currentBackStack -> currentBackStack !is TaskRoute }
                            backStack.add(DashboardRoute)
                        }
                    },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Estadísticas") },
                    label = { Text("Statistics") }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) // Componente para mostrar el Snackbar
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(paddingValues = innerPadding)) {
            
            NavDisplay(
                backStack = backStack,
            ) { key ->
                when(key) {
                    is TaskRoute ->
                        NavEntry(key) {
                            TaskScreen(
                                tasks = tasks,
                                onDeleteTask = { taskToDelete ->
                                    val index = tasks.indexOfFirst { task -> task.id == taskToDelete.id } // Guardo el índice exacto antes de eliminar (para el Deshacer)

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
                                },
                                onTaskClick = { clickedTaskId ->
                                    backStack.add(TaskDetailRoute(taskId = clickedTaskId))
                                }
                            )
                        }
                    is DashboardRoute ->
                        NavEntry(key) {
                            DashboardMainScreen(stats = dashboardStats)
                        }
                    is TaskDetailRoute ->
                        NavEntry(key) {
                            TaskDetailScreen(taskId = key.taskId) {
                                backStack.removeLastOrNull()
                            }
                        }
                    else -> error("Unknown key $key")
                }
            }
            /*
            Navigation 2
            NavHost(
                navController = navController,
                startDestination = TaskRoute
            ) {
                //Nodo 1: La pantalla de las tareas
                //composable es una función constructura que pertenece a NavGraphBuilder
                //su función es registrar un "nodo" en el mapa topológico del NavHost
                composable<TaskRoute> {
                    TaskScreen(
                        tasks = tasks,
                        onDeleteTask = { taskToDelete ->
                            val index = tasks.indexOfFirst { task -> task.id == taskToDelete.id } // Guardo el índice exacto antes de eliminar (para el Deshacer)

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
                        },
                        onTaskClick = { clickedTaskId ->
                            navController.navigate(route = TaskDetailRoute(taskId = clickedTaskId))
                        }
                    )
                }
                //Nodo 2: La pantalla de las estadísticas
                composable<DashboardRoute> {
                    DashboardMainScreen(stats = dashboardStats)
                }
                //Nodo 3: La pantalla de detalles/edición de tareas
                composable<TaskDetailRoute> { backStackEntry ->
                    //deserializamos el parámetro de la ruta
                    val route = backStackEntry.toRoute<TaskDetailRoute>()

                    TaskDetailScreen(taskId = route.taskId) {
                        navController.popBackStack()
                    }
                }
            }*/
        }
    }
}
