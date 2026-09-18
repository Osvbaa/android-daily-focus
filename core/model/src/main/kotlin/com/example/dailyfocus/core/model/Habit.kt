package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class HabitId(val value: String)

@Serializable
enum class HabitFrequency { DAILY, WEEKLY, TIMES_PER_WEEK, EVERY_N_DAYS }

@Serializable
enum class HabitCompletionLevel { MINI, PLUS, ELITE }

@Serializable
data class Habit(
    val id: HabitId,
    val name: String,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    /** Used only for EVERY_N_DAYS; must be >= 1 when that frequency is selected. */
    val intervalDays: Int = 1,
    /** Used only for TIMES_PER_WEEK; must be between 1 and 7 when selected. */
    val occurrencesPerWeek: Int = 1,
    val targetLevel: HabitCompletionLevel = HabitCompletionLevel.ELITE,
    val createdEpochDay: Long = 0L,
    val isArchived: Boolean = false,
)

@Serializable
data class HabitCompletion(val habitId: HabitId, val epochDay: Long, val level: HabitCompletionLevel)
