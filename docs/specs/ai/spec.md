# Extracción asistida de tareas

Estado: Motor local vigente, disponibilidad condicional. Prioridad P0. Decisión [ADR-019](../../adr/ADR-019-local-ai-and-dormant-modules.md); [BDD legado](../../product/features/03_ai_extraction.md).

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| AI-01 | El motor devuelve sugerencias estructuradas y descarta salidas inválidas | Vigente | `:ai` parser y tests |
| AI-02 | La persona confirma antes de que una sugerencia sea tarea | Vigente | `NoteEditorViewModelTest`: extracción no escribe; confirmación seleccionada sí |
| AI-03 | Sin disponibilidad de modelo se comunica error y continúa la captura manual | No verificado | Revisar motor y ViewModel |
| AI-04 | La extracción productiva no envía contenido de nota a un backend | Vigente | Adaptador ML Kit local, ADR-019 |

Cloud fallback, LiteRT y Koog son propuestas fuera del grafo. No mostrar métricas de éxito sin instrumentación y consentimiento definidos.
