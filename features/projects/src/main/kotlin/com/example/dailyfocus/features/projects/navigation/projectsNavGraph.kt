package com.example.dailyfocus.features.projects.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.projects.ui.screen.ProjectsScreen
import com.example.dailyfocus.features.projects.ui.viewmodel.ProjectListViewModel

fun projectsNavGraph(key: NavKey, onOpenTask: (String) -> Unit): NavEntry<NavKey>? = when (key) {
    ProjectsRoute -> NavEntry(key) {
        val viewModel: ProjectListViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        ProjectsScreen(state, viewModel::onEvent, onOpenTask = onOpenTask)
    }
    else -> null
}
