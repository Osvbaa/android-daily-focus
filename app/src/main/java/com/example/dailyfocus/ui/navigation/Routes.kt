package com.example.dailyfocus.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

//Ruta 1: La pantalla principal con las tareas
@Serializable
data object TaskRoute : NavKey
//Ruta 2: La pantalla con las estadisticas
@Serializable
object DashboardRoute : NavKey
//Ruta 3: La pantalla de detalles/edición de tareas (sin el ID no compila)
@Serializable
data class TaskDetailRoute(val taskId: String) : NavKey