# Auditoría de módulos

Revisada: 2026-09-17. Decisión base: [ADR-019](../adr/ADR-019-local-ai-and-dormant-modules.md). El estado se refiere al código inspeccionado; no sustituye pruebas pendientes.

| Módulo | Hallazgo | Decisión |
|---|---|---|
| `:sync` | Solo `build.gradle.kts`; ninguna fuente ni consumidor | Retirar del grafo y disco |
| `:integrations:google` | Solo `build.gradle.kts`; ninguna fuente ni consumidor | Retirar |
| `:core:analytics` | Solo `build.gradle.kts`; tracker real/no-op en `:core:common` | Retirar módulo vacío; revisar contrato de métricas aparte |
| `:core:network` | Tres archivos Kotlin sin implementación, uno con comentario placeholder; `:core:data` no importa sus símbolos | Retirar y quitar dependencia de data |
| `:ai` | Motor Gemini Nano, parser y test | Conservar; spec de disponibilidad y confirmación |
| `:app` | Ensambla navegación y DI; tests de rutas | Conservar; revisar tests `Example*` |
| `:core:model`, `:core:data`, `:core:database` | Modelos, repositorios, DAOs/migraciones y tests | Conservar; no tocar schemas por limpieza |
| `:core:common`, `:core:testing` | Reloj/dispatchers/analítica y fakes/guardrails | Conservar; revisar alias y contratos individuales |
| `:core:designsystem`, `:core:ui` | Tema y componentes usados; UI fuera de esta renovación | Conservar y revisar referencias de componentes |
| Ocho `:features:*` | Rutas/ViewModels/pantallas con funciones actuales | Conservar. Calendar/Dashboard se evalúan frente a Today antes de fusionar |

`build-logic` usa application, library, library.compose, compose, feature, room, hilt y jvm. `application.compose` no tenía consumidores: se retiró su registro y clase. `AndroidFeatureConventionPlugin` aplica un conjunto amplio de dependencias a cada feature; su reducción se tratará con `buildHealth` y cambios por módulo para no romper compilación accidentalmente.

Revisión de archivos: se retiraron `ExampleUnitTest` y `ExampleInstrumentedTest` (solo pruebas de plantilla sin comportamiento de producto), el alias `core/common/model/Priority.kt` (ningún import), y `TaskItemCard`/`NoteSnippetCard` de `:core:ui` (ningún consumidor). `PriorityColors.kt` se conserva: Tasks y Today importan `priorityColor`. Los tests de navegación reales de `:app` siguen presentes.

Revisión Gradle raíz: `settings.gradle.kts` tenía los cuatro módulos sin consumidores; se retiraron. `build.gradle.kts` conserva detekt central y el gate `checkQuality`, ahora con pruebas del módulo JVM. `build-logic/settings.gradle.kts` usa un único included build `:convention` y el catálogo raíz; su estructura es válida. `build-logic/convention/build.gradle.kts` registraba un plugin sin aplicación (`application.compose`), retirado. Los plugins restantes se usan directa o indirectamente (por `feature`/`library.compose`); no se eliminan por ausencia de aplicación directa en los módulos.
El gate raíz se corrigió para incluir `:core:model:test`: este módulo JVM puro no crea `testDebugUnitTest`, por lo que sus pruebas estaban fuera de `checkQuality`.

`buildHealth` todavía informa advertencias no fatales de dependencias heredadas en módulos conservados. Se revisarán por módulo y con comprobación de API pública; sus sugerencias de convertir dependencias internas a `api` no se aplican automáticamente porque podrían filtrar tipos de Room fuera de la frontera ADR-012.

Dependencias explícitas revisadas: `:core:model` publica colecciones inmutables y anotaciones de serialización como parte de sus modelos; se cambiaron a `api` y se retiraron coroutines/JSON no usados. `:core:common` dejó de aplicar serialization y de declarar datetime, JSON y core-ktx sin imports. `:core:database` y `:core:data` retiraron declaraciones core-ktx sin uso; `:core:database` tampoco importa `:core:common`. `:core:designsystem` retiró core-ktx e iconos extendidos sin uso. `:core:ui` conserva `:core:model` para `priorityColor`, y retiró design system, common, core-ktx e iconos sin consumidores propios. `checkQuality` pasó después de estos cambios. Los módulos feature aún requieren revisión individual de dependencias inyectadas por la convención.

Features: `AndroidFeatureConventionPlugin` quedó limitado a plugins y bibliotecas Android/Compose compartidas; las dependencias hacia `:core:model`, `:core:data`, `:core:common`, `:core:ui` y `:ai` están ahora declaradas en cada `features/*/build.gradle.kts` según uso. Iconos y colecciones inmutables son explícitos por módulo; el tema de Roborazzi es `testImplementation` solo en Tasks. `:core:common` ya no reexporta `:core:model`. El grafo compila y `checkQuality` pasó. Aún hay recomendaciones no fatales de `buildHealth` para dependencias Compose/Hilt transitivas; la revisión de API pública continúa.

Persistencia: `AppDatabase` está en versión 10 con migraciones declaradas 1→10. Existen schemas exportados 1–5 y 7–10; **falta `6.json`**. Se conservan todos los existentes. `TaskDatabaseMigrationTest` cubre 1→2, 9→10 y 3→10 con BundledSQLiteDriver, pero requiere dispositivo y no se ejecuta en `checkQuality`. `connectedDebugAndroidTest` se intentó y falló por ausencia de dispositivo. No declarar R-05 cerrado hasta recuperar/verificar el schema 6 y ejecutar las pruebas instrumentadas.

Riesgo de negocio detectado al inspeccionar Tareas/Today/Hábitos/Focus: las superficies completan tareas y hábitos mediante repositorios, pero registran el ledger de actividad en ViewModels distintos. `TaskNotificationReceiver` completa una tarea y emite analítica con valores constantes `0/false`, sin registrar actividad. La futura slice de cierre debe centralizar la mutación y su crédito en `:core:data`, respetar la ventana Undo de la UI y probar notificación, Today y Tareas contra el mismo contrato. No se reparó ad hoc dentro del receiver porque duplicaría aún más la regla.

Superficies secundarias inspeccionadas: Calendar está conectado desde More, pero su ViewModel solo consulta el día actual, sin selección de fecha ni avance tras medianoche; no cumple aún una spec de calendario. Dashboard está conectado desde Today y combina tareas con resumen del ledger, por lo que sí aporta una vista de estadísticas distinta. Su estado inicial contiene `progressText = "%f"`, visible potencialmente durante carga. Conservar ambos módulos mientras se decide la intención de Calendar y se prueban las rutas; no eliminarlos por parecido nominal con Today.
El usuario decidió conservar Calendario con selección de fecha. Su ViewModel ahora cambia la consulta al día elegido y restaura esa selección; la pantalla Compose actual permanece sin modificar y todavía usa texto fijo de «hoy». La spec y el contrato UI documentan lo que el propietario debe conectar. Dashboard sigue separado.

Projects: la creación recorta los títulos de hitos antes de validar unicidad. Una prueba de integración Room cubre rechazo sin escrituras de duplicados tras normalización, preservación de tarea/hito al archivar y rechazo de un hito perteneciente a otro proyecto. `checkQuality` pasó después de esta slice.

Habits: una prueba de integración Room confirma que Mini→Plus→Elite en un mismo día deja una sola finalización y un evento idempotente de ocho puntos. La coordinación entre mutación y crédito aún está separada entre repositorios, por lo que ADR-021 sigue propuesto. Dashboard: el estado inicial ya expresa `0%` en lugar del marcador `%f`. `checkQuality` pasó tras ambos cambios.

Today: el estado combinado dejaba `errorMessage` en null en cada emisión y ocultaba los errores locales. Se conserva el mensaje hasta que el evento de edición o una operación exitosa lo limpia. `TodayViewModelTest` y `checkQuality` pasaron. Las cifras de demostración en Compose siguen reservadas al propietario de la UI.

Siguiente nivel de auditoría: para cada archivo del módulo conservado, registrar consumidor, requisito y prueba antes de mover o borrar. Los directorios `domain/data/ui` se crean al aparecer código real; no se generan vacíos. Los recursos y manifiestos se inspeccionan junto al Kotlin, especialmente servicio/receiver y schema Room.
