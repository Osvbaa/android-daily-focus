# Plan: confirmar tareas desde nota nueva

Estado: Verificado localmente. Requisitos [NOTE-02/03](spec.md), [AI-02](../ai/spec.md). ADR-012/019.

**Domain:** cada tarea extraída debe referenciar un ID de nota persistida; cero tareas antes de confirmación. **Data:** `NoteRepository.upsertNote` devuelve ID estable y `TaskRepository.createTaskFromNote` devuelve resultado tipado; no se modifica schema ni se añaden módulos. **ViewModel:** al confirmar, guardar la nota nueva o su edición pendiente antes de crear tareas; si falla el guardado, no crear ninguna. Si falla una tarea, mantener sugerencias pendientes para reintento sin duplicar las ya creadas. **UI:** [contrato](ui-contract.md), sin editar Compose. **Pruebas:** nota nueva crea solo tareas seleccionadas vinculadas; fallo de guardado no crea tareas; fallo parcial preserva solo pendientes.

La operación entre repositorios no es transacción única. El ViewModel conserva estado de éxito parcial; una transacción atómica cruzada requeriría mover la coordinación a `:core:data` y ADR si el producto la exige. `:features:notes:testDebugUnitTest --tests '*NoteEditorViewModelTest'` y `checkQuality` terminaron correctamente. Las pruebas instrumentadas no se ejecutaron; no cambió el esquema.
