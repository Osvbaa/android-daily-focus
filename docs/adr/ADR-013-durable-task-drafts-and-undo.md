# ADR-013: Borradores duraderos y Deshacer independiente en Tasks

## Status

Accepted — aprobado explícitamente por el usuario el 2026-09-12.

Fecha: 2026-09-12.

Requiere [ADR-012](ADR-012-repository-persistence-boundary.md). Amplía
[ADR-001](ADR-001-mvi-udf-state-reducer.md) y el
[contrato de analítica](../analytics/analytics_contract.md), sin sustituir UDF.

## Context

El usuario eligió conservar borradores hasta guardar o descartar y ofrecer un
Deshacer independiente por tarea. Un `SavedStateHandle` ligado a una entrada
no cubre volver a abrir el editor después de eliminar esa entrada. Un snapshot
y un job únicos tampoco representan varias finalizaciones simultáneas.

El contrato actual lee antes de completar y restaura subtareas por separado;
guardar puede sobrescribir cambios externos o recrear un ID eliminado. El cierre
necesita transacciones y detección de conflictos, no solo nuevos estados visuales.

## Decision

### Borradores

- Guardar borradores en tablas de la `AppDatabase` existente, separados de tareas
  publicadas. Un borrador de creación compartido por las entradas al editor y
  uno por tarea editada. Captura rápida conserva su buffer separado.
- Cada borrador contiene su identidad, versión del formato, contenido completo
  ordenado, revisión de edición y revisión base del agregado, si existe.
  Las subtareas de borrador tienen ID estable y no necesitan un padre ficticio.
- Persistir ediciones mediante escrituras serializadas fuera del hilo principal.
  El estado distingue pendiente de persistir, persistido y error. No prometer
  durabilidad de pulsaciones aún no confirmadas por almacenamiento.
- Volver, cambiar de sección o seleccionar otra tarea espera a persistir el
  último contenido. Un fallo mantiene abierto el editor con reintento.
- `SavedStateHandle` complementa la recuperación de la sesión; nunca sustituye
  la persistencia durable. Una carga atrasada no reemplaza una revisión más nueva.
- Guardar publica tarea y subtareas y elimina su borrador en la misma transacción.
  Descartar elimina únicamente el borrador, con confirmación explícita.
- Si la tarea desapareció, conservar el borrador y mostrar inexistencia; no
  recrear automáticamente el ID. Si cambió desde la revisión base, devolver
  conflicto. Recargar requiere confirmar la pérdida de los cambios locales.

### Mutaciones reversibles

- Completar lee el agregado, valida pendientes, captura los valores anteriores,
  cambia los estados y registra la operación en una única transacción.
- Cada operación tiene ID único, tarea, valores afectados antes/después,
  versión de completitud esperada, vencimiento y metadatos analíticos sin texto.
- Completar B no modifica la operación ni el vencimiento de A. Duplicar la misma
  petición no produce una segunda finalización ni otro evento de analítica.
- Deshacer recibe el ID de operación, no un snapshot arbitrario de la UI.
  Valida existencia, vigencia y compatibilidad y restaura todo atómicamente.
- Cambios de texto o fecha no se revierten al deshacer. Cambios posteriores de
  completitud o estructura invalidan la restauración incompatible, incluso si
  los booleanos finalmente coinciden de nuevo. Una operación nunca resucita
  registros eliminados.
- La ventana base es 4.000 ms desde la confirmación de la mutación. El adaptador
  Android obtiene la ampliación de accesibilidad y comunica la duración efectiva;
  UI, repositorio y analítica comparten el mismo vencimiento.
- Durante el mismo arranque del dispositivo se utiliza tiempo monotónico,
  conservando referencia al arranque para restaurar tras muerte del proceso.
  Después de reiniciar el dispositivo se consideran vencidas las ventanas
  anteriores; cambiar el reloj civil no prolonga Deshacer.

### Estado, efectos y analítica

- Operaciones pendientes y estado del borrador forman parte de estado observable
  durable. Navegación, foco y hápticos siguen siendo efectos transitorios de
  ADR-001. Mostrar un aviso nunca bloquea la recepción de navegación.
- Una operación usa un aviso; varias usan una bandeja con acciones individuales.
  No se encolan avisos que aparezcan después de su vencimiento.
- Al vencer sin reversión, la operación queda elegible para `TaskCompleted`.
  Un despachador recuperable procesa los registros pendientes sin depender de
  la vida del ViewModel. No se garantiza ejecución de background a los 4.000 ms:
  se garantiza que el evento no se despacha antes ni tras Deshacer exitoso.
- Deshacer y reclamación para analítica se excluyen transaccionalmente. La
  recuperación no vuelve a ofrecer operaciones vencidas. Los registros
  terminales se eliminan después de gestionar su resultado; no son un historial.
- No se promete entrega exactamente una vez al SDK externo. Un fallo entre
  envío y confirmación local puede requerir deduplicación externa por operación;
  nunca se incluirán títulos, descripciones ni cuerpos de notas.

## Consequences

Requiere ampliar el esquema 2 mediante una migración exportada y verificada,
manteniendo la ruta 1→2→nuevo esquema. No se introduce otra base ni migración
destructiva. Es un coordinador de operaciones de Tasks, no un sistema genérico
de eventos o sincronización remota.

Los contratos expondrán borradores y operaciones por ID, resultados tipados y
revisiones. El editor y la lista usarán reducers puros para sus transiciones.
Los fakes implementarán estas mismas garantías, con reloj, IDs, latencia y
fallos controlables.

Verificar rollback, doble guardado, A/B con ventanas solapadas, conflicto de
completitud, eliminación durante edición, fallo de Deshacer, cambio del reloj,
reinicio y restauración serializada real. Los tests no reutilizarán el mismo
`SavedStateHandle` para simular muerte del proceso.
