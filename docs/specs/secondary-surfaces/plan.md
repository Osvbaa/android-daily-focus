# Plan: Calendario seleccionable

Estado: Lógica verificada, UI pendiente. Requisito [SURFACE-01](spec.md).

Domain/Data: `TaskRepository.observeTasksForDay(epochDay)` ya ofrece el contrato; no se añade submódulo, DAO ni migración. ViewModel: selección reactiva persistida en `SavedStateHandle`, consulta de tareas del día elegido y estados carga/error. UI: [contrato](ui-contract.md), implementación visual del propietario. Pruebas: `CalendarViewModelTest` confirma selección y restauración; `checkQuality` pasó tras este cambio.
