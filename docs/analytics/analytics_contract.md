
# Contrato de Analítica de Producto:

> Propuesta de instrumentación; el tracker actual no envía eventos ni permite afirmar las métricas de producto. Una implementación remota requiere spec, ADR y decisión de privacidad.

## 1. Principios de Diseño y Privacidad (Zero-PII)

1. **Desacoplamiento Estricto:** La capa de UI y Dominio no conocen SDKs de terceros (Firebase, Mixpanel, PostHog). Solo consumen la interfaz `AnalyticsTracker` declarada en `:core:common`.
2. **Cero Datos Personales (Zero PII):** Queda **estrictamente prohibido** enviar títulos de tareas, cuerpo de notas, nombres de proyectos o cualquier texto ingresado por el usuario. La analítica captura exclusivamente metadatos, conteos cuantitativos, duraciones y enums.
3. **Manejo de Fallos Silencioso:** El tracker de analítica jamás propaga excepciones hacia el flujo de la aplicación (`try-catch` interno o canal no bloqueante).
4. **Respeto a la Ventana de "Deshacer" (Undo):** Para no falsear métricas, las acciones optimistas con ventana de reversión (como `TaskCompleted`) no despachan su evento de analítica hasta que los 4 segundos del temporizador de Undo hayan expirado sin cancelación.

---

## 2. Mapeo Quirúrgico contra las Hipótesis del PRD

| Métrica del PRD | Hipótesis a Validar | Eventos y Parámetros Involucrados | Fórmula de Cálculo |
| --- | --- | --- | --- |
| **H-1: Adopción de IA** | 35% de tareas creadas nacen de notas vía IA. | `task_created` (`origin = AI_NOTE`) vs (`origin != AI_NOTE`). | $\frac{\text{tasks}(origin == AI\_NOTE)}{\text{total tasks created}} \ge 0.35$ |
| **H-2: Retención D30** | Retención $\ge 28\%$ al día 30. | `session_started` agrupado por `user_pseudo_id` e intervalo temporal. | $\frac{\text{Active Users}(D30)}{\text{Cohort New Users}(D0)} \ge 0.28$ |
| **H-3: Captura < 4s** | Fricción de triaje inicial imperceptible. | `task_created` (`duration_ms`), `task_rescheduled` (`duration_ms`). | $P_{75}(\text{duration\_ms}) < 4000$ |
| **H-4: Acciones Fuera de Pantalla** | $\ge 40\%$ de interacciones fuera de la UI completa sin canibalizar retención. | `task_completed`, `task_rescheduled` con parámetro `surface`. | $\frac{\text{actions}(surface \in [NOTIFICATION, WIDGET, GESTURE])}{\text{total actions}} \ge 0.40$ |

---

## 3. Catálogo Exhaustivo de Eventos Tipados

### Dominio: Tareas (`tasks`)

#### `task_created`

Emitido inmediatamente después de persistir una tarea con éxito en base de datos.

* **Parámetros:**
* `origin` (`TaskOrigin`): Canal de captura. Valores: `MANUAL_QUICK_INPUT`, `MANUAL_FULL_EDITOR`, `AI_NOTE`, `NOTE_TEXT_SELECTION`.
* `surface` (`InteractionSurface`): Origen del gesto. Valores: `APP_SCREEN`, `QUICK_WIDGET`, `NOTIFICATION_ACTION`.
* `has_due_date` (`Boolean`): Si se programó con fecha de vencimiento.
* `is_priority_set` (`Boolean`): Si se asignó prioridad explícita.
* `duration_ms` (`Long`): Tiempo transcurrido desde que se abrió el input/diálogo hasta la confirmación.

#### `task_completed`

Emitido **únicamente tras transcurrir los 4 segundos** del Snackbar de Undo sin que el usuario haya revertido la acción.

* **Parámetros:**
* `surface` (`InteractionSurface`): `APP_SCREEN`, `NOTIFICATION_ACTION`, `WIDGET`.
* `task_lifetime_hours` (`Int`): Horas transcurridas desde `created_at` hasta su compleción.
* `was_overdue` (`Boolean`): Indica si la tarea se completó después de su fecha límite.

#### `task_rescheduled`

Emitido al posponer o mover la fecha programada de una tarea.

* **Parámetros:**
* `surface` (`InteractionSurface`): `APP_SCREEN_RADIAL_MENU`, `APP_SCREEN_DATE_PICKER`, `NOTIFICATION_ACTION`.
* `preset_selected` (`ReschedulePreset`): `TODAY`, `TOMORROW`, `THIS_WEEKEND`, `NEXT_WEEK`, `CUSTOM_DATE`.
* `duration_ms` (`Long`): Milisegundos que tomó completar el gesto de triaje.

#### `task_deleted`

Emitido al borrar definitivamente una tarea.

* **Parámetros:**
* `surface` (`InteractionSurface`): `APP_SCREEN_DETAIL`, `APP_SCREEN_SWIPE`.
* `had_linked_note` (`Boolean`): Indica si la tarea provenía de una nota vinculada.

---

### Dominio: Notas y Edge AI (`notes` & `ai`)

#### `note_saved`

Emitido al crear o actualizar una nota tras salir del editor.

* **Parámetros:**
* `char_count_bucket` (`String`): Rango de caracteres para análisis de volumen sin exponer texto (`"1-100"`, `"101-500"`, `"501-1000"`, `"1000+"`).
* `has_checklists` (`Boolean`): Si el cuerpo contiene sintaxis `[ ]`.
* `is_new_note` (`Boolean`): `true` en creación, `false` en edición subsiguiente.

#### `note_ai_extraction_requested`

Emitido en el instante en que el usuario presiona el botón "Extraer Tareas".

* **Parámetros:**
* `word_count` (`Int`): Cantidad de palabras en la nota evaluada.
* `engine_targeted` (`AiEngine`): `LOCAL_LITERT`, `CLOUD_FALLBACK`.

#### `note_ai_extraction_completed`

Emitido cuando el motor de IA termina el análisis y presenta la hoja de confirmación.

* **Parámetros:**
* `engine_used` (`AiEngine`): `LOCAL_LITERT`, `CLOUD_FALLBACK`.
* `latency_ms` (`Long`): Milisegundos exactos consumidos por el proceso de inferencia.
* `tasks_detected_count` (`Int`): Total de tareas encontradas por el modelo.



#### `note_ai_tasks_confirmed`

Emitido cuando el usuario presiona "Agregar Seleccionadas" en la hoja inferior.

* **Parámetros:**
* `tasks_suggested_count` (`Int`): Total de tareas presentadas al usuario.
* `tasks_accepted_count` (`Int`): Tareas que el usuario mantuvo seleccionadas.
* `acceptance_rate` (`Float`): $\frac{\text{tasks\_accepted\_count}}{\text{tasks\_suggested\_count}}$ (ej. `0.75`).

#### `note_ai_extraction_failed`

Emitido ante fallos de análisis o degradación no recuperable.

* **Parámetros:**
* `failure_reason` (`AiFailureReason`): `WEIGHTS_NOT_DOWNLOADED`, `OFFLINE_NO_CLOUD`, `INSUFFICIENT_TEXT`, `INFERENCE_TIMEOUT`, `PARSER_ERROR`.

---

## 4. Contratos de Código en Kotlin (`:core:common`)

Estructura de interfaces y modelos inmutables a implementar:

/**
 * Puerto de analítica desacoplado. Consumido por Repositorios y ViewModels.
 */
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

/**
 * Evento base sellado. Todos los eventos son clases fuertemente tipadas.
 */
sealed interface AnalyticsEvent {
    val name: String
    val params: Map<String, Any>

    // --- DOMINIO: TAREAS ---
    data class TaskCreated(
        val origin: TaskOrigin,
        val surface: InteractionSurface,
        val hasDueDate: Boolean,
        val isPrioritySet: Boolean,
        val durationMs: Long
    ) : AnalyticsEvent {
        override val name = "task_created"
        override val params = mapOf(
            "origin" to origin.name,
            "surface" to surface.name,
            "has_due_date" to hasDueDate,
            "is_priority_set" to isPrioritySet,
            "duration_ms" to durationMs
        )
    }

    data class TaskCompleted(
        val surface: InteractionSurface,
        val taskLifetimeHours: Int,
        val wasOverdue: Boolean
    ) : AnalyticsEvent {
        override val name = "task_completed"
        override val params = mapOf(
            "surface" to surface.name,
            "task_lifetime_hours" to taskLifetimeHours,
            "was_overdue" to wasOverdue
        )
    }

    // --- DOMINIO: NOTAS & IA ---
    data class NoteAiExtractionCompleted(
        val engineUsed: AiEngine,
        val latencyMs: Long,
        val tasksDetectedCount: Int
    ) : AnalyticsEvent {
        override val name = "note_ai_extraction_completed"
        override val params = mapOf(
            "engine_used" to engineUsed.name,
            "latency_ms" to latencyMs,
            "tasks_detected_count" to tasksDetectedCount
        )
    }

    data class NoteAiTasksConfirmed(
        val tasksSuggestedCount: Int,
        val tasksAcceptedCount: Int,
        val acceptanceRate: Float
    ) : AnalyticsEvent {
        override val name = "note_ai_tasks_confirmed"
        override val params = mapOf(
            "tasks_suggested_count" to tasksSuggestedCount,
            "tasks_accepted_count" to tasksAcceptedCount,
            "acceptance_rate" to acceptanceRate
        )
    }
}

enum class TaskOrigin {
    MANUAL_QUICK_INPUT,
    MANUAL_FULL_EDITOR,
    AI_NOTE,
    NOTE_TEXT_SELECTION
}

enum class InteractionSurface {
    APP_SCREEN,
    APP_SCREEN_RADIAL_MENU,
    APP_SCREEN_DATE_PICKER,
    APP_SCREEN_SWIPE,
    APP_SCREEN_DETAIL,
    NOTIFICATION_ACTION,
    QUICK_WIDGET
}

enum class AiEngine {
    LOCAL_LITERT,
    CLOUD_FALLBACK
}

enum class AiFailureReason {
    WEIGHTS_NOT_DOWNLOADED,
    OFFLINE_NO_CLOUD,
    INSUFFICIENT_TEXT,
    INFERENCE_TIMEOUT,
    PARSER_ERROR
}

---

## 5. Implementación del Debounce de "Undo" para `TaskCompleted`

Mecanismo estándar para diferir la emisión del evento analítico hasta que concluyan los 4 segundos del Snackbar:

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Gestor de eventos diferidos con soporte para cancelación por reversión (Undo).
 */
class DelayedAnalyticsDispatcher(
    private val tracker: AnalyticsTracker,
    private val scope: CoroutineScope
) {
    private val pendingCompletions = ConcurrentHashMap<String, Job>()

    fun scheduleTaskCompletion(
        taskId: String,
        event: AnalyticsEvent.TaskCompleted,
        undoWindowMillis: Long = 4000L
    ) {
        // Cancela cualquier temporizador previo para este mismo ID
        cancelPendingCompletion(taskId)

        val job = scope.launch {
            delay(undoWindowMillis)
            tracker.track(event)
            pendingCompletions.remove(taskId)
        }
        pendingCompletions[taskId] = job
    }

    fun cancelPendingCompletion(taskId: String) {
        pendingCompletions.remove(taskId)?.cancel()
    }
}

---

## 6. Verificación en Pruebas Unitarias

Para cumplir con la regla de **prohibición de dynamic mocking** (`MockK`/`Mockito`), los tests de eventos usarán un Fake puro en `:core:testing`:

// :core:testing/src/main/kotlin/com/dailyfocus/core/testing/analytics/FakeAnalyticsTracker.kt
class FakeAnalyticsTracker : AnalyticsTracker {
    private val _events = mutableListOf<AnalyticsEvent>()
    val events: List<AnalyticsEvent> get() = _events.toList()

    override fun track(event: AnalyticsEvent) {
        _events.add(event)
    }

    fun hasDispatched(eventName: String): Boolean = _events.any { it.name == eventName }
    fun clear() = _events.clear()
}
