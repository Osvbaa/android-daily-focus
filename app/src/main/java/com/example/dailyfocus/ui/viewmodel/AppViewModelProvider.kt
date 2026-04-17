package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.dailyfocus.DailyFocusApplication

object AppViewModelProvider {
    //Proporciona una Factory para crear instancias de ViewModels en toda la app.
    val Factory = viewModelFactory {
        // El inicializador sabe cómo construir el TaskViewModel
        // ¡Inyectamos! Creamos el ViewModel pasándole su herramienta
        initializer {
            TaskViewModel(repository = dailyFocusApplication().container.taskRepository)
        }

        initializer {
            DashboardViewModel(repository = dailyFocusApplication().container.taskRepository)
        }
    }
        fun factoryForTaskDetail(taskId: String) = viewModelFactory {
            initializer {
                TaskDetailViewModel(
                    repository = dailyFocusApplication().container.taskRepository,
                    taskId = taskId
                )
            }
        }
}

fun CreationExtras.dailyFocusApplication(): DailyFocusApplication =
    // 1. Extraemos la instancia de la aplicación desde las entrañas de Android
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DailyFocusApplication)