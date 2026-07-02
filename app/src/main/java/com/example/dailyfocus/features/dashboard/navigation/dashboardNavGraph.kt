package com.example.dailyfocus.features.dashboard.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.dashboard.ui.screen.DashboardMainScreen
import com.example.dailyfocus.features.dashboard.ui.viewmodel.DashboardViewModel
import com.example.dailyfocus.app.ui.AppViewModelProvider

fun dashboardNavGraph(key: NavKey): NavEntry<NavKey> = when(key) {
    is DashboardRoute ->
        NavEntry(key) {
            val viewModel: DashboardViewModel =
                viewModel(factory = AppViewModelProvider.Factory)
            val dashboardStats by viewModel.stats.collectAsStateWithLifecycle()
            DashboardMainScreen(stats = dashboardStats)
        }
    else -> error("Unknown key $key")
}