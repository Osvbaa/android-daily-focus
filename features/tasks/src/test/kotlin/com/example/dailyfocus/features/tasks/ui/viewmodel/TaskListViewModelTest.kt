package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.dailyfocus.core.common.analytics.AnalyticsEvent
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.common.time.MonotonicClock
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.model.*
import com.example.dailyfocus.core.testing.analytics.FakeAnalyticsTracker
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import com.example.dailyfocus.core.testing.repository.FakeProjectRepository
import com.example.dailyfocus.core.testing.repository.FakeActivityRepository
import com.example.dailyfocus.core.testing.repository.FakeFocusSessionRepository
import com.example.dailyfocus.core.testing.rules.MainDispatcherRule
import com.example.dailyfocus.features.tasks.ui.state.TaskListEvent
import com.example.dailyfocus.features.tasks.ui.state.TaskListEffect
import java.time.LocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskListViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()
    private val repo = FakeTaskRepository()
    private val analytics = FakeAnalyticsTracker()
    private val projects = FakeProjectRepository()
    private val activity = FakeActivityRepository()
    private val focusSessions = FakeFocusSessionRepository()
    private val date = LocalDate.of(2026, 9, 10)
    private fun viewModel() = TaskListViewModel(repo, analytics, DateProvider { date }, WallClock { 10_000 }, MonotonicClock { 100 }, SavedStateHandle(), projects, activity, focusSessions)

    @Test fun `blank capture does not persist`() = runTest {
        val vm = viewModel(); vm.onEvent(TaskListEvent.CaptureChanged("   ")); vm.onEvent(TaskListEvent.CaptureSubmitted); advanceUntilIdle()
        assertTrue(vm.uiState.value.tasks.isEmpty())
    }

    @Test fun `quick capture is trimmed and due today`() = runTest {
        val vm = viewModel(); vm.onEvent(TaskListEvent.CaptureChanged("  Revisar PR  ")); vm.onEvent(TaskListEvent.CaptureSubmitted); advanceUntilIdle()
        assertEquals("Revisar PR", vm.uiState.value.tasks.single().title)
        assertEquals(date.toEpochDay(), vm.uiState.value.tasks.single().dueDateEpochDays)
    }

    @Test fun `create request opens the editor without a task ID`() = runTest {
        val vm = viewModel()

        vm.onEvent(TaskListEvent.CreateRequested)

        assertEquals(TaskListEffect.NavigateToEditor(), vm.effects.first())
    }

    @Test fun `pending subtasks require resolution and undo restores exact snapshot`() = runTest {
        val id = TaskId("task")
        repo.seed(Task(id, "Grupo", createdAtMillis = 0, subtasks = persistentListOf(Subtask(SubtaskId("sub"), id, "Paso", false, 0))))
        val vm = viewModel(); advanceUntilIdle(); vm.onEvent(TaskListEvent.CompleteRequested(id)); advanceUntilIdle()
        assertEquals(id, vm.uiState.value.pendingSubtaskResolution)
        assertFalse(repo.getTask(id)!!.isCompleted)
        vm.onEvent(TaskListEvent.CompleteAll(id)); advanceTimeBy(1); vm.onEvent(TaskListEvent.UndoCompletion); advanceUntilIdle()
        assertFalse(repo.getTask(id)!!.isCompleted)
        assertFalse(repo.getTask(id)!!.subtasks.single().isCompleted)
        assertTrue(analytics.events.none { it is AnalyticsEvent.TaskCompleted })
    }
    @Test fun `undo request targets its operation when completion windows overlap`() = runTest {
        val first = TaskId("first")
        val second = TaskId("second")
        repo.seed(
            Task(first, "Primera", createdAtMillis = 0),
            Task(second, "Segunda", createdAtMillis = 1),
        )
        val vm = viewModel()
        advanceUntilIdle()

        vm.onEvent(TaskListEvent.CompleteAll(first))
        runCurrent()
        val firstOperation = repo.observeUndoOperations().first().single { it.taskId == first }

        vm.onEvent(TaskListEvent.CompleteAll(second))
        runCurrent()
        val secondOperation = repo.observeUndoOperations().first().single { it.taskId == second }

        vm.onEvent(TaskListEvent.UndoRequested(firstOperation.operationId))
        runCurrent()

        assertFalse(requireNotNull(repo.getTask(first)).isCompleted)
        assertTrue(requireNotNull(repo.getTask(second)).isCompleted)
        assertEquals(listOf(secondOperation.operationId), repo.observeUndoOperations().first().map { it.operationId })
    }
}
