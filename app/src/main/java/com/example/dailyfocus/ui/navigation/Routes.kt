package com.example.dailyfocus.ui.navigation

import kotlinx.serialization.Serializable

//Ruta 1: La pantalla principal con las tareas
@Serializable
object TaskRoute
//Ruta 2: La pantalla con las estadisticas
@Serializable
object DashboardRoute
//Ruta 3: La pantalla de detalles/edición de tareas (sin el ID no compila)
@Serializable
data class TaskDetailRoute(val taskId: String)