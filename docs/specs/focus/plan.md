# Plan: decisión de objetivo tras Focus

Estado: En curso. Requisito [FOCUS-03](spec.md), [ADR-020](../../adr/ADR-020-focus-target-confirmation.md).

Domain: estado `NONE/PENDING/CONFIRMED/DISMISSED`. Data: columna aditiva en `focus_sessions`, migración 9→10 con histórico `NONE`, repositorio para observar y resolver pendientes, coordinación con tarea/hábito y ledger. ViewModel: eventos de confirmar/omitir y estado pendiente restaurable. UI: solo [contrato](ui-contract.md). Pruebas: migración, persistencia y coordinación con fakes; gates completos. La UI visual queda para el propietario.

La prueba de migración compila; `connectedDebugAndroidTest` no pudo ejecutarse porque no había dispositivo conectado. Se requiere ejecución en dispositivo/emulador antes de declarar la migración verificada.
