# Hábitos

Estado: Vigente. Prioridad P1. [BDD legado](../../product/features/06_habits.md), [ADR-016](../../adr/ADR-016-features-closure-activity-loop.md).

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| HABIT-01 | La programación admite DAILY, WEEKLY, EVERY_N_DAYS y TIMES_PER_WEEK | Vigente | HabitSchedule, repositorio/tests |
| HABIT-02 | Un día civil puede acreditarse como Mini, Plus o Elite sin duplicar el evento | Vigente; atomicidad entre repositorios pendiente de ADR-021 | `OfflineFirstHabitRepositoryTest` y ledger |
| HABIT-03 | Racha y puntos se calculan de eventos persistentes, no de la pantalla | Vigente | ActivityRepository, modelo/tests |

La relación automática Pomodoro→Hábito descrita en el core loop antiguo queda No verificada hasta contrato y pruebas específicas.
