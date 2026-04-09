package com.example.dailyfocus.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.dailyfocus.DailyFocusApplication

object AppViewModelProvider {
    //Proporciona una Factory para crear instancias de ViewModels en toda la app.
    val Factory = viewModelFactory {
        // El inicializador sabe cómo construir el TaskViewModel
        initializer {
            // 1. Extraemos la instancia de la aplicación desde las entrañas de Android
            val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as DailyFocusApplication)

            // 2. Obtenemos el repositorio que vive en el AppContainer
            val repository = application.container.taskRepository

            // 3. ¡Inyectamos! Creamos el ViewModel pasándole su herramienta
            TaskViewModel(repository = repository)
        }
    }
}