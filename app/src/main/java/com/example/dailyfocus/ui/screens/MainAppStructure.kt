package com.example.dailyfocus.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dailyfocus.ui.navigation.TaskRoute
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.dailyfocus.ui.navigation.DashboardRoute
import com.example.dailyfocus.ui.navigation.TaskDetailRoute
import com.example.dailyfocus.ui.viewmodel.AppViewModelProvider
import com.example.dailyfocus.ui.viewmodel.DashboardViewModel
import com.example.dailyfocus.ui.viewmodel.TaskDetailViewModel
import com.example.dailyfocus.ui.viewmodel.TaskViewModel
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    // 1. Sintonizamos el flujo de Items de UI (que ya traen la fecha formateada)
    val snackbarHostState = remember { SnackbarHostState() } // Estado para el Snackbar
    val backStack = rememberNavBackStack(TaskRoute) // Estado para la pantalla actual utilizando Navigation

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
                            val viewModel: TaskViewModel = viewModel(factory = AppViewModelProvider.Factory)
                            val groupedTasks by viewModel.groupedTasks.collectAsStateWithLifecycle()

                            // 2. ESCUCHAMOS LOS EVENTOS (SharedFlow)
                            // Cuando el Chef diga "Tarea eliminada", el Mesero muestra el Snackbar
                            LaunchedEffect(key1 = Unit) {
                                viewModel.uiEvent.collect { message ->
                                    val result = snackbarHostState.showSnackbar(
                                        message = message,
                                        actionLabel = "Deshacer"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            }

                            TaskScreen(
                                onDeleteTask = { taskToDelete ->
                                    viewModel.deleteTask(taskId = taskToDelete.id)
                                },
                                onTaskClick = { clickedTaskId ->
                                    backStack.add(TaskDetailRoute(taskId = clickedTaskId))
                                },
                                onCheckedChange = { task ->
                                    viewModel.toggleTaskCompletion(task = task)
                                },
                                groupedTasks = groupedTasks
                            )
                        }
                    is DashboardRoute ->
                        NavEntry(key) {
                            val viewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
                            val dashboardStats by viewModel.stats.collectAsStateWithLifecycle()
                            DashboardMainScreen(stats = dashboardStats)
                        }
                    is TaskDetailRoute ->
                        NavEntry(key) {
                            val viewModel: TaskDetailViewModel = viewModel(
                                key = key.taskId,
                                factory = AppViewModelProvider.factoryForTaskDetail(key.taskId)
                            )

                            LaunchedEffect(key1 = key.taskId) {
                                viewModel.refresh()
                            }
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val canSave by viewModel.isEntryValid.collectAsStateWithLifecycle()

                            TaskDetailScreen(
                                title = uiState.title,
                                description = uiState.description,
                                isCompleted = uiState.isCompleted,
                                isSaveEnabled = canSave,
                                onValueChangeTitle = { newTitle ->
                                    viewModel.onValueChangeTitle(newTitle = newTitle) },
                                onValueChangeDescription = { newDescription ->
                                    viewModel.onValueChangeDescription(newDescription = newDescription)
                                },
                                onToggleCompleted = { isCompleted ->
                                    viewModel.onToggleCompleted(completed = isCompleted)
                                },
                                onSaveTaskChanges = {
                                    if (canSave) {
                                        viewModel.onSaveTaskChanges()
                                        backStack.removeLast()
                                    }
                                },
                                onNavigateBack = {
                                    if (backStack.size > 1) backStack.removeLast()
                                }
                            )
                        }
                    else -> error("Unknown key $key")
                }
            }
        }
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
