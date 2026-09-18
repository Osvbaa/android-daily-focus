# Projects - contrato de cierre

> Registro histórico de cierre. Contrato vigente: [spec de Proyectos](../../specs/projects/spec.md).

Estado: implementado localmente como slice de cierre; pendiente validacion conectada en dispositivo.

## Alcance

Un proyecto agrupa tareas sin que Tasks dependa del feature Projects. El MVP permite crear, observar y archivar proyectos, definir hitos y consultar el progreso.

## Dominio y persistencia

- `ProjectId` es fuertemente tipado.
- Cada tarea pertenece a cero o un proyecto mediante `projectId`; no existe una relacion muchos-a-muchos.
- Una tarea puede tener un `milestoneId`, que siempre debe pertenecer al mismo proyecto.
- Las tablas `projects` y `project_milestones`, y las columnas nullable en tareas y borradores, pertenecen a `:core:database`.
- La tarjeta de tarea resuelve el nombre desde `ProjectRepository` y muestra `Proyecto: ...`.

## Reglas

- Un nombre vacio se rechaza.
- Archivar no elimina ni desasigna tareas.
- El primer hito queda `ACTIVE`; los siguientes quedan `LOCKED`.
- Al completar todas las tareas asignadas al hito activo, se marca `COMPLETED` y se activa el siguiente.
- El feature consume `ProjectRepository` desde `:core:data`.
- No se permite dependencia `:features:projects` -> `:features:tasks`.

## Navegacion y UI

La ruta solo contiene primitives. El editor de tareas ofrece seleccion de proyecto e hito y el listado de proyectos muestra tareas y estados de hitos.

## Migraciones

La migración `3->4` es no destructiva y agrega la relación tipada de tareas, proyectos y hitos. La creación de proyecto e hitos se ejecuta en una única transacción. El cierre incorpora además `5->6` para actividad, `6->7` para Focus Timer, `7->8` para estimaciones Focus y `8->9` para recuperación precisa de sesiones.
