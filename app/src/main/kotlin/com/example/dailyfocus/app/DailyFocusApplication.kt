package com.example.dailyfocus.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Punto de entrada global de la app para inicialización y Hilt DI.
 */
@HiltAndroidApp
class DailyFocusApplication : Application()