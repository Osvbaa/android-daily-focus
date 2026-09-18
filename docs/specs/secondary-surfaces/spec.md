# Calendario y Dashboard

Estado: Vigentes; propósito y solapamiento No verificados. Prioridad P2.

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| SURFACE-01 | Calendario muestra agenda de fecha seleccionada sin perder tareas | Lógica implementada; UI pendiente | `CalendarViewModelTest`: selección, consulta reactiva y restauración |
| SURFACE-02 | Dashboard muestra actividad real, sin contadores inventados | Lógica vigente, UI pendiente | `DashboardViewModel` combina tareas del día y ledger; estado inicial usa `0%` |
| SURFACE-03 | Retirar o fusionar una ruta conserva navegación y datos visibles | Propuesto | Requiere pruebas de rutas y aceptación de producto |

Decisión de producto: Calendario permanece separado y permite seleccionar fecha; Mi Día sigue siendo el resumen del día actual. Dashboard permanece como vista de estadísticas. La pantalla visual de Calendario todavía dice «hoy» y debe adaptarse por su propietario; no se modificó Compose en esta slice.
