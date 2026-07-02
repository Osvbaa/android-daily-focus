package com.example.dailyfocus.features.tasks.ui.state

import com.example.dailyfocus.features.tasks.domain.model.Task

// 1. Definimos un modelo de UI para que la vista reciba todo listo
data class TaskUiState(
    val task: Task,
    val formattedTime: String
)