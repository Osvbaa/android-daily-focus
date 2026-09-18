# Plan: acreditación de niveles

Estado: mejora de nivel verificada; coordinación durable pendiente de ADR-021. Requisito HABIT-02.

Domain: orden Mini → Plus → Elite, sin descensos. Data: `HabitRepository` guarda una finalización por hábito/día y `ActivityRepository` acredita con clave idempotente. ViewModel: mantiene eventos de nivel; la coordinación transaccional entre repositorios se resolverá bajo [ADR-021](../../adr/ADR-021-unified-completion-ledger-proposal.md). UI: [contrato](ui-contract.md), visuales del propietario. Tests: integración Room de mejora de nivel y evento único; `checkQuality`. Sin migración.
