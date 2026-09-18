# Habits - contrato de cierre

> Registro histórico de cierre. Contrato vigente: [spec de Hábitos](../../specs/habits/spec.md).

Estado: implementado localmente como slice de cierre; pendiente validacion conectada en dispositivo.

## Alcance

Un habito define una practica recurrente y registra cumplimiento por fecha civil. El streak se calcula a partir de dias con algun nivel cumplido.

## Dominio

- `HabitId` es fuertemente tipado.
- `Habit(name, frequency, intervalDays, occurrencesPerWeek, targetLevel)`.
- `HabitCompletion(habitId, epochDay, level)` registra Mini, Plus o Elite.
- Los tres niveles son fijos: Mini, Plus y Elite.

## Frecuencias

- `DAILY`: una oportunidad cada dia.
- `WEEKLY`: una oportunidad en la semana civil lunes-domingo.
- `EVERY_N_DAYS`: usa el dia de creacion como ancla y `intervalDays`.
- `TIMES_PER_WEEK`: permite de 1 a 7 dias distintos por semana mediante `occurrencesPerWeek`.

Subir de nivel el mismo dia actualiza el cumplimiento; no crea un segundo dia de racha.

## Integracion

Las fechas usan `epochDay` y `DateProvider`. El Focus Timer puede guardar `habitId` y el nivel elegido al iniciar; solo al llegar a cero registra el nivel automáticamente. Cancelar o finalizar manualmente no modifica el hábito ni la racha. Las features consumen `HabitRepository` y nunca acceden a DAOs.

## Migracion y gate

La persistencia usa `habits` y `habit_completions` con migracion no destructiva `4->5`. No se crean cross-references entre features.
