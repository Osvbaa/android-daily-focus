# Desarrollo guiado por especificaciones

DailyFocus es un proyecto existente. SDD describe comportamiento **vigente** y separa de él mejoras propuestas; una idea de un PRD antiguo no equivale a código implementado.

## Lectura y fuentes

1. [Alcance](../product/00-prd.md) e [índice de specs](../specs/README.md): problema y comportamiento.
2. `docs/specs/<capacidad>/spec.md`: escenarios e invariantes observables.
3. [ADR](../adr/) y [topología](../architecture/system_topology.md): decisiones y límites aceptados.
4. `plan.md` y `tasks.md` de la capacidad: implementación y evidencia.
5. Código y pruebas: evidencia de lo que funciona ahora, no sustituto de requisitos.

## Flujo

1. **Especificar** problema, resultado, reglas, casos límite y estado (`Vigente`, `Propuesto`, `No verificado`) sin fijar solución técnica.
2. **Aclarar** contradicciones de producto. Si cambia un ADR aceptado, proponer uno que lo sustituya y resolverlo antes de editar ese límite.
3. **Planificar** módulos, contratos, migraciones, riesgos, pruebas y contrato UI, con el menor cambio válido.
4. **Desglosar** tareas con ID de requisito y evidencia. Una slice a la vez: Domain → Data → ViewModel → contrato UI → Tests; persistencia con migración no destructiva si procede.
5. **Implementar y verificar** con fakes manuales e integración donde importe el almacenamiento. Ejecutar `checkQuality` y pruebas de dispositivo aplicables.
6. **Converger**: actualizar spec, plan, tareas y ADR con el resultado real. No declarar `Vigente` un flujo solo descrito.

[AGENTS.md](../../AGENTS.md) contiene las restricciones obligatorias. Las [plantillas](templates/) guían el trabajo sin repetir decisiones ya fijadas en ADR.
