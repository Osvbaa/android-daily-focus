package com.example.dailyfocus.features.today.ui.viewmodel

import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.StreakEventType
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.testing.repository.FakeActivityRepository
import com.example.dailyfocus.core.testing.repository.FakeHabitRepository
import com.example.dailyfocus.core.testing.repository.FakeProjectRepository
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import com.example.dailyfocus.core.testing.rules.MainDispatcherRule
import com.example.dailyfocus.features.today.ui.state.TodayEvent
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val date = LocalDate.of(2026, 9, 10)
    private val tasks = FakeTaskRepository()
    private val projects = FakeProjectRepository()
    private val habits = FakeHabitRepository()
    private val activity = FakeActivityRepository()

    private fun viewModel() = TodayViewModel(tasks, projects, habits, activity, DateProvider { date })

    @Test fun `shows overdue and due today tasks plus eligible habits`() = runTest {
        val project = Project(ProjectId("project"), "Entrega", createdAtMillis = 0)
        projects.seed(project)
        tasks.seed(
            Task(TaskId("overdue"), "Atrasada", projectId = project.id, createdAtMillis = 0, dueDateEpochDays = date.minusDays(1).toEpochDay()),
            Task(TaskId("today"), "Hoy", projectId = project.id, createdAtMillis = 1, dueDateEpochDays = date.toEpochDay()),
            Task(TaskId("future"), "Después", createdAtMillis = 2, dueDateEpochDays = date.plusDays(1).toEpochDay()),
        )
        habits.seed(
            Habit(HabitId("daily"), "Caminar", HabitFrequency.DAILY, createdEpochDay = date.minusDays(2).toEpochDay()),
            Habit(HabitId("not-due"), "Leer", HabitFrequency.EVERY_N_DAYS, intervalDays = 3, createdEpochDay = date.minusDays(1).toEpochDay()),
        )

        val viewModel = viewModel()
        val state = viewModel.uiState.first { !it.isLoading }

        assertEquals(listOf("overdue", "today"), state.tasks.map { it.id.value })
        assertEquals(date.toEpochDay(), state.todayEpochDay)
        assertEquals(com.example.dailyfocus.features.today.ui.state.TodayPanel.TASKS, state.expandedPanel)
        assertEquals(2, state.projects.single().pendingTaskCount)
        assertEquals(listOf("Caminar"), state.habits.map { it.name })
    }

    @Test fun `quick task keeps project focus estimate and today due date`() = runTest {
        val project = Project(ProjectId("project"), "Entrega", createdAtMillis = 0)
        projects.seed(project)
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(TodayEvent.QuickTitleChanged("Preparar demo"))
        viewModel.onEvent(TodayEvent.QuickPriorityChanged(Priority.HIGH))
        viewModel.onEvent(TodayEvent.QuickProjectChanged(project.id))
        viewModel.onEvent(TodayEvent.QuickDurationChanged("40"))
        viewModel.onEvent(TodayEvent.QuickTaskSubmitted)
        advanceUntilIdle()

        val task = tasks.observeTasks().first().single()
        assertEquals(date.toEpochDay(), task.dueDateEpochDays)
        assertEquals(project.id, task.projectId)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(40 * 60L, task.estimatedDurationSeconds)
    }

    @Test fun `task completion records a project task streak event`() = runTest {
        val project = Project(ProjectId("project"), "Entrega", createdAtMillis = 0)
        projects.seed(project)
        val taskId = TaskId("today")
        tasks.seed(Task(taskId, "Hoy", projectId = project.id, createdAtMillis = 0, dueDateEpochDays = date.toEpochDay()))
        val viewModel = viewModel()
        advanceUntilIdle()

        viewModel.onEvent(TodayEvent.TaskCompletionRequested(taskId))
        advanceUntilIdle()

        assertTrue(tasks.getTask(taskId)!!.isCompleted)
        assertTrue(activity.observeEvents().first().any { it.type == StreakEventType.PROJECT_TASK && it.sourceId == taskId.value })
        assertFalse(viewModel.uiState.value.tasks.any { it.id == taskId && !it.isCompleted })
    }

    @Test fun `toggle stats expands and collapses stats metrics`() = runTest {
        val viewModel = viewModel()
        backgroundScope.launch {
            viewModel.uiState.collect {}
        }
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isStatsExpanded)
        viewModel.onEvent(TodayEvent.ToggleStatsRequested)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isStatsExpanded)
        viewModel.onEvent(TodayEvent.ToggleStatsRequested)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isStatsExpanded)
    }

    @Test fun `projects requested emits navigate to projects effect`() = runTest {
        val viewModel = viewModel()
        viewModel.onEvent(TodayEvent.ProjectsRequested)
        assertEquals(com.example.dailyfocus.features.today.ui.state.TodayEffect.NavigateToProjects, viewModel.effects.first())
    }

    @Test fun `counts pomodoro tasks correctly`() = runTest {
        tasks.seed(
            Task(TaskId("t1"), "Con Pomodoro", createdAtMillis = 0, dueDateEpochDays = date.toEpochDay(), estimatedDurationSeconds = 1500L),
            Task(TaskId("t2"), "Sin Pomodoro", createdAtMillis = 1, dueDateEpochDays = date.toEpochDay(), estimatedDurationSeconds = null),
            Task(TaskId("t3"), "Con Pomodoro 2", createdAtMillis = 2, dueDateEpochDays = date.toEpochDay(), estimatedDurationSeconds = 1800L),
        )
        val viewModel = viewModel()
        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(2, state.pomodoroTasks)
    }

    @Test fun `quick capture validation error remains visible until title changes`() = runTest {
        val viewModel = viewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onEvent(TodayEvent.QuickTaskSubmitted)
        advanceUntilIdle()
        assertEquals("Escribe un título para la tarea", viewModel.uiState.value.errorMessage)

        viewModel.onEvent(TodayEvent.QuickTitleChanged("Preparar demo"))
        advanceUntilIdle()
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }
}
