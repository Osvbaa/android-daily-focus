package com.example.dailyfocus.features.calendar.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import com.example.dailyfocus.core.testing.rules.MainDispatcherRule
import com.example.dailyfocus.features.calendar.ui.state.CalendarEvent
import java.time.LocalDate
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class CalendarViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `selecting a date observes that day and restores selection`() = runTest {
            val today = LocalDate.of(2026, 9, 17).toEpochDay()
            val tomorrow = today + 1
            val repository = FakeTaskRepository().apply {
                seed(
                    Task(TaskId("today"), "Hoy", createdAtMillis = 1, dueDateEpochDays = today),
                    Task(TaskId("tomorrow"), "Mañana", createdAtMillis = 2, dueDateEpochDays = tomorrow),
                )
            }
            val savedState = SavedStateHandle()
            val viewModel = CalendarViewModel(repository, DateProvider { LocalDate.of(2026, 9, 17) }, savedState)
            val collecting = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiState.collect { }
            }
            advanceUntilIdle()
            assertEquals(listOf("Hoy"), viewModel.uiState.value.tasks.map { it.title })

            viewModel.onEvent(CalendarEvent.DateSelected(tomorrow))
            advanceUntilIdle()
            assertEquals(tomorrow, viewModel.uiState.value.epochDay)
            assertEquals(listOf("Mañana"), viewModel.uiState.value.tasks.map { it.title })
            assertFalse(viewModel.uiState.value.isLoading)
            assertEquals(tomorrow, savedState.get<Long>(CalendarViewModel.SELECTED_EPOCH_DAY))

            val recreated = CalendarViewModel(repository, DateProvider { LocalDate.of(2026, 9, 17) }, savedState)
            val recreatedCollection = backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
                recreated.uiState.collect { }
            }
            advanceUntilIdle()
            assertEquals(listOf("Mañana"), recreated.uiState.value.tasks.map { it.title })
            collecting.cancel()
            recreatedCollection.cancel()
    }
}
