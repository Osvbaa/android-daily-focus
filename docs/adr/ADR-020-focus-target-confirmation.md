# ADR-020: Confirmación explícita del objetivo de Focus

Estado: Aceptado por elección explícita del usuario, 2026-09-17. Aclara y sustituye únicamente la cláusula de finalización de objetivo de [ADR-016](ADR-016-features-closure-activity-loop.md).

## Contexto

ADR-016 indica que al llegar a cero se acredita la sesión y se **solicita** completar tarea/hábito. `FocusTimerService` lo hacía automáticamente. La persona eligió preguntar antes de completar.

## Decisión

- Al terminar, el servicio marca la sesión `COMPLETED` y acredita el pomodoro. No completa tarea ni hábito.
- Un objetivo vinculado queda con decisión durable `PENDING` hasta `CONFIRMED` o `DISMISSED`; una sesión sin objetivo queda `NONE`. Las sesiones históricas migran a `NONE`, porque podrían haber completado su objetivo con el comportamiento anterior.
- `:core:data` coordina la decisión mediante repositorios; la feature solo invoca ese contrato. Si completar el objetivo falla, la decisión sigue pendiente. Reintentos no deben duplicar crédito.
- La UI visual futura ofrece confirmar u omitir y expone el estado pendiente incluso tras reinicio. Esta renovación prepara estado/eventos y contrato UI sin modificar Compose.

## Consecuencias

Se necesita migración Room aditiva y pruebas de repositorio. El pomodoro y la tarea/hábito son eventos distintos. La ausencia de UI renovada deja la solicitud pendiente, sin completar por sorpresa.
