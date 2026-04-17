package com.example.dailyfocus.data.repository

import com.example.dailyfocus.data.model.Task
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository : TaskRepository {
    // A. LA FUENTE DE DATOS (Privada)
    private val tasks = MutableStateFlow(
        value = persistentListOf(
            Task(title = "Aprender LazyLayouts", description = "Entender keys"),
            Task(title = "Refactorizar Daily Focus", isCompleted = true),
            Task(title = "Entender Inmutabilidad", description = "Usar @Immutable"),
            Task(title = "Aprender Inglés", description = "Practicar nuevo vocabulario"),
            Task(title = "Practicar speaking", description = "Hablar por 5 minutos en inglés"),
            Task(title = "Aprender a utilizar LazyLists"),
            Task(title = "Hacer tarea", isCompleted = true),
            Task(title = "Estudiar Android", description = "Practicar conceptos nuevos"),
            Task(title = "Leer", description = "Leer libros por 5 minutos en inglés"),
            Task(title = "Trabajar sobre Daily Focus"),
            Task(title = "Limpiar el cuarto", isCompleted = true)
        )
    )
    // B. FUNCIONES DE LECTURA (Implementando la interfaz)
    override fun getTasksStream() = tasks.asStateFlow()

    // C. FUNCIONES DE ESCRITURA
    override suspend fun insertTask(task: Task) {
        tasks.update { list -> list.add(task) }
    }

    override suspend fun deleteTask(taskId: String) {
        tasks.update { list ->
            list.removeAll { task -> task.id == taskId }
        }
    }

    override suspend fun updateTask(task: Task) {
        tasks.update { list ->
            list.map { if (it.id == task.id) task else it }.toPersistentList()
        }
    }

    override suspend fun restoreTask(index: Int, task: Task) {
        tasks.update { list ->
            list.add(index, element = task)
        }
    }
}