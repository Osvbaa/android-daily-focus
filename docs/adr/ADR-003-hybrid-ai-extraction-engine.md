ADR-003: Motor de Inferencia de IA Híbrido (LiteRT On-Device + Cloud Fallback con Ktor SSE)
Estado: Aceptado

Fecha: 2026-09-07

Módulos afectados: :core:ai, :core:network, :feature:notes

Contexto
La hipótesis central del PRD (H-1) exige convertir notas en tareas en menos de 4 segundos con extracción asistida por IA. Los modelos on-device (SLMs de 1B a 2B parámetros) pesan entre 1.0 GB y 1.8 GB, lo que impide que un usuario recién instalado cuente con los pesos locales de inmediato. Además, notas extensas en dispositivos de gama media-baja saturan la memoria RAM.

Decisión
Encapsulamiento en :core:ai: Se define una interfaz abstracta AiExtractionEngine en el dominio. Las capas superiores ignoran si la inferencia corre en local o remoto.

Estrategia Híbrida:

Ruta Preferente (Edge First): Si los pesos del modelo LiteRT están descargados y el texto tiene menos de 1,000 palabras, se procesa en el dispositivo usando la NPU/GPU vía LiteRT runtime.

Ruta Alternativa (Cloud Streaming): Si los pesos no están disponibles o el hardware reporta RAM insuficiente, se delega al backend mediante Ktor con Server-Sent Events (SSE) para recibir la respuesta parseada en streaming sin bloquear la UI.

Degradación Offline: Si no hay pesos locales descargados y el dispositivo no cuenta con red, se emite un error estructurado no bloqueante (AiFailureReason.OFFLINE_NO_CLOUD).

Consecuencias
Positivas: Experiencia inmediata desde el primer minuto de instalación; privacidad y velocidad instantánea una vez descargados los pesos locales.

Negativas: Necesidad de mantener dos adaptadores del contrato (LiteRT executor local y cliente HTTP Ktor).

Verificación: Pruebas unitarias en :core:ai con un motor simulado verificando la degradación ante ausencia de red y pesos.