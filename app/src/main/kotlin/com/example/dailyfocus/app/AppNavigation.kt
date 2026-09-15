package com.example.dailyfocus.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.dailyfocus.features.dashboard.navigation.dashboardNavGraph
import com.example.dailyfocus.features.tasks.navigation.taskNavGraph

@Composable
fun AppNavigation(
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState
) {
    NavDisplay(backStack = backStack) { key ->
        taskNavGraph(key, backStack, snackbarHostState)
            ?: dashboardNavGraph(key)
            ?: error("Unknown key: $key")
    }
}
