> BDD legado. Contrato vigente y estado de verificación: [spec de Notas](../../specs/notes/spec.md).

Feature: Captura de Notas y Conversión de Texto a Tarea (MUST)

  Background:
    Given el usuario accede al editor de notas

  # Happy Paths
  Scenario: Creación y guardado automático de nota en Markdown
    When escribe el título "Reunión de Producto"
    And escribe el cuerpo "Discutir roadmap de Q4 y fechas de lanzamiento"
    And navega hacia atrás
    Then la nota se persiste en Room con título y contenido
    And se registra la fecha y hora de última modificación

  Scenario: Conversión manual de fragmento resaltado a Tarea
    Given el usuario tiene una nota abierta con el texto "Revisar contrato con proveedor mañana"
    When resalta el fragmento "Revisar contrato con proveedor"
    And presiona el chip flotante "Crear Tarea"
    Then se abre el diálogo de confirmación rápida con el título pre-llenado "Revisar contrato con proveedor"
    When confirma la creación
    Then se crea una nueva tarea vinculada al ID de la nota actual en "TaskNoteCrossRef"
    And la nota permanece intacta en el editor

  # Edge Cases & Integridad Referencial
  Scenario: Eliminación de nota vinculada a tareas existentes
    Given existe una nota "Ideas de Proyecto"
    And existe una tarea "Comprar dominio" vinculada a esa nota en "TaskNoteCrossRef"
    When el usuario elimina la nota "Ideas de Proyecto"
    Then la nota se borra de la tabla de notas
    And el registro de unión en "TaskNoteCrossRef" se elimina automáticamente en cascada (ON DELETE CASCADE)
    And la tarea "Comprar dominio" NO se elimina y preserva su estado independiente

  Scenario: Eliminación de una nota sin tareas vinculadas 
   Given existe una nota "Apuntes sueltos" sin registros en "TaskNoteCrossRef" 
   When el usuario elimina la nota "Apuntes sueltos" 
   Then la nota se borra de la tabla de notas sin efectos secundarios en otras tablas
