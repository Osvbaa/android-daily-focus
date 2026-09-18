package com.example.dailyfocus.app

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.dailyfocus.features.focustimer.navigation.FocusTimerRoute
import com.example.dailyfocus.features.habits.navigation.HabitsRoute
import com.example.dailyfocus.features.notes.navigation.NoteEditorRoute
import com.example.dailyfocus.features.notes.navigation.NoteListRoute
import com.example.dailyfocus.features.tasks.navigation.TaskEditorRoute
import com.example.dailyfocus.features.tasks.navigation.TaskRoute
import com.example.dailyfocus.features.today.navigation.TodayRoute

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun MainAppStructure(initialTaskId: String? = null) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack = rememberNavBackStack(TodayRoute)
    LaunchedEffect(initialTaskId) {
        if (initialTaskId != null) {
            backStack.clear()
            backStack.add(TodayRoute)
            backStack.add(TaskEditorRoute(initialTaskId))
        }
    }
    val destination = backStack.lastOrNull()
    val showNavigation = destination == TodayRoute || destination == TaskRoute ||
        destination == NoteListRoute || destination is FocusTimerRoute ||
        destination == HabitsRoute || destination == MoreRoute

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            AnimatedVisibility(visible = showNavigation) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.94f),
                        tonalElevation = 6.dp,
                        shadowElevation = 10.dp,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                    ) {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Destination("Hoy", Icons.Default.Home, destination == TodayRoute) { selectRoot(backStack, TodayRoute) }
                            Destination("Pomodoro", Icons.Default.Timer, destination is FocusTimerRoute) { selectRoot(backStack, FocusTimerRoute()) }
                            Destination("Tareas", Icons.Default.CheckCircle, destination == TaskRoute) { selectRoot(backStack, TaskRoute) }
                            Destination("Notas", Icons.Default.Note, destination == NoteListRoute) { selectRoot(backStack, NoteListRoute) }
                            Destination("Hábitos", Icons.Default.Repeat, destination == HabitsRoute) { selectRoot(backStack, HabitsRoute) }
                            FilledTonalIconButton(onClick = { backStack.add(NoteEditorRoute()) }) {
                                Icon(Icons.Default.Add, contentDescription = "Capturar nota")
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        Box(Modifier.fillMaxSize().padding(contentPadding)) {
            AppNavigation(backStack = backStack, snackbarHostState = snackbarHostState)
        }
    }
}

@Composable
private fun Destination(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = MaterialTheme.colorScheme
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) { if (selected) bringIntoViewRequester.bringIntoView() }
    val container by animateColorAsState(
        if (selected) color.primaryContainer else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "Fondo $label",
    )
    val contentColor by animateColorAsState(
        if (selected) color.onPrimaryContainer else color.onSurfaceVariant,
        label = "Color texto $label",
    )
    Surface(
        onClick = onClick,
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        shape = RoundedCornerShape(24.dp),
        color = container,
    ) {
        Row(
            Modifier
                .animateContentSize(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = label, tint = contentColor)
            AnimatedVisibility(selected) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                )
            }
        }
    }
}

internal fun selectRoot(backStack: MutableList<NavKey>, root: NavKey) {
    if (backStack.lastOrNull() == root) return
    backStack.remove(root)
    backStack.add(root)
}
