# Contrato UI: Proyectos

El propietario implementa los visuales. Estados: lista vacía, activos, archivados, edición, carga y error. Eventos: crear/editar/archivar, elegir proyecto e hito, abrir tarea relacionada. Navegación con `projectId`/`milestoneId` primitivos. Una tarea archivada o completada no desaparece por archivar el proyecto. El estado debe explicar validaciones de pertenencia hito-proyecto. Probar PROJECT-01 a PROJECT-03 y accesibilidad de progreso.
