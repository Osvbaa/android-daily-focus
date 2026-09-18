# ADR-021: Cierre y crédito de actividad unificados

Estado: Propuesto. Requiere revisión de producto/arquitectura antes de implementarse.

## Contexto

Today, Tasks, Habits, Focus y el receiver de notificación mutan objetivos mediante repositorios, pero el crédito del ledger se coordina en distintos ViewModels. La notificación completa tareas sin crédito de actividad y emite `TaskCompleted` con duración `0` y `wasOverdue = false` constantes. ADR-016 exige un ledger local idempotente y ADR-010 impide que el receiver escriba DAOs directamente.

## Decisión propuesta

Crear en `:core:data` contratos de acción de negocio para completar tarea/hábito y acreditar actividad. Una operación incluirá superficie de origen, fecha civil obtenida de `DateProvider` y política de Undo. Todos los callers (UI, notificación, Focus confirmado) invocarán el mismo contrato. El receptor no calculará métricas ni ledger; recibirá un resultado tipado.

El crédito de tareas con Undo se emite al vencer la ventana durable sin reversión, una vez. La notificación sin Undo acreditará tras mutación exitosa. Los eventos del ledger usan claves idempotentes existentes. La analítica derivará duración y atraso de la tarea persistida, sin valores constantes ni contenido personal.

## Riesgos y pruebas requeridas

Definir transacción y recuperación si se completa la tarea pero falla el ledger; no reutilizar una operación de Undo expirada. Probar igualdad de resultado desde Tasks, Today y notificación, reversión dentro/fuera de ventana, tarea de proyecto/hito y doble entrega del Intent. Probar migraciones solo si la solución añade estado persistido; no introducir otro `AppDatabase`.
