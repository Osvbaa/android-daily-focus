package com.example.dailyfocus.features.dashboard.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.dashboard.ui.screen.DashboardMainScreen
import com.example.dailyfocus.features.dashboard.ui.viewmodel.DashboardViewModel

fun dashboardNavGraph(key: NavKey): NavEntry<NavKey>? = when(key) {
    is DashboardRoute ->
        NavEntry(key) {
            val viewModel: DashboardViewModel = hiltViewModel()
            val dashboardStats by viewModel.uiState.collectAsStateWithLifecycle()
            DashboardMainScreen(stats = dashboardStats)
        }
    else -> null
}
