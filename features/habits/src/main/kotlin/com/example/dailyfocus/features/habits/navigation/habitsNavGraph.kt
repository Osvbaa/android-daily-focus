package com.example.dailyfocus.features.habits.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.habits.ui.screen.HabitsScreen
import com.example.dailyfocus.features.habits.ui.viewmodel.HabitListViewModel

fun habitsNavGraph(key: NavKey): NavEntry<NavKey>? = when (key) {
    HabitsRoute -> NavEntry(key) {
        val viewModel: HabitListViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        HabitsScreen(state, viewModel::onEvent)
    }
    else -> null
}
