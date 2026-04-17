package com.example.dailyfocus

import android.app.Application
import com.example.dailyfocus.data.DailyFocusAppContainer
import com.example.dailyfocus.data.DefaultAppContainer

/**
 * Esta clase es el punto de entrada global de la app.
 * Hereda de Application para que Android sepa que debe mantenerla viva.
 */
class DailyFocusApplication : Application() {

    // El contenedor que guarda nuestras herramientas (repositorios)
    // Usamos 'lateinit' porque lo inicializaremos en el onCreate()
    lateinit var container: DailyFocusAppContainer

    override fun onCreate() {
        super.onCreate()

        // Instanciamos el contenedor una sola vez para toda la vida de la app.
        container = DefaultAppContainer()
    }
}