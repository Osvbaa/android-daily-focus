package com.example.dailyfocus.features.today.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.today.ui.screen.TodayScreen
import com.example.dailyfocus.features.today.ui.state.TodayEffect
import com.example.dailyfocus.features.today.ui.viewmodel.TodayViewModel

fun todayNavGraph(
    key: NavKey,
    onOpenTask: (String) -> Unit,
    onOpenDashboard: () -> Unit,
    onOpenProjects: () -> Unit = {},
): NavEntry<NavKey>? = when (key) {
    TodayRoute -> NavEntry(key) {
        val viewModel: TodayViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is TodayEffect.NavigateToTask -> onOpenTask(effect.taskId)
                    TodayEffect.NavigateToDashboard -> onOpenDashboard()
                    TodayEffect.NavigateToProjects -> onOpenProjects()
                }
            }
        }
        TodayScreen(state = state, onEvent = viewModel::onEvent)
    }
    else -> null
}
