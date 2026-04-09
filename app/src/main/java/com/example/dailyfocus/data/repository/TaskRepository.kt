package com.example.dailyfocus.data.repository

import com.example.dailyfocus.data.model.Task
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    // 1. EL FLUJO DE DATOS (LECTURA)
    // Devuelve una tubería constante de tareas.
    // Si algo cambia en la base de datos, este Flow emitirá la nueva lista automáticamente.
    fun getTasksStream(): Flow<PersistentList<Task>>

    // 2. OPERACIONES DE ESCRITURA (ACCIÓN)
    // Usamos 'suspend' porque estas operaciones pueden tardar (escribir en disco o red).
    // Así obligamos a que se ejecuten en una corrutina.
    suspend fun insertTask(task: Task)

    suspend fun deleteTask(taskId: String)

    suspend fun updateTask(task: Task)

    // Para el "Deshacer" (Undo)
    suspend fun restoreTask(index: Int, task: Task)
}