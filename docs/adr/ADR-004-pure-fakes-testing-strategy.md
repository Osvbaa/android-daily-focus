ADR-004: Estrategia de Testing con Fakes Manuales Puros y Prohibición de Dynamic Mocking
Estado: Aceptado

Fecha: 2026-09-07

Módulos afectados: :core:testing, todos los módulos de tests (testDebugUnitTest)

Contexto
El uso de frameworks de mocking por reflexión dinámica (MockK, Mockito) introduce serios problemas:

Ralentiza la suite de tests por sobrecarga del classloader y manipulación de bytecode en Kotlin 2.x.

Produce tests frágiles acoplados a la implementación interna de los métodos (every { repo.get() } returns ...) en lugar de evaluar el comportamiento observable.

Los agentes de IA frecuentemente abusan de los mocks para forzar tests en verde que ocultan fallos de integración y concurrencia.

Decisión
Prohibición Total: Se prohíbe terminantemente la inclusión de mockk o mockito en cualquier bloque de dependencias de build.gradle.kts o libs.versions.toml.

Fakes Puros en Memoria: Todo repositorio, cliente de red o motor que requiera aislamiento en pruebas debe contar con una implementación "Fake" basada en la misma interfaz de dominio, ubicada exclusivamente en :core:testing.

Comportamiento Real: Los Fakes mantendrán estado mutable interno protegido por corrutinas (ej. listas en memoria, MutableStateFlow) para replicar con exactitud el comportamiento del contrato real (errores, latencias, filtros).

Consecuencias
Positivas: Tiempos de ejecución de tests unitarios ultrarrápidos (milisegundos); pruebas 100% deterministas y portables; arquitectura desacoplada orientada a contratos.

Negativas: Se debe escribir y mantener código de infraestructura de testing manualmente en :core:testing.

Verificación: Konsist fallará el build si se detecta cualquier importación de paquetes io.mockk.* o org.mockito.* en el árbol de código.