# Plan técnico de renovación

Estado: En curso. Spec: [REN](spec.md).

1. **Documentos**: alinear PRD, ADR y topología, crear specs y contratos; conservar documentos históricos como contexto con avisos de vigencia.
2. **Inventario**: por módulo y archivo, buscar consumidores, tests y datos antes de conservar, mover o eliminar. Mantener schemas/migraciones. Ver [inventario](../../architecture/module-audit.md).
3. **Gradle raíz**: retirar solo módulos vacíos o placeholder confirmados; revisar `settings.gradle.kts`, `build.gradle.kts` y dependencias sin uso.
4. **Build logic**: inspeccionar settings, plugin Gradle, convention plugins y referencias reales antes de simplificar.
5. **Features**: priorizar Tareas, Notas, IA; luego Today, Projects, Habits, Focus y superficies secundarias. Resolver cada una como vertical slice con contrato UI, sin implementar visuales.
6. **Verificar**: guardrails, pruebas unitarias, lint, detekt, buildHealth, Roborazzi y pruebas instrumentadas de migración cuando haya dispositivo.

Riesgo mayor: el checkout contiene archivos sin seguimiento de Git. No usar limpieza global ni borrar migraciones. Cada retiro requiere referencias comprobadas y verificación posterior.
