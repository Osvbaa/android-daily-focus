package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.HabitDraft
import com.example.dailyfocus.core.data.repository.HabitRepository
import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletion
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Small deterministic fake for feature behaviour tests. */
class FakeHabitRepository : HabitRepository {
    private val habits = MutableStateFlow<List<Habit>>(emptyList())
    private val completions = MutableStateFlow<List<HabitCompletion>>(emptyList())
    private var nextId = 0

    fun seed(vararg values: Habit) {
        habits.value = values.toList()
    }

    override fun observeHabits(): Flow<List<Habit>> = habits

    override fun observeCompletions(habitId: HabitId): Flow<List<HabitCompletion>> =
        completions.map { values -> values.filter { it.habitId == habitId } }

    override suspend fun createHabit(draft: HabitDraft): Result<HabitId> = runCatching {
        require(draft.name.isNotBlank()) { "Habit name must not be blank" }
        HabitId("habit-${++nextId}").also { id ->
            habits.value += Habit(
                id = id,
                name = draft.name.trim(),
                frequency = draft.frequency,
                intervalDays = draft.intervalDays,
                occurrencesPerWeek = draft.occurrencesPerWeek,
            )
        }
    }

    override suspend fun completeLevel(
        habitId: HabitId,
        epochDay: Long,
        level: HabitCompletionLevel,
    ): Result<Unit> = runCatching {
        check(habits.value.any { it.id == habitId && !it.isArchived }) { "Habit not found" }
        val current = completions.value.firstOrNull { it.habitId == habitId && it.epochDay == epochDay }
        check(current == null || level.ordinal >= current.level.ordinal) { "Habit level cannot be reduced" }
        completions.value = completions.value.filterNot { it.habitId == habitId && it.epochDay == epochDay } +
            HabitCompletion(habitId, epochDay, level)
    }

    override suspend fun archiveHabit(habitId: HabitId): Result<Unit> = runCatching {
        check(habits.value.any { it.id == habitId }) { "Habit not found" }
        habits.value = habits.value.map { habit -> if (habit.id == habitId) habit.copy(isArchived = true) else habit }
    }
}
