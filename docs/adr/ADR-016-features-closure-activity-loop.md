# ADR-016: Cierre del loop local de Features

Estado: Aceptado para el MVP local, 2026-09-15.

## Decision

- Projects y Habits viven detras de repositorios en `:core:data`; ninguna feature importa otra feature ni `:core:database`.
- Una tarea pertenece a cero o un proyecto mediante `projectId` nullable. Si usa hito, el hito debe pertenecer al mismo proyecto. Archivar no elimina tareas.
- Habits soporta `DAILY`, `WEEKLY`, `EVERY_N_DAYS` y `TIMES_PER_WEEK`. Las fechas son dias civiles y los niveles son Mini/Plus/Elite.
- `activity_events` es un ledger local idempotente. Todos los eventos alimentan una racha por dias unicos: tarea, tarea de proyecto, hito, nivel de habito y pomodoro.
- El ledger aplica los topes de puntos v1: 5 por primera tarea del dia, 10 por primer hito, 3/5/8 por nivel de habito, 1 por pomodoro hasta cuatro y 25 globales por dia.
- Focus Timer persiste `focus_sessions` y usa un Foreground Service para mantener el tiempo en segundo plano. Su objetivo se captura como ID primitivo; solo al llegar a cero acredita el pomodoro y solicita completar la tarea/habito mediante su repositorio. Finalizar manualmente cancela la sesión.
- El esquema evoluciona sin destrucción: `3->4` proyectos/tareas, `4->5` hábitos, `5->6` actividad, `6->7` sesiones de Focus Timer, `7->8` estimación Focus de tareas y `8->9` fecha límite de pared de la sesión. Las migraciones implementan tanto `SupportSQLiteDatabase` como `SQLiteConnection`, porque producción usa `BundledSQLiteDriver`.

## Consecuencias

La racha y los puntos no dependen de una pantalla concreta y sobreviven a la recreacion del proceso. El ledger es local y no es autoridad para recompensas economicas futuras; cualquier descuento o beneficio premium requerira backend, autenticacion y sincronizacion explicita.

La politica de puntos es deliberadamente simple para el MVP y puede cambiarse con `rulesVersion` sin reinterpretar eventos historicos.
