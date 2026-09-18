package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletion
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.HabitId
import kotlinx.coroutines.flow.Flow

data class HabitDraft(
    val name: String,
    val frequency: HabitFrequency,
    val intervalDays: Int = 1,
    val occurrencesPerWeek: Int = 1,
)

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>
    fun observeCompletions(habitId: HabitId): Flow<List<HabitCompletion>>
    suspend fun createHabit(draft: HabitDraft): Result<HabitId>
    suspend fun completeLevel(habitId: HabitId, epochDay: Long, level: HabitCompletionLevel): Result<Unit>
    suspend fun archiveHabit(habitId: HabitId): Result<Unit>
}
