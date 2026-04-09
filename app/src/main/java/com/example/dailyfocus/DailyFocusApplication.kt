package com.example.dailyfocus

import android.app.Application
import com.example.dailyfocus.data.AppContainer
import com.example.dailyfocus.data.DefaultAppContainer

/**
 * Esta clase es el punto de entrada global de la app.
 * Hereda de Application para que Android sepa que debe mantenerla viva.
 */
class DailyFocusApplication : Application() {

    // El contenedor que guarda nuestras herramientas (repositorios)
    // Usamos 'lateinit' porque lo inicializaremos en el onCreate()
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()

        // ¡Aquí ocurre la magia!
        // Instanciamos el contenedor una sola vez para toda la vida de la app.
        container = DefaultAppContainer()
    }
}