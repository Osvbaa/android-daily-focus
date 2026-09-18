# Cierre de Tasks: ejecución y contrato objetivo

> Registro histórico de cierre. Nuevos cambios se guían por [spec de Tareas](../../specs/tasks/spec.md), plan y pruebas actuales.

Fecha: 2026-09-14.

Estado: slice local implementado y verificado; ADR-012 a ADR-015 aceptados el 2026-09-12.
Las evidencias de dispositivo/emulador y rendimiento siguen separadas de los gates locales.

## Alcance elegido

Complementa [el BDD de Tasks](01_tasks.md): CRUD, subtareas de un nivel, gestos,
Mi Día, Deshacer individual, borradores duraderos, recordatorio diario opcional
y consulta de la nota origen. No incluye editor/conversión de Notas, IA,
Pomodoro, widgets ni hora de aviso por tarea. No se habilitan destinos ficticios.

## Hitos y dependencias

| Hito | Entregable | Estado y condición de salida |
| --- | --- | --- |
| 1 | Contratos, decisiones y trazabilidad | ADRs aceptados, contrato objetivo definido, documentación y nombres de lista/editor consolidados; APIs duraderas se materializan en H2–H4 |
| 2 | Persistencia atómica, borradores, operaciones y conflictos | Implementado: Room v3, migraciones 1→2→3, revisiones optimistas, borradores y Undo durables; contrato Fake/Room en verde |
| 3 | Editor, navegación y restauración | Implementado: rutas primitivas, ViewModel por entrada, restauración de borrador y resolución de conflictos |
| 4 | Deshacer individual y analítica | Implementado: operación por ID, ventana monotónica, recuperación tras reinicio y analítica diferida |
| 5 | Mi Día, nota origen y recordatorio | Implementado: consulta por fecha, nota solo lectura, scheduler one-shot y acciones de notificación seguras |
| 6 | UI Expressive, accesibilidad y gestos | Implementado localmente: Compose adaptativo, gesto contextual, semántica y goldens revisados de lista/editor |
| Cierre | Gates, instrumentación y rendimiento | Gates locales en verde; quedan pruebas conectadas y medición de rendimiento en dispositivo para cierre operativo |

No se empieza otra feature para completar estas conexiones. Compartir reglas
mediante repositorios y componentes de presentación, nunca importando Tasks desde
Dashboard. Las decisiones de persistencia están en los ADR, no duplicadas aquí:

- [ADR-012: fronteras y stack](../../adr/ADR-012-repository-persistence-boundary.md).
- [ADR-013: borradores y Deshacer](../../adr/ADR-013-durable-task-drafts-and-undo.md).
- [ADR-014: recordatorio diario](../../adr/ADR-014-task-daily-reminder.md).
- [ADR-015: goldens revisados](../../adr/ADR-015-reviewed-local-screenshot-goldens.md).

## Contrato UDF objetivo

Mantener `StateFlow<UiState>`, `onEvent` y efectos transitorios según ADR-001.
Toda colección de estado es inmutable. Las transiciones complejas se calculan
en reducers puros; I/O y relojes se inyectan y ejecutan fuera del reducer.

| Superficie | Estado observable | Intenciones y efectos |
| --- | --- | --- |
| Lista | Carga/contenido/error, filtro, captura, envío activo, operaciones vigentes, resolución de subtareas, confirmación de eliminación | Capturar, abrir, completar, reabrir, resolver/cancelar, reprogramar, eliminar, Deshacer por ID, reintentar; navegación, foco y hápticos transitorios |
| Editor | Identidad, fase Loading/Editing/Saving/LoadError/Missing, borrador, base/revisión, persistencia del borrador, dirty derivado, errores de campo, conflicto, confirmación de descarte | Cambiar campos, operar subtareas por ID, guardar, volver conservando, reintentar, solicitar/confirmar descarte, recargar conflicto con confirmación |
| Nota origen | Carga, contenido de solo lectura, inexistencia, error | Abrir por ID y volver; no editar ni convertir texto |
| Recordatorio | Habilitado, hora local, permiso/canal, persistencia de ajustes | Habilitar/deshabilitar, cambiar hora, solicitar permiso y abrir ajustes desde acción del usuario |

Los nombres Kotlin canónicos son `TaskList*` y `TaskEditor*`. La ruta renombrada
conserva su nombre serializado anterior para no romper estado guardado; no hay
alias Kotlin de Detail. Cada entrada de navegación tendrá identidad y ViewModel propios;
sus argumentos se pasarán explícitamente a la fábrica. Solo IDs en el back stack.

Reglas del editor:

- No editar ni guardar durante carga; doble Save no genera trabajo adicional.
- Durante guardado se bloquean cambios y nuevas salidas hasta conocer el resultado.
- Un fallo preserva contenido y vuelve a permitir reintento. `isDirty` compara
  contenido; eventos sin cambios no lo activan.
- Las ediciones de subtareas se refieren a IDs estables, incluso después de mover.
- Una carga que ya no corresponde a la entrada o revisión actual se ignora.
- Al volver se conserva el borrador; descartar no elimina la tarea publicada.

## Matriz de aceptación pendiente

Los identificadores siguientes nombran escenarios de aceptación, no tests ya
existentes. Registrar el nombre real del test y su resultado al implementarlo.

| ID | Dado / Cuando | Resultado exigido | Verificación |
| --- | --- | --- | --- |
| TASK-01 | Captura vacía, espacios o doble confirmación | Sin inserción vacía/duplicada; foco conservado | Fake + VM + Compose |
| TASK-02 | Captura válida desde Tasks o Mi Día | Una tarea pendiente de hoy, título normalizado, un evento con origen correcto | Contrato Fake/Room + integración |
| TASK-03 | Abrir A, volver, abrir B y luego creación | Identidades y ViewModels aislados; creación no hereda B | Navegación real |
| TASK-04 | Escribir tras una carga lenta o pulsar Save durante carga | La carga no borra una edición válida; no se guarda estado incompleto | VM con latencia |
| TASK-05 | Guardar subtareas editadas y reordenadas | IDs únicos, posiciones consecutivas y publicación atómica | Contrato Fake/Room |
| TASK-06 | Fallar a mitad de una escritura del agregado | Sin cambios parciales ni eliminación del borrador | Integración Room |
| TASK-07 | Volver, cambiar sección y reiniciar tras persistir borrador | Se recupera el contenido completo sin publicar tarea | Persistencia + navegación |
| TASK-08 | Fallar al conservar borrador antes de salir | Editor abierto, contenido intacto y reintento visible | VM + Compose |
| TASK-09 | Descartar y cancelar/confirmar | Cancelar conserva; confirmar elimina solo borrador | Fake + Compose |
| TASK-10 | Otra superficie modifica o elimina la tarea durante edición | Conflicto/inexistencia; no sobrescritura ni resurrección | Contrato + integración |
| TASK-11 | Completar padre con pendientes | Sin mutación previa; ver, completar todas o cancelar | VM + Compose |
| TASK-12 | Completar A y B con ventanas solapadas; deshacer A | A restaurada, B conserva su ventana y analítica | Tiempo virtual + Compose |
| TASK-13 | Undo antes, exactamente al vencer y después | Solo antes puede restaurar; misma frontera en UI y datos | Contrato con reloj fake |
| TASK-14 | Undo después de cambio incompatible o eliminación | Conflicto/inexistencia sin sobrescribir ni resucitar | Contrato Fake/Room |
| TASK-15 | Fallar al restaurar una subtarea | Padre e hijos permanecen consistentes; error recuperable | Integración Room |
| TASK-16 | Recrear proceso, cambiar reloj civil o reiniciar dispositivo | No reaparecen ventanas vencidas ni se prolongan por reloj civil | Restauración + dispositivo |
| TASK-17 | Reintentar lectura repetidamente | Un observador activo; cancelación propagada | VM con fake observable |
| TASK-18 | Reprogramar, medianoche y cambio de zona | Fecha correcta y actualización reactiva de Mi Día | Reloj fake + integración |
| TASK-19 | Gesto cancelado o alternativa por botón/teclado | Cancelar no muta; alternativa produce idéntico resultado | Compose semántico |
| TASK-20 | Eliminar nota vinculada | Desaparece vínculo, tarea independiente permanece | Room con claves foráneas |
| TASK-21 | Consultar nota origen ausente o con fallo de lectura | Estado explícito y volver operativo, sin destino ficticio | VM + navegación |
| TASK-22 | Desactivar recordatorio o denegar permiso/canal | Sin avisos ni insistencia; CRUD sigue operativo | Fake de plataforma + dispositivo |
| TASK-23 | Ejecutar recordatorio repetido, atrasado o sin pendientes | Sin duplicados diarios, avisos vacíos ni días históricos | Worker con fakes |
| TASK-24 | Acción de notificación sin UI, duplicada o con ID inválido | Mutación válida única o rechazo; cierre asíncrono garantizado | Receptor + dispositivo |
| TASK-25 | Acciones desde gesto, menú, editor y notificación | Metadatos correctos sin contenido personal | FakeAnalyticsTracker |
| TASK-26 | Texto ampliado, RTL, teclado, barras y ancho reducido | Controles accesibles y sin solapamientos; Guardar visible | Compose + Roborazzi + dispositivo |
| TASK-27 | Migrar instalación previa con tareas/notas/subtareas | Datos y relaciones conservados, esquema validado | Migraciones 1→2→nuevo y 2→nuevo |

## Estrategia de pruebas y evidencia

Inventario inspeccionado: JUnit4, coroutines-test, Hilt, fakes manuales,
Robolectric y Roborazzi configurados. Hay tests de ViewModels y persistencia,
pero su presencia no acredita los escenarios anteriores. La prueba actual de
restauración reutiliza el mismo `SavedStateHandle`; debe complementarse con
serialización/restauración real. El generador de IDs del test de editor devolvía
siempre el mismo valor; se corrigió para representar subtareas persistibles
distintas, sin debilitar sus aserciones.

Aplicar la guía `testing-setup` respetando ADR-004 y el stack existente:

- Suite de contrato reutilizable para FakeTaskRepository y repositorio con Room.
  Fakes compartidos en `:core:testing`, sin mocks dinámicos ni librerías nuevas
  por conveniencia. Controlar fallos de lectura, escritura, latencia, IDs y tiempo.
- Compose con matchers semánticos; tags solo cuando la semántica no distingue
  elementos. Separar aserciones de comportamiento y regresión visual.
- Una primera captura verificable antes de expandir la UI. Lista/editor en
  anchos 400/610/900 dp y altos 400/500/1000 dp, temas y escala 1,5; comprobar
  adicionalmente comportamiento con escala 2,0 y teclado.
- Instrumentadas de Room real y recorridos de notificaciones/navegación, incluida
  API 37. No confundir una recreación de Activity con muerte del proceso.
- Ejecutar los gates de [testing](../../testing/README.md) y `AGENTS.md`.
  `checkQuality` no sustituye pruebas de dispositivo ni revisión de imágenes.
- Medir captura/triaje, scroll y apertura contra los
  [presupuestos](../../performance/performance_budgets.md), anotando dispositivo,
  configuración y build. No declarar fluidez basándose en capturas estáticas.

## Disciplina de ejecución

Implementar las decisiones aceptadas por hitos con tests de comportamiento antes
de cada cambio. No declarar un hito cerrado sin sus evidencias de verificación.
Los cambios anteriores del usuario se preservan; no hacer staging global,
limpieza del árbol ni commits con verificaciones pendientes.

## Registro de ejecución: cierre local, 2026-09-14

- ADR-012 a ADR-015 permanecen alineados con la implementación. La frontera de
  persistencia sigue siendo `core:data` → `core:database`; Tasks no importa otras
  features.
- H2: Room v3 con migraciones 1→2→3, revisiones optimistas, transacciones de
  agregado, borradores y operaciones de Undo durables. El contrato compartido
  corre contra `FakeTaskRepository` y `OfflineFirstTaskRepository`.
- H3: `TaskEditorRoute` acepta solo el ID primitivo; cada entrada tiene su
  ViewModel, restaura borradores completos y presenta conflictos sin sobrescribir.
- H4: cada finalización obtiene su operación/ventana monotónica, se puede deshacer
  por ID una sola vez y `TaskUndoRecoveryWorker` recupera analítica tras reinicio.
- H5: Mi Día consulta la fecha civil, la nota origen es solo lectura y el
  recordatorio usa trabajo único con `initialDelay`, reprogramación por zona/hora,
  acciones explícitas y receiver no exportado.
- H6: lista/editor/reminder/source note son Compose stateless con layout adaptable,
  gesto contextual (long press), semántica de accesibilidad y dos goldens revisados:
  `TaskSurfaceScreenshotTest_taskListSurface.png` y
  `TaskSurfaceScreenshotTest_editorSurface.png`.
- Pruebas añadidas: durabilidad/conflictos/expiración/reinicio en
  `FakeTaskRepositoryDurableTest`, matriz de planificación en
  `TaskReminderPlannerTest` y regresión visual en `TaskSurfaceScreenshotTest`.
- Verificación local verde: `checkQuality --continue --console=plain`, guardrail
  `*ArchitectureGuardrailsTest`, compilación de `:app:compileDebugKotlin`, tests
  unitarios de core/data, core/database, core/testing y Tasks, y
  `:features:tasks:verifyRoborazziDebug`.
- Dependency Analysis conserva una advertencia no fatal porque AGP 9.3.1 está fuera
  del rango probado por el plugin. No se debilitó ningún gate ni tolerancia visual.
- Pendiente para declarar cierre operativo en dispositivo: `connectedDebugAndroidTest`
  con API 37, validación manual de permisos/canal/notificaciones y medición de los
  presupuestos de rendimiento en un dispositivo real. No hay emulador ejecutado en
  este entorno.
