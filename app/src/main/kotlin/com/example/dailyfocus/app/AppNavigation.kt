package com.example.dailyfocus.app

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.example.dailyfocus.features.dashboard.navigation.dashboardNavGraph
import com.example.dailyfocus.features.notes.navigation.notesNavGraph
import com.example.dailyfocus.features.focustimer.navigation.focusTimerNavGraph
import com.example.dailyfocus.features.calendar.navigation.calendarNavGraph
import com.example.dailyfocus.features.tasks.navigation.taskNavGraph
import com.example.dailyfocus.features.projects.navigation.projectsNavGraph
import com.example.dailyfocus.features.habits.navigation.habitsNavGraph
import com.example.dailyfocus.features.focustimer.navigation.FocusTimerRoute
import com.example.dailyfocus.features.tasks.navigation.TaskEditorRoute
import com.example.dailyfocus.features.dashboard.navigation.DashboardRoute
import com.example.dailyfocus.features.projects.navigation.ProjectsRoute
import com.example.dailyfocus.features.today.navigation.todayNavGraph

@Composable
fun AppNavigation(
    backStack: NavBackStack<NavKey>,
    snackbarHostState: SnackbarHostState
) {
    NavDisplay(backStack = backStack) { key ->
        if (key is MoreRoute) {
            androidx.navigation3.runtime.NavEntry(key) {
                MoreScreen(onNavigate = { destination ->
                    backStack.add(destination)
                })
            }
        } else {
            todayNavGraph(
                key = key,
                onOpenTask = { taskId -> backStack.add(TaskEditorRoute(taskId)) },
                onOpenDashboard = { backStack.add(DashboardRoute) },
                onOpenProjects = { backStack.add(ProjectsRoute) },
            )
            ?: taskNavGraph(
                key = key,
                backStack = backStack,
                snackbarHostState = snackbarHostState,
                onStartFocus = { taskId, durationSeconds ->
                    backStack.add(FocusTimerRoute(taskId = taskId, durationSeconds = durationSeconds))
                },
            )
            ?: dashboardNavGraph(key)
            ?: notesNavGraph(key, backStack)
            ?: focusTimerNavGraph(key)
            ?: calendarNavGraph(key)
            ?: projectsNavGraph(key, onOpenTask = { taskId -> openProjectTask(backStack, taskId) })
            ?: habitsNavGraph(key)
            ?: error("Unknown key: $key")
        }
    }
}

internal fun openProjectTask(backStack: MutableList<NavKey>, taskId: String) {
    backStack.add(TaskEditorRoute(taskId))
}
