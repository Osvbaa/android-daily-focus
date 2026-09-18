package com.example.dailyfocus.features.calendar.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.features.calendar.ui.state.CalendarEvent
import com.example.dailyfocus.features.calendar.ui.state.CalendarUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val dates: DateProvider,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val selectedEpochDay = MutableStateFlow(savedStateHandle[SELECTED_EPOCH_DAY] ?: dates.today().toEpochDay())

    val uiState: StateFlow<CalendarUiState> = selectedEpochDay.flatMapLatest { epochDay ->
        repository.observeTasksForDay(epochDay)
            .map { tasks -> CalendarUiState(tasks.toPersistentList(), epochDay, isLoading = false) }
            .onStart { emit(CalendarUiState(epochDay = epochDay)) }
            .catch { error ->
                emit(CalendarUiState(epochDay = epochDay, isLoading = false, errorMessage = error.message ?: "No se pudo cargar la fecha"))
            }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CalendarUiState(epochDay = selectedEpochDay.value),
    )

    fun onEvent(event: CalendarEvent) {
        val epochDay = when (event) {
            is CalendarEvent.DateSelected -> event.epochDay
            CalendarEvent.TodayRequested -> dates.today().toEpochDay()
        }
        savedStateHandle[SELECTED_EPOCH_DAY] = epochDay
        selectedEpochDay.value = epochDay
    }

    companion object {
        const val SELECTED_EPOCH_DAY = "calendar_selected_epoch_day"
    }
}
