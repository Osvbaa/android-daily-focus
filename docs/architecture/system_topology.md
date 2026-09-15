1. Grafo Acíclico Dirigido (DAG)
   El sistema implementa una arquitectura modular desacoplada basada en capas orientadas a features y bibliotecas núcleo transversales.
                          ┌─────────────┐
                          │    :app     │
                          └──────┬──────┘
                                 │ (compila todo el grafo)
┌───────────────────┬────────────┴───────┬───────────────────┐
▼                   ▼                    ▼                   ▼
┌───────────────┐   ┌───────────────┐    ┌───────────────┐   ┌───────────────┐
│ :feature:home │   │:feature:tasks │    │:feature:notes │   │ :feature:ai   │
└───────┬───────┘   └───────┬───────┘    └───────┬───────┘   └───────┬───────┘
        │                   │                    │                   │
        │ (solo APIs)       │                    │                   │
        ├───────────────────┴──────────┬─────────┴───────────────────┤
        │                              │                             │
        ▼                              ▼                             ▼
┌───────────────┐              ┌───────────────┐             ┌───────────────┐
│   :core:ui    │              │  :core:data   │             │   :core:ai    │
└───────┬───────┘              └───────┬───────┘             └───────┬───────┘
        │                              │                             │
        ▼                              ├─────────────────────────────┘
┌───────────────┐                      │
│:core:design...│                      ├─────────────────────────────┐
└───────┬───────┘                      │                             │
        │                              ▼                             ▼
        │                      ┌───────────────┐             ┌───────────────┐
        │                      │:core:database │             │ :core:network │
        │                      └───────┬───────┘             └───────┬───────┘
        │                              │                             │
        ├──────────────────────────────┴───────────────┬─────────────┘
        │                                              │
        ▼                                              ▼
┌───────────────┐                              ┌───────────────┐
│ :core:common  │                              │  :core:model  │
└───────┬───────┘                              └───────────────┘
        │                                       ▲ (Kotlin JVM)
        └───────────────────────────────────────┘

Regla Inmutable: Las dependencias fluyen estrictamente de arriba hacia abajo. Queda prohibida cualquier flecha horizontal entre nodos del mismo nivel, en especial entre módulos :feature:*.

1. Inventario de Módulos y Responsabilidades
Capa de Integración:app (Android Application):
Orquestador central y punto de entrada (MainActivity).
Configuración del grafo de inyección global con Hilt (@HiltAndroidApp).
Implementa NavDisplay de Navigation 3 integrando los destinos de las features.
Define la resolución de Deep Links (DeepLinkHandler).
2. Capa de Funcionalidades (:feature:*)
:feature:home (Android Library):
Dashboard unificado "Mi Día".
Consume flujos combinados de tareas de hoy y notas recientes.
:feature:tasks (Android Library):
Captura rápida, lista de pendientes, triaje ágil (hoy/mañana/semana) y subtareas.
Contiene TaskNotificationReceiver para acciones rápidas fuera de pantalla (ADR-010).
:feature:notes (Android Library):
Editor de notas con soporte de Markdown, transclusión y selección de texto.
Disparador de extracción de tareas vía UI.
:feature:pomodoro (SHOULD - Fuera del MVP inmediato):
Temporizador de concentración, soporte para Doze Mode y Foreground Service.
:feature:habits (COULD - Fuera del MVP inmediato):
Seguimiento de hábitos elásticos de 3 niveles (Mini/Plus/Elite).
Capa Intermedia de Dominio y Datos (:core:*)
:core:data (Android Library):
Implementaciones concretas de repositorios (OfflineFirstTaskRepository, OfflineFirstNoteRepository).
Gestión de transacciones cruzadas (ej. creación de tarea vinculada desde nota mediante TaskNoteCrossRef).
Motor de sincronización en segundo plano.
:core:ai (Android Library):
Implementación del motor híbrido de inferencia (ADR-003).
Adaptador local para LiteRT runtime y cliente para Cloud Fallback.
Parser determinista de salida de IA a estructuras de tareas.Capa de UI y Tokens (:core:*)
:core:designsystem (Android Library):
Tokens de diseño Material Design 3 (ColorScheme, TypeScale, Shapes, Insets).
Componentes visuales atómicos no acoplados a modelos de negocio (Botones, Chips, TextFields, Skeletons).
:core:ui (Android Library):
Componentes visuales reutilizables que aceptan modelos de negocio de :core:model (ej. TaskCard, NoteSnippetCard).
Capa de Infraestructura Base (:core:*)
:core:database (Android Library):
AppDatabase única con Room fijado en el catálogo y BundledSQLiteDriver (ADR-002 y ADR-012). Las features consumen repositorios; solo :core:data consume DAOs.
Definición de @Entity, migraciones SQLite y DAOs granulares.
:core:network (Android Library):
Cliente Ktor configurado con serialización JSON y SSE para streaming.
Data Transfer Objects (DTOs) remotos.
:core:common (Android Library):
Clases base de concurrencia (AppDispatchers), wrappers funcionales (Result<T>).
Contrato de analítica desacoplado (AnalyticsTracker, DelayedAnalyticsDispatcher).
:core:model (Kotlin JVM Puro):Único módulo con plugin kotlin("jvm").
Cero dependencias del framework Android.
Clases de dominio inmutables (Task, Note, Project), IDs fuertemente tipados y enums.
:core:testing (Android Library):
Módulo transversal de infraestructura para tests unitarios y de integración.
Aloja los Fakes puros en memoria (FakeTaskRepository, FakeAnalyticsTracker, FakeAiEngine).
Aloja las pruebas estáticas de arquitectura con Konsist.

3. Matriz de Visibilidad y Reglas de Dependencia
La siguiente tabla estipula qué módulos tienen permitido declarar como dependencia a otros módulos mediante Gradle (implementation o api):
Módulo Consumidor Módulos Permitidos como Dependencia Módulos Estrictamente Prohibidos
:app Todas las :feature:*, :core:designsystem, :core:common, :core:modelNinguno (nodo raíz)
:feature:* :core:data, :core:ui, :core:designsystem, :core:model, :core:commonOtras :feature:*, :core:database, :core:network
:core:data :core:database, :core:network, :core:model, :core:commonCualquier :feature:*, :core:ui, :core:designsystem:core:database:core:model, :core:commonCualquier :feature:*, :core:data, :core:network:core:network:core:model, :core:commonCualquier :feature:*, :core:data, :core:database:core:ui:core:designsystem, :core:model, :core:commonCualquier :feature:*, :core:data, :core:database:core:designsystemNinguno (solo dependencias Compose de terceros)Todo módulo de la aplicación:core:modelNinguno (Kotlin JVM puro, ni siquiera android.*)Todo módulo de la aplicación:core:common:core:modelTodo módulo de feature, datos o UI4. Contrato Estricto entre Capas (Clean Slice)

4. Para asegurar que los agentes de IA no mezclen responsabilidades, cada flujo debe respetar esta separación de interfaces:

[ UI / Compose Screen ]
   │
   ▼ (Consume UiState / Emite UiEvent)
   [ Screen ViewModel ]
   │
   ▼ (Invoca suspend fun o Flow de dominio)
   [ Repository Interface ] (Ubicada conceptualmente en capa de datos consumible)
   │
   ▼ (Implementada por)
   [ OfflineFirstRepository ] (Ubicada en :core:data)
   │
   ┌────┴───────────────────────────┐
   ▼                                ▼
   [ Room DAO ] (:core:database)    [ Ktor API Client ] (:core:network)
   │                                │
   ▼                                ▼
   [ Local SQLite DB ]              [ Remote REST/SSE Backend ]
   Reglas de Conversión y Mapeo:Entidades SQLite (*Entity): Residen exclusivamente dentro de :core:database. Jamás se exponen hacia los módulos :feature:*.DTOs de Red (*Dto / *Response): Residen exclusivamente dentro de :core:network. Jamás escapan hacia :core:data sin ser mapeados.Modelos de Dominio: :core:data mapea de Entity a Model antes de emitir a través de los métodos del repositorio. Los ViewModels y composables manejan únicamente modelos de dominio puros definidos en :core:model.5. Salvaguardas Estáticas de Arquitectura (Konsist)En la Fase 7 y 8, el módulo :core:testing ejecutará validaciones automáticas para hacer cumplir este documento en CI:Kotlin// Reglas a implementar en ArchitectureTest.kt
   class ArchitectureTest {

   @Test
   fun `feature modules never depend on other feature modules`() {
   Konsist.scopeFromProject()
   .modules("feature..")
   .assertFalse { module ->
   module.dependsOn(Konsist.scopeFromProject().modules("feature..") - module)
   }
   }

   @Test
   fun `core model has zero android framework dependencies`() {
   Konsist.scopeFromModule("core:model")
   .files
   .assertFalse { it.hasImport { import -> import.name.startsWith("android.") } }
   }

   @Test
   fun `features never directly access room database`() {
   Konsist.scopeFromProject()
   .modules("feature..")
   .files
   .assertFalse { it.hasImport { import -> import.name.contains("androidx.room") } }
   }
   }
