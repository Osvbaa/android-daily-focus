package com.example.dailyfocus.features.habits.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.data.repository.HabitDraft
import com.example.dailyfocus.core.data.repository.HabitRepository
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.StreakEventType
import com.example.dailyfocus.features.habits.ui.state.HabitListEvent
import com.example.dailyfocus.features.habits.ui.state.HabitListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HabitListViewModel @Inject constructor(
    private val repository: HabitRepository,
    private val dates: DateProvider,
    private val activityRepository: ActivityRepository,
) : ViewModel() {
    private val localState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = repository.observeHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
        .let { habits ->
            combine(habits, localState) { models, local ->
                local.copy(isLoading = false, habits = models.toPersistentList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HabitListUiState())

    fun onEvent(event: HabitListEvent) {
        when (event) {
            is HabitListEvent.NameChanged -> localState.update { it.copy(name = event.value, errorMessage = null) }
            is HabitListEvent.FrequencyChanged -> localState.update { it.copy(frequency = event.value) }
            is HabitListEvent.IntervalChanged -> localState.update { it.copy(intervalDays = event.value) }
            is HabitListEvent.OccurrencesChanged -> localState.update { it.copy(occurrencesPerWeek = event.value) }
            HabitListEvent.Create -> create()
            is HabitListEvent.Complete -> complete(event)
            is HabitListEvent.Archive -> archive(HabitId(event.habitId))
        }
    }

    private fun create() {
        val state = localState.value
        viewModelScope.launch {
            val result = repository.createHabit(
                HabitDraft(
                    name = state.name,
                    frequency = state.frequency,
                    intervalDays = state.intervalDays.toIntOrNull() ?: 0,
                    occurrencesPerWeek = state.occurrencesPerWeek.toIntOrNull() ?: 0,
                ),
            )
            result.onSuccess { localState.update { it.copy(name = "", errorMessage = null) } }
                .onFailure { error -> localState.update { it.copy(errorMessage = error.message ?: "No se pudo crear el habito") } }
        }
    }

    private fun complete(event: HabitListEvent.Complete) {
        viewModelScope.launch {
            val epochDay = dates.today().toEpochDay()
            repository.completeLevel(HabitId(event.habitId), epochDay, event.level)
                .onSuccess {
                    activityRepository.recordCompletion(
                        type = StreakEventType.HABIT_LEVEL,
                        sourceId = event.habitId,
                        epochDay = epochDay,
                        habitLevel = event.level,
                    )
                }
                .onFailure { error -> localState.update { it.copy(errorMessage = error.message ?: "No se pudo registrar el habito") } }
        }
    }

    private fun archive(id: HabitId) {
        viewModelScope.launch {
            repository.archiveHabit(id).onFailure { error ->
                localState.update { it.copy(errorMessage = error.message ?: "No se pudo archivar el habito") }
            }
        }
    }
}
