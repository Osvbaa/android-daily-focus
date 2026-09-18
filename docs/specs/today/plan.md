# Plan: errores de captura rápida

Estado: lógica verificada; visuales pendientes del propietario. Requisito TODAY-04.

Domain/Data: reutilizar validación y `TaskRepository` existentes. ViewModel: preservar `errorMessage` local al combinar flujos de tareas, proyectos y hábitos; despejarlo en edición o éxito. UI: [contrato](ui-contract.md); el propietario muestra el mensaje. Tests: prueba de validación fallida mientras se observa el estado; `checkQuality`. Sin migración.
