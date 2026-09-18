# Proyectos

Estado: Vigente. Prioridad P1. [BDD legado](../../product/features/05_projects.md), [ADR-016](../../adr/ADR-016-features-closure-activity-loop.md).

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| PROJECT-01 | Crear/editar/archivar proyecto mantiene tareas asociadas | Vigente | ProjectRepository, DAO, ADR-016 |
| PROJECT-02 | Una tarea usa cero o un proyecto; un hito pertenece al mismo proyecto | Vigente | `OfflineFirstProjectRepositoryTest` |
| PROJECT-03 | Archivar no elimina tareas | Vigente | `OfflineFirstProjectRepositoryTest` |
| PROJECT-04 | Al crear un proyecto, los títulos de hitos se recortan y deben ser únicos tras ese recorte; si no, no se persiste nada | Vigente | `OfflineFirstProjectRepositoryTest` |

Descomposición de hitos mediante IA es propuesta; requiere spec y prueba propias antes de activarse.
