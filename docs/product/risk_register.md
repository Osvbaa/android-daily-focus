# Risk Register

| ID | Riesgo | Probabilidad | Impacto | Estrategia de Mitigación |
| :--- | :--- | :---: | :---: | :--- |
| **R-01** | La extracción con IA on-device consume demasiada memoria o calienta el dispositivo en notas largas. | Media | Alto | Limitar el prompt local a notas < 1000 palabras; fallback a cloud streaming si el hardware no cuenta con recursos suficientes. |
| **R-02** | El usuario abandona la app por fricción si la IA sugiere tareas incorrectas o irrelevantes. | Alta | Alto | La extracción siempre es de confirmación explícita (previsualización con checkboxes antes de guardar en la base de datos). |
| **R-03** | Pérdida de tareas por muerte del proceso del sistema durante la edición rápida. | Media | Medio | Persistencia de borrador automático mediante `SavedStateHandle` en Compose. |
| **R-04** | El alcance del MVP se expande hacia Pomodoro o Hábitos antes de validar el loop Tareas↔Notas. | Alta | Crítico | Regla estricta MoSCoW: los módulos `:feature:pomodoro` y `:feature:habits` no se crean en Gradle hasta que el MUST esté cerrado y medido. |