# Notas

Estado: Vigente; reconciliación con [BDD legado](../../product/features/02_notes.md) pendiente. Prioridad P0.

| ID | Regla observable | Estado | Evidencia inicial |
|---|---|---|---|
| NOTE-01 | Crear, editar y consultar notas conserva contenido tras reinicio | Vigente | NoteRepository, Room, ViewModels |
| NOTE-02 | Convertir sugerencia seleccionada en tarea solicita confirmación y mantiene origen | Vigente | `NoteEditorViewModelTest`: nota nueva, selección, ID persistido y reintento parcial |
| NOTE-03 | Un fallo al guardar la nota no crea tareas ni pierde sugerencias | Vigente | `NoteEditorViewModelTest`: error de repositorio y reintento |

La nota sigue disponible sin motor de IA. Al confirmar sugerencias sobre una nota nueva o editada, se guarda primero la nota y después se crean solo las tareas seleccionadas vinculadas a su ID. Si falla el guardado, no se crean tareas. Si algunas tareas ya se crearon antes de un fallo posterior, el reintento conserva solo las pendientes. La conversión de texto seleccionado no quedó verificada por esta slice. Markdown, checklist, búsqueda y transclusión se consideran propuestas solo donde no exista implementación y prueba; no inferirlas del PRD histórico.
