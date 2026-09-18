# AGENTS.md — DailyFocus

## Spec Driven Development

Antes de cambiar comportamiento, lee [docs/sdd/README.md](docs/sdd/README.md), la spec de la capacidad, los ADR relacionados y la implementación/pruebas existentes. Si falta una spec, créala con criterios observables antes de codificar. Mantén trazabilidad `requisito → tarea → prueba → resultado` y una vertical slice a la vez: **Domain → Data → ViewModel → contrato UI → Tests**; añade persistencia y migración cuando proceda. `domain`, `data` y `ui` son paquetes dentro de cada feature, no submódulos Gradle.

El usuario implementa la UI visual. Durante esta renovación, crea o ajusta solo contratos UI (estados, eventos, navegación, accesibilidad, criterios de prueba); conserva la UI existente compilable. No cambies Compose visual sin nueva instrucción.

## Límites obligatorios

- Ningún `:features:*` depende de otro `:features:*`. Modelos compartidos en `:core:model`, Kotlin/JVM puro sin Android, Compose, Room ni Ktor.
- `:core:database` posee la única `AppDatabase`; solo `:core:data` consume DAOs. Features consumen interfaces de repositorio de `:core:data` (ADR-012). Nunca uses migración destructiva en producción.
- Navegación transporta solo primitivas, primitivas anulables o colecciones de primitivas. `UiState` es inmutable y sus colecciones usan `kotlinx.collections.immutable`. Sigue UDF/MVI del proyecto.
- Prueba comportamiento con fakes manuales. Se prohíben MockK, Mockito y mocking dinámico. No debilites pruebas ni guardrails para hacer pasar código.
- Respeta targetSdk 37, edge-to-edge, predictive back, alineación nativa de 16 KB y restricciones de ciclo de vida/ejecución de Android.
- Reutiliza infraestructura. Antes de añadir dependencias revisa `gradle/libs.versions.toml` y el costo de tamaño, inicio, memoria, seguridad y mantenimiento.
- Si una spec exige cambiar una decisión aceptada, redacta un ADR que la sustituya y resuelve la decisión antes de editar ese límite. No alteres silenciosamente la arquitectura.

## Fuentes y verificación

`docs/specs/` define comportamiento vigente; `docs/adr/` decisiones técnicas; `docs/architecture/` grafo; `docs/testing/` estrategia; `docs/performance/` presupuestos. Los documentos legados de `docs/product/` aportan contexto cuando una spec aún no cubre un caso, pero sus fases históricas no redefinen el alcance actual.

Antes de cerrar cambios de código ejecuta:

```bash
./gradlew testDebugUnitTest
./gradlew :core:model:test
./gradlew :core:testing:testDebugUnitTest --tests "*ArchitectureGuardrailsTest"
./gradlew detekt lintDebug
./gradlew buildHealth
./gradlew :features:tasks:verifyRoborazziDebug
```

`./gradlew checkQuality` agrupa estas comprobaciones. Actualiza goldens solo explícitamente con `recordRoborazziDebug`. Si falla un gate requerido, el cambio no está completo. Para cambios solo documentales comprueba enlaces, rutas y consistencia; registra que los gates de código no se ejecutaron.

Usa Conventional Commits en checkpoints independientes y solo después de los gates pertinentes. Al terminar informa cambios, pruebas, comandos, resultados y ADR pendiente si existe.
