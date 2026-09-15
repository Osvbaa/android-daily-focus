package com.example.dailyfocus.app

import android.os.Build
import android.annotation.SuppressLint
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.dailyfocus.features.dashboard.navigation.DashboardRoute
import com.example.dailyfocus.features.tasks.navigation.TaskEditorRoute
import com.example.dailyfocus.features.tasks.navigation.TaskRoute

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainAppStructure(initialTaskId: String? = null) {
    val snackbarHostState = remember { SnackbarHostState() } // Estado para el Snackbar
    val backStack = rememberNavBackStack(TaskRoute) // Estado para la pantalla actual utilizando Navigation
    LaunchedEffect(initialTaskId) {
        if (initialTaskId != null) {
            backStack.clear()
            backStack.add(TaskRoute)
            backStack.add(TaskEditorRoute(initialTaskId))
        }
    }

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
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavigation(backStack = backStack, snackbarHostState = snackbarHostState)
        }
    }
}
