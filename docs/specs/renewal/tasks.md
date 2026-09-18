# Tareas de renovación

| ID | Requisito | Entregable | Estado | Evidencia |
|---|---|---|---|---|
| R-01 | REN-01 | Flujo SDD, PRD, índice y ADR | En curso | `docs/sdd/`, `docs/specs/`, ADR-019 |
| R-02 | REN-02 | Auditoría de módulos vacíos/placeholder | En curso | `docs/architecture/module-audit.md` |
| R-03 | REN-02/04 | Ajustar settings y dependencias | Hecho para módulos retirados | `settings.gradle.kts`, `core/data/build.gradle.kts`, compilación y buildHealth |
| R-04 | REN-02/04 | Auditar root build y build-logic | Parcial | plugin sin consumidores retirado; gate JVM; dependencias core explícitas por feature; avisos Compose/Hilt pendientes |
| R-05 | REN-03 | Revisar schema/migraciones y tests | En curso | schema 6 ausente; migraciones 1→9 presentes; prueba instrumentada pendiente de dispositivo |
| R-06 | REN-05 | Slice Tareas | Parcial | TASK-02 política de reprogramación verificada; resto pendiente |
| R-07 | REN-05 | Slice Notas e IA; resolver confirmación sobre nota nueva (NOTE-02) | Parcial | slice NOTE-02/03 y AI-02 verificada; resto de requisitos pendiente |
| R-08 | REN-05 | Revisar Today, Projects, Habits, Focus, Calendar, Dashboard | Parcial | Focus decisión explícita; Projects integridad; Habits mejora de nivel; Calendar selección reactiva; Dashboard estado inicial corregido; Today tiene contadores de demo |
| R-09 | REN-06 | Contratos UI por feature | Hecho como contrato inicial | `ui-contract.md` en las ocho capacidades; validar con UI visual cuando se implemente |
| R-10 | REN-02/03/04/06 | Gates completos | Verificado en el estado actual; repetir al cambiar código | `checkQuality` exitoso, advertencias no fatales de buildHealth |
| R-11 | REN-05 | Unificar finalización y ledger entre superficies | ADR propuesto | ADR-021; requiere decisión y pruebas antes de código |
