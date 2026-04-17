package com.example.dailyfocus.data.model

// 1. Definimos un modelo de UI para que la vista reciba todo listo
data class TaskUiItem(
    val task: Task,
    val formattedTime: String
)