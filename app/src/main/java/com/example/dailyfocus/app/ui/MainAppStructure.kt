package com.example.dailyfocus.app.ui

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.dailyfocus.app.navigation.AppNavigation
import com.example.dailyfocus.features.dashboard.navigation.DashboardRoute
import com.example.dailyfocus.features.tasks.navigation.TaskRoute

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    val snackbarHostState = remember { SnackbarHostState() } // Estado para el Snackbar
    val backStack =
        rememberNavBackStack(TaskRoute) // Estado para la pantalla actual utilizando Navigation

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
                    icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Tasks") },
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
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Estadísticas"
                        )
                    },
                    label = { Text("Statistics") }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) // Componente para mostrar el Snackbar
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(paddingValues = innerPadding)) {
            AppNavigation(backStack = backStack, snackbarHostState = snackbarHostState)
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
