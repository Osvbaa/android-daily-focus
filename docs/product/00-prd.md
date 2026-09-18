# DailyFocus: alcance de producto

Estado: vigente para la renovación SDD, 2026-09-17. Las [specs](../specs/README.md) concretan cada capacidad.

## Problema y propuesta

Capturar y mantener tareas, notas y compromisos suele exigir demasiados pasos. Las ideas escritas quedan sin convertir en acciones, los sistemas rígidos de hábitos penalizan días difíciles y una lista de pendientes no ayuda a empezar una sesión de trabajo. DailyFocus une captura, organización y ejecución en un flujo local rápido.

## Prioridad

El núcleo prioritario es **Tareas → Notas → sugerencias de IA confirmadas por la persona**. Mi Día organiza la ejecución diaria. **Proyectos, Hábitos y Focus permanecen como capacidades activas**; su mantenimiento no se posterga por la antigua clasificación MoSCoW. Calendario y Dashboard necesitan evaluación de solapamiento con Mi Día antes de cambiar sus rutas o datos.

La persistencia de la versión actual es local con Room. La IA productiva usa Gemini Nano con ML Kit Prompt API y debe degradarse sin pérdida de datos cuando no esté disponible. La sincronización remota, cloud fallback, LiteRT/Koog, widgets, Wear, voz y colaboración son propuestas futuras que requieren spec, ADR y evidencia antes de entrar al grafo productivo.

## Hipótesis, aún sin validación de producto

Una captura rápida y la conversión confirmada de notas en tareas pueden mejorar constancia. Las antiguas metas de 35% de tareas originadas en IA, D30 >28% y captura <4 s son **hipótesis**, no gates instrumentados ni resultados alcanzados. No se deben presentar como métricas medidas mientras el tracker sea local/no-op y no exista diseño de medición de cohortes y privacidad.

## Principios

- Las sugerencias de IA nunca crean tareas sin confirmación.
- Las tareas vinculadas conservan referencia a su nota de origen.
- La falta de IA, red o permiso de notificación no bloquea la captura manual.
- Las fechas de hábitos se interpretan como días civiles; Focus y acciones de tareas sobreviven al ciclo de vida conforme a ADR-013/014/016.
- Las relaciones entre features pasan por modelos/repositorios compartidos e IDs primitivos, no dependencias Gradle entre features.

[Core loop histórico](01-core-loop.md) y [risk register](risk_register.md) ofrecen contexto; las fases y promesas que contradigan este alcance quedan sustituidas por este documento y las specs vigentes.
