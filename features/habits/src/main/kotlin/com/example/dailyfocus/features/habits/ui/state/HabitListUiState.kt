package com.example.dailyfocus.features.habits.ui.state

import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class HabitListUiState(
    val isLoading: Boolean = true,
    val habits: ImmutableList<Habit> = persistentListOf(),
    val name: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val intervalDays: String = "2",
    val occurrencesPerWeek: String = "3",
    val errorMessage: String? = null,
)

sealed interface HabitListEvent {
    data class NameChanged(val value: String) : HabitListEvent
    data class FrequencyChanged(val value: HabitFrequency) : HabitListEvent
    data class IntervalChanged(val value: String) : HabitListEvent
    data class OccurrencesChanged(val value: String) : HabitListEvent
    data object Create : HabitListEvent
    data class Complete(val habitId: String, val level: HabitCompletionLevel) : HabitListEvent
    data class Archive(val habitId: String) : HabitListEvent
}
