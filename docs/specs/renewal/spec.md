# Renovación SDD y arquitectura

Estado: En curso. Aprobada por el usuario el 2026-09-17.

## Resultado

El repositorio refleja el producto actual, permite cambios pequeños guiados por specs y compila sin módulos placeholder. Se mantiene el flujo de tareas/notas/IA y también Proyectos, Hábitos y Focus. El usuario conserva la implementación visual de UI.

| ID | Requisito observable | Evidencia de cierre |
|---|---|---|
| REN-01 | Specs separan vigente, propuesto y no verificado | `docs/specs/`, enlaces y estados revisados |
| REN-02 | Gradle incluye solo módulos con consumidores o contratos productivos | `settings.gradle.kts`, búsqueda de referencias, buildHealth |
| REN-03 | Ninguna eliminación pierde datos ni rompe migraciones | schemas y migraciones Room conservados; pruebas de migración |
| REN-04 | Los límites feature→core y core:data→database se mantienen | guardrails y grafo Gradle |
| REN-05 | Cada cambio de negocio avanza Domain→Data→ViewModel→contrato UI→Tests | spec/plan/tasks por capacidad y pruebas trazadas |
| REN-06 | La UI visual existente compila, su renovación queda en contratos | compilación de app y contratos UI |

Calendar y Dashboard solo podrán fusionarse con Today después de especificar equivalencia de casos, rutas, datos y prueba de regresión. Las propuestas de nube/analítica remota no son implementación vigente.
