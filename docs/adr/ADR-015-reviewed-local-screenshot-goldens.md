# ADR-015: Goldens revisados localmente y verificación visual en CI

## Status

Accepted — aprobado explícitamente por el usuario el 2026-09-12.

Fecha: 2026-09-12.

Sustituye únicamente la frase sobre actualización de goldens de
[ADR-011](ADR-011-experimental-compose-task-surfaces.md).

## Context

ADR-011 dice que los goldens se actualizan mediante CI. En cambio `AGENTS.md`
y [la estrategia de pruebas](../testing/README.md) requieren generación explícita
local, revisión y CI de solo verificación. No se puede cerrar regresión visual
con dos procedimientos contradictorios.

## Decision

- Roborazzi continúa siendo la fuente de regresión visual.
- La generación se solicita explícitamente con `recordRoborazziDebug`.
- Se comparan y revisan las imágenes antes de versionarlas. Un cambio visual
  no se acepta solamente porque coincida con un golden recién regenerado.
- CI ejecuta `verifyRoborazziDebug`, nunca generación ni aceptación automática.
- La configuración debe ejecutar tests reales y verificar un inventario de
  goldens no vacío. Una tarea `NO-SOURCE` no demuestra cierre visual.
- No se cambian los opt-ins, tokens o decisión de `SceneStrategy` de ADR-011.

## Consequences

La cláusula de ADR-011 se actualiza enlazando esta decisión.
No añadir otro motor de capturas ni debilitar tolerancias para ocultar diferencias.
La revisión visual y la verificación automatizada son evidencias complementarias.
