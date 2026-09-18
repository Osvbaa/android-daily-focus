# Índice de capacidades

Estado revisado: 2026-09-17. `Vigente` significa código productivo localizado; **no** significa que todos los criterios del documento estén probados. Cada spec marca sus mejoras propuestas y evidencia pendiente. Los contratos visuales son para el propietario de la UI.

| Prioridad | Capacidad | Módulo | Estado | Fuente de detalle legado |
|---|---|---|---|---|
| P0 | [Tareas](tasks/spec.md) | `:features:tasks` | Vigente, revisión de negocio pendiente | [Tareas](../product/features/01_tasks.md), [cierre](../product/features/01_tasks_closure.md) |
| P0 | [Notas](notes/spec.md) | `:features:notes` | Vigente, revisión de negocio pendiente | [Notas](../product/features/02_notes.md) |
| P0 | [Extracción IA](ai/spec.md) | `:ai` + Notas | Vigente con disponibilidad condicional | [IA](../product/features/03_ai_extraction.md) |
| P1 | [Mi Día](today/spec.md) | `:features:today` | Vigente | [Core loop](../product/01-core-loop.md) |
| P1 | [Proyectos](projects/spec.md) | `:features:projects` | Vigente | [Proyectos](../product/features/05_projects.md) |
| P1 | [Hábitos](habits/spec.md) | `:features:habits` | Vigente | [Hábitos](../product/features/06_habits.md) |
| P1 | [Focus](focus/spec.md) | `:features:focustimer` | Vigente | [ADR-016](../adr/ADR-016-features-closure-activity-loop.md) |
| P2 | [Calendario y Dashboard](secondary-surfaces/spec.md) | `:features:calendar`, `:features:dashboard` | Vigente; solapamiento pendiente de decidir | [Topología](../architecture/system_topology.md) |

El [plan de renovación](renewal/plan.md) y sus [tareas](renewal/tasks.md) registran la auditoría de módulos, Gradle y archivos. Las plantillas SDD están en [`docs/sdd/templates/`](../sdd/templates/).
