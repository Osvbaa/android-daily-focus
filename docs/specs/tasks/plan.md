# Plan: política de reprogramación

Estado: Verificado localmente. Requisito [TASK-02](spec.md).

La conversión de presets a fechas civiles estaba dentro de `TaskListViewModel`. Se movió a `features/tasks/domain/TaskReschedulePolicy.kt`, función pura con `LocalDate` inyectada desde `DateProvider` por el ViewModel. Repositorio, Room y UI no cambian. `THIS_WEEKEND` mantiene sábado/domingo actual; `NEXT_WEEK` significa lunes de la semana siguiente incluso si hoy es lunes. Las pruebas fijan ambos límites. `TaskReschedulePolicyTest` y `checkQuality` terminaron correctamente.
