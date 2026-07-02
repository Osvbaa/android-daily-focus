package com.example.dailyfocus.features.tasks.ui.detail.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

//Ruta 3: La pantalla de detalles/edición de tareas (sin el ID no compila)
@Serializable
data class TaskDetailRoute(val taskId: String) : NavKey