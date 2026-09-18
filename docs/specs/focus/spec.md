# Focus

Estado: Vigente. Prioridad P1. [ADR-016](../../adr/ADR-016-features-closure-activity-loop.md).

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| FOCUS-01 | Sesión conserva objetivo por ID y tiempo al cambiar ciclo de vida | Vigente | FocusSessionRepository, service, Room |
| FOCUS-02 | Solo llegar a cero acredita pomodoro; terminar manualmente cancela | Vigente | ADR-016, ViewModel/service a probar |
| FOCUS-03 | Completar tarea/hábito tras sesión requiere acción explícita; decisión pendiente sobrevive reinicio | Lógica implementada; UI y migración en dispositivo pendientes | ADR-020, repositorio de decisión, schema 10 y pruebas con fakes |

Overtime adaptativo y otras extensiones antiguas son propuestas. El servicio debe respetar restricciones de ejecución en segundo plano.
