Feature: Gestión y Captura Rápida de Tareas (MUST)

  Background:
    Given el sistema de persistencia local Room está inicializado

  # Happy Paths
  Scenario: Creación ultra-rápida de una tarea para hoy
    Given el usuario se encuentra en el Dashboard "Mi Día"
    When ingresa el título "Revisar arquitectura de red" en la barra de captura rápida
    And confirma la acción
    Then la tarea se persiste en Room con estado "PENDIENTE"
    And la fecha asignada es la fecha actual del sistema
    And la tarea aparece en la lista visible de "Mi Día"
    And se emite el evento de analítica "TaskCreated" con origen "MANUAL"

  Scenario: Reprogramación ágil mediante selector rápido
    Given existe una tarea pendiente "Pagar servidor" fechada para hoy
    When el usuario abre el menú de triaje rápido de la tarea
    And selecciona la opción "Mañana"
    Then la fecha de vencimiento se actualiza a la fecha de mañana en base de datos
    And la tarea desaparece de la vista "Mi Día"

  Scenario: Completar tarea con opción de deshacer (Undo)
    Given existe una tarea pendiente "Comprar café" visible en "Mi Día"
    When el usuario marca el checkbox de completado
    Then el estado de la tarea cambia a "COMPLETADA"
    And se muestra un Snackbar con la acción "Deshacer" durante 4 segundos
    When el usuario presiona "Deshacer" antes de que expire el tiempo
    Then el estado de la tarea vuelve a "PENDIENTE"
    And la tarea permanece en la lista de "Mi Día"

  # Analítica
  Scenario: Emisión de analítica de finalización solo tras expirar la ventana de Undo
    Given existe una tarea pendiente "Enviar factura"
    When el usuario marca el checkbox de completado And no presiona "Deshacer" durante los 4 segundos del Snackbar
    Then se emite el evento de analítica "TaskCompleted"

  Scenario: No se emite analítica de finalización si se deshace a tiempo 
    Given existe una tarea pendiente "Enviar factura" 
    When el usuario marca el checkbox de completado And presiona "Deshacer" antes de que expiren los 4 segundos 
    Then NO se emite el evento de analítica "TaskCompleted"

  Scenario: Eliminación definitiva de una tarea
    Given existe una tarea pendiente "Tarea de prueba"
    When el usuario elimina la tarea desde el menú de triaje rápido 
    Then la tarea se elimina de Room de forma permanente 
    And la tarea desaparece de todas las vistas ("Mi Día", listados, búsqueda)

  # Edge Cases & Validaciones
  Scenario: Intento de creación con título vacío o solo espacios
    Given el usuario activa el campo de captura rápida
    When ingresa "   " y presiona confirmar
    Then el sistema ignora la acción
    And no se inserta ningún registro en la base de datos
    And el campo de texto mantiene el foco sin emitir errores intrusivos

  Scenario: Persistencia de estado ante interrupción (Process Death)
    Given el usuario escribe "Borrador de tarea importante" en la barra de captura rápida
    When el sistema destruye el proceso de la aplicación en segundo plano
    And el usuario regresa a la aplicación
    Then el texto "Borrador de tarea importante" permanece en el campo de entrada

  Scenario: Completar un padre con subtareas pendientes
    Given una tarea contiene subtareas pendientes
    When el usuario intenta completar la tarea padre
    Then no se persiste ningún cambio
    And se ofrecen las acciones "Ver subtareas" y "Completar todas"

  Scenario: Guardar subtareas ordenadas de un nivel
    Given el usuario edita una tarea sin escribir todavía en Room
    When añade, edita y reordena subtareas y pulsa "Guardar"
    Then la tarea y sus subtareas se persisten atómicamente con posiciones consecutivas

 
  
