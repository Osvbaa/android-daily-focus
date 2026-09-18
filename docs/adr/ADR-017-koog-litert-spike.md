# ADR-017: Spike aislado Koog + LiteRT

Estado: Propuesto

## Objetivo

Evaluar si un modelo Gemma/LiteRT ejecutado mediante Koog puede complementar o reemplazar el adaptador Gemini Nano sin convertirlo en requisito del MVP.

## Criterios de aceptación

- Sin llamadas de red durante inferencia.
- Modelo y runtime no aumentan el APK base en más de 25 MB sin justificarlo.
- P95 de extracción menor a 4 s en un dispositivo de referencia.
- Memoria adicional menor a 300 MB y sin ANR/jank observable.
- Parser produce únicamente sugerencias válidas del contrato `AiExtractionResult`.
- Fallback manual conserva toda la funcionalidad cuando no hay modelo.

## Método

El spike se implementa detrás de `AiExtractionEngine` en un source set o módulo experimental separado. Se comparan tamaño, tiempo de primera inferencia, P50/P95, memoria, batería y exactitud sobre un corpus local anonimizado. No se añade Koog ni LiteRT a `:app` hasta que todos los criterios pasen.

## Decisión actual

El MVP usa Gemini Nano mediante ML Kit Prompt API. Koog es un framework de orquestación, no una fuente de modelos ni una garantía de costo cero; por ello queda fuera del grafo productivo hasta completar las mediciones.
