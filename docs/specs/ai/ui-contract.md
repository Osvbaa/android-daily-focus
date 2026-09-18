# Contrato UI: extracción IA

El propietario implementa los visuales. Estados: disponible, descargable, descargando, no soportado, extrayendo, sugerencias, sin acciones, texto insuficiente y fallo. La UI presenta sugerencias seleccionables y solicita confirmación antes de crear tareas; una extracción fallida conserva texto y selección. La disponibilidad del modelo se puede actualizar durante la vida del editor. Movimiento y transiciones deben respetar reducción de movimiento y no ocultar estado de progreso/error. Pruebas de aceptación: AI-01 a AI-04, incluyendo cancelación y nota nueva.
