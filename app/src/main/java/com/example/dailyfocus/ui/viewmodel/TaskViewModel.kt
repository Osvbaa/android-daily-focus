package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.data.model.DashboardState
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.data.model.Task
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
class TaskViewModel : ViewModel() {
    // La fuente de datos
    private val _tasks = MutableStateFlow(
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
    val tasks = _tasks.asStateFlow()
    // El canal de alertas (SharedFlow para las alertas en el snackbar)
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Calculamos el progreso diario
    val stats: StateFlow<DashboardState> = _tasks.map { currentTasks ->
        val total = currentTasks.size
        if (total == 0) return@map DashboardState(message = "¡Empieza agregando una tarea!")
        val completed = currentTasks.count { it.isCompleted }
        val progress = completed.toFloat() / total
        val pending = total - completed

        val tasksCompleted = currentTasks.count { task ->  task.isCompleted }
        val tasksPending = currentTasks.count { task ->  !task.isCompleted }

        DashboardState(
            stats = persistentListOf(
                StatItem(title = "Total tareas", value = total),
                StatItem(title = "Completadas", value = completed),
                StatItem(title = "Pendientes", value = pending)
            ),
            totalTasks = total,
            tasksCompleted = tasksCompleted,
            tasksPending = tasksPending,
            progress = progress,
            progressText = "${(progress * 100).toInt()}%",
            message = if (completed == total) "¡Todas las tareas completadas! 🎉" else "Casi llegas a tu meta de hoy"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        initialValue = DashboardState(message = "",)
    )

    // Actualizamos el estado de la tarea
    fun toggleTaskCompletion(taskId: String) {
        _tasks.update { currentTask -> // Garantizamos que la transformación se haga sobre la versión más reciente del estado
            currentTask.map { task ->
                if (taskId == task.id) {
                    task.copy(isCompleted = task.isCompleted.not())
                } else { task }
            }.toPersistentList()
        }
    }
    private var lastDeletedTask: Task? = null
    private var lastDeletedIndex: Int = -1
    fun deleteTask(taskId: String) {
        val currentList = _tasks.value
        val index = currentList.indexOfFirst { task -> taskId == task.id }

        if (index != -1) {
            lastDeletedTask = currentList[index]
            lastDeletedIndex = index

            _tasks.update { tasks ->
                tasks.removeAt(index = index)
            }
            
            viewModelScope.launch {
                _uiEvent.emit(value = "Tarea eliminada")
            }
        }
    }
    fun undoDelete() {
        val taskToRestore = lastDeletedTask ?: return
        val indexToRestore = lastDeletedIndex

        if (indexToRestore != -1) {
            _tasks.value = _tasks.value.add(indexToRestore, element = taskToRestore)
        }

        // Restablecer las variables de borrado después de la deshacer
        lastDeletedTask = null
        lastDeletedIndex = -1
    }
}