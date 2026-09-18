package com.example.dailyfocus.features.calendar.ui.state

import com.example.dailyfocus.core.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CalendarUiState(
    val tasks: ImmutableList<Task> = persistentListOf(),
    val epochDay: Long = 0L,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface CalendarEvent {
    data class DateSelected(val epochDay: Long) : CalendarEvent
    data object TodayRequested : CalendarEvent
}
