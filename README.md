# DailyFocus

Aplicación Android de productividad local: tareas, notas, proyectos, hábitos y sesiones de foco. La extracción asistida de tareas desde notas usa actualmente Gemini Nano mediante ML Kit Prompt API y requiere confirmación del usuario. El núcleo prioritario es **Tareas → Notas → IA**; Proyectos, Hábitos y Focus siguen activos. No hay sincronización remota productiva.

El proyecto usa Kotlin, Jetpack Compose, Room, Hilt y módulos Gradle por responsabilidad.

El trabajo se guía por [SDD](docs/sdd/README.md): cada cambio de comportamiento requiere spec, plan, tareas y evidencia. El propietario implementa la UI visual; los agentes preparan sus contratos y pruebas. Consulta [ADR](docs/adr/), [topología](docs/architecture/system_topology.md) y [AGENTS.md](AGENTS.md).

Con JDK y Android SDK configurados, ejecuta `./gradlew checkQuality`. Las pruebas instrumentadas requieren dispositivo o emulador.
