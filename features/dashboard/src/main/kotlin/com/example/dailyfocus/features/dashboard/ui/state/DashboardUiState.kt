package com.example.dailyfocus.features.dashboard.ui.state

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

// data class con el estado de la pantalla de estadisticas
data class DashboardUiState(
    val stats: PersistentList<StatUiState> = persistentListOf(),
    val totalTasks: Int = 0,
    val tasksCompleted: Int = 0,
    val tasksPending: Int = 0,
    val progress: Float = 0f,
    val progressText: String = "0%",
    val message: String = "",
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val totalPoints: Int = 0,
    val todayPoints: Int = 0,
)
