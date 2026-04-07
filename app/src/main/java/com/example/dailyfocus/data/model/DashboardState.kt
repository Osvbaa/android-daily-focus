package com.example.dailyfocus.data.model

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class DashboardState(
    val stats: PersistentList<StatItem> = persistentListOf(),
    val totalTasks: Int = 0,
    val tasksCompleted: Int = 0,
    val tasksPending: Int = 0,
    val progress: Float = 0f,
    val progressText: String = "%f",
    val message: String
)