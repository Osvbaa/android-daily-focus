# Tareas

Estado: Vigente; criterios detallados pendientes de reconciliar con [BDD legado](../../product/features/01_tasks.md) y [cierre](../../product/features/01_tasks_closure.md). Prioridad P0.

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| TASK-01 | Crear, editar, completar y reprogramar una tarea mantiene identidad y estado tras reiniciar | Vigente | TaskRepository, Room, tests de repositorio |
| TASK-02 | Prioridad y fecha se conservan; reprogramación Hoy/Mañana/Fin de semana/Próxima semana usa día civil | Vigente | Task model/DAO y `TaskReschedulePolicyTest` |
| TASK-03 | Borrador y deshacer sobreviven interrupción del proceso | Vigente | ADR-013, tests de repositorio |
| TASK-04 | Recordatorio y acción de notificación pasan por repositorio; permisos denegados no bloquean tareas | Vigente | ADR-010/014, planner y receiver |
| TASK-05 | Tarea originada en nota conserva referencia por ID | Vigente | cross-ref, contrato de repositorio |

Invariantes: ninguna feature accede directamente a DAO; proyecto e hito deben ser coherentes (ADR-016); errores son tipados. Escenarios de fallo, concurrencia y fecha civil requieren plan de revisión antes de cambiar lógica.

Brecha detectada: completar desde notificación no acredita el ledger y reporta analítica con duración/atraso constantes. Es una slice posterior de coordinación en `:core:data`, con pruebas de comportamiento entre superficies.
