# Testing y quality gates

El contrato local recomendado es `./gradlew checkQuality`.

```bash
./gradlew testDebugUnitTest
./gradlew :core:testing:testDebugUnitTest --tests "*ArchitectureGuardrailsTest"
./gradlew detekt
./gradlew buildHealth
./gradlew lintDebug
./gradlew :features:tasks:recordRoborazziDebug
./gradlew :features:tasks:compareRoborazziDebug
./gradlew :features:tasks:verifyRoborazziDebug
./gradlew connectedDebugAndroidTest
```

`testDebugUnitTest` ejecuta las pruebas JVM de todos los módulos Android. La
prueba explícita de `:core:testing` ejecuta los guardrails Konsist de arquitectura.
`connectedDebugAndroidTest` usa un dispositivo conectado; CI usa el dispositivo
administrado `pixel2api35` mediante `:app:pixel2api35DebugAndroidTest`.

Detekt genera HTML, XML y SARIF en `build/reports/detekt/`. Lint genera sus
reportes en `<módulo>/build/reports/lint-results-debug.*`. Dependency analysis
escribe en `build/reports/dependency-analysis/`. Las pruebas dejan resultados en
`<módulo>/build/test-results/` y Roborazzi en `features/tasks/build/reports/roborazzi/`
y `features/tasks/src/test/screenshots/`.

Los goldens se actualizan localmente con `recordRoborazziDebug`, se revisan con
`compareRoborazziDebug` y se versionan sólo después de revisar el diff. CI ejecuta
únicamente `verifyRoborazziDebug` y nunca activa `roborazzi.test.record`.

Las nuevas violaciones de detekt, lint o arquitectura hacen fallar el gate.
Dependency analysis está en adopción: `buildHealth` publica advertencias para el
inventario heredado en `build/reports/dependency-analysis/`; cada excepción debe
justificarse y migrarse antes de volver a `severity("fail")`. No se deben ocultar
errores de detekt mediante `ignoreFailures`.

Requisitos locales: JDK 17, Android SDK API 37 y build tools 37.0.0. Las pruebas
instrumentadas reproducibles usan la imagen AOSP API 35 del dispositivo
administrado configurado en `app/build.gradle.kts`.
