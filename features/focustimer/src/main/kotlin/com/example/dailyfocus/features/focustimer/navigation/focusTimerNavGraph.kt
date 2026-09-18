package com.example.dailyfocus.features.focustimer.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.focustimer.ui.screen.FocusTimerScreen
import com.example.dailyfocus.features.focustimer.ui.viewmodel.FocusTimerViewModel

fun focusTimerNavGraph(key: NavKey): NavEntry<NavKey>? = when (key) {
    is FocusTimerRoute -> NavEntry(key) {
        val viewModel: FocusTimerViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        FocusTimerScreen(state, viewModel::onEvent)
    }
    else -> null
}
