# Mi Día

Estado: Vigente. Prioridad P1.

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| TODAY-01 | Resume tareas pertinentes al día y accesos a captura/ejecución | Vigente | TodayViewModel y rutas |
| TODAY-02 | Los conteos y progreso provienen de datos reales, también en estado vacío | Incumplido en UI actual | `TodayScreen.kt` usa 8/3/2 cuando no hay tareas y muestra rachas de ejemplo |
| TODAY-03 | Navegar a detalle usa ID primitivo y refleja cambios al volver | No verificado | Revisar AppNavigation y pruebas |
| TODAY-04 | Un error de validación o mutación permanece visible hasta que la persona corrige la entrada o una operación exitosa lo resuelve | Vigente | `TodayViewModelTest` cubre validación y corrección de título |

El contrato de esta capacidad debe distinguir Today de Calendar y Dashboard antes de fusionar destinos.
