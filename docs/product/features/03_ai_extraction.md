Feature: Extracción Contextual de Tareas con IA (MUST)

  # Happy Path On-Device
  Scenario: Extracción exitosa local mediante LiteRT
    Given el usuario visualiza una nota guardada con 60 palabras
    And los pesos del modelo LiteRT están disponibles en el almacenamiento local
    When presiona el botón "Extraer Tareas"
    Then la UI entra en estado "Loading" con indicador no bloqueante
    And el motor LiteRT procesa el texto en local
    And la UI transiciona a "Success" desplegando una hoja inferior de revisión con las tareas detectadas
    And ninguna tarea se ha insertado en Room todavía

  # Confirmación Selectiva
  Scenario: Confirmación parcial de tareas sugeridas
    Given la hoja inferior muestra 2 tareas sugeridas: "Enviar reporte" y "Pedir insumos"
    When el usuario desmarca "Pedir insumos"
    And presiona "Agregar (1) Tarea"
    Then solo se persiste en Room la tarea "Enviar reporte"
    And se genera el vínculo en "TaskNoteCrossRef" con la nota de origen
    And se emite el evento de analítica "TaskCreated" con origen "AI_NOTE"
    And la hoja inferior se cierra con confirmación háptica

  # Edge Cases & Fallbacks
  Scenario: Nota con contenido insuficiente para análisis
    Given el usuario tiene una nota con el texto "Comprar pan" (menos de 10 palabras)
    When visualiza la barra de herramientas de la nota
    Then el botón "Extraer Tareas" se muestra deshabilitado o con tooltip explicativo
    And no se dispara ninguna llamada a motores de IA

  Scenario: La nota no contiene elementos accionables
    Given el usuario presiona "Extraer Tareas" en una nota de reflexión personal sin compromisos
    When el motor de IA concluye el análisis sin detectar acciones
    Then la UI muestra un estado informativo: "No se detectaron tareas pendientes en esta nota"
    And no se altera la base de datos

  Scenario: Fallback a Cloud Ingestion por ausencia de modelo local
    Given los pesos de LiteRT no se han descargado en el dispositivo
    And el dispositivo cuenta con conexión a Internet activa
    When el usuario solicita "Extraer Tareas"
    Then el sistema canaliza la solicitud vía Ktor SSE hacia el backend en Cloud
    And la hoja inferior despliega las tareas sugeridas normalmente
    And se emite el evento de analítica "NoteAiSummarized" con target "CLOUD"

  Scenario: Falla total de inferencia offline sin modelo descargado
    Given los pesos de LiteRT no están disponibles
    And el dispositivo está en modo avión (sin red)
    When el usuario solicita "Extraer Tareas"
    Then el sistema muestra un estado de error transitorio: "Se requiere conexión para descargar el motor de IA"
    And la nota permanece editable sin bloqueos ni cierres forzados