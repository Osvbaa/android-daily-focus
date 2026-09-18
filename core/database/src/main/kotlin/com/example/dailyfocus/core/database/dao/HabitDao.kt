package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.dailyfocus.core.database.model.HabitCompletionEntity
import com.example.dailyfocus.core.database.model.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY is_archived ASC, name ASC")
    fun observeHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habit_completions WHERE habit_id = :habitId ORDER BY epoch_day DESC")
    fun observeCompletions(habitId: String): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE epoch_day BETWEEN :fromEpochDay AND :toEpochDay")
    suspend fun getCompletions(fromEpochDay: Long, toEpochDay: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabit(id: String): HabitEntity?

    @Upsert
    suspend fun upsertHabit(habit: HabitEntity)

    @Upsert
    suspend fun upsertCompletion(completion: HabitCompletionEntity)

    @Query("UPDATE habits SET is_archived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean): Int
}
