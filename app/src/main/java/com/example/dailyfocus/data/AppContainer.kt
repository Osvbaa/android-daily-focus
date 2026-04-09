package com.example.dailyfocus.data

import android.app.Application
import com.example.dailyfocus.data.repository.InMemoryTaskRepository
import com.example.dailyfocus.data.repository.TaskRepository

/**
 * 1. La Interfaz del Contenedor:
 * Define qué herramientas están disponibles en toda la app.
 */
interface AppContainer {
    val taskRepository: TaskRepository
}

/**
 * 2. La Implementación del Contenedor:
 * Aquí es donde REALMENTE se crean los objetos (usamos 'new' o el constructor).
 */
class DefaultAppContainer : AppContainer {
    // Usamos 'by lazy' para que el repositorio solo se cree
    // la primera vez que alguien lo necesite. Si nadie entra a la
    // pantalla de tareas, el objeto no gasta memoria RAM.
    override val taskRepository: TaskRepository by lazy {
        InMemoryTaskRepository()
    }
}

