package com.example.dailyfocus.features.tasks.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import com.example.dailyfocus.core.testing.repository.FakeProjectRepository
import com.example.dailyfocus.core.testing.rules.MainDispatcherRule
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEffect
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorEvent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskEditorViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `blank title requests focus and does not save`() = runTest {
        val repository = FakeTaskRepository()
        val viewModel = editor(repository)

        viewModel.onEvent(TaskEditorEvent.Save)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.titleError)
        assertTrue(repository.getTask(TaskId("fake-1")) == null)
        assertEquals(TaskEditorEffect.RequestTitleFocus, viewModel.effects.first())
    }

    @Test
    fun `saving editor persists subtasks with consecutive positions`() = runTest {
        val repository = FakeTaskRepository()
        val viewModel = editor(repository)

        viewModel.onEvent(TaskEditorEvent.TitleChanged("Parent"))
        viewModel.onEvent(TaskEditorEvent.DueDateChanged(20_000L))
        viewModel.onEvent(TaskEditorEvent.PriorityChanged(Priority.HIGH))
        viewModel.onEvent(TaskEditorEvent.AddSubtask)
        viewModel.onEvent(TaskEditorEvent.AddSubtask)
        viewModel.onEvent(TaskEditorEvent.SubtaskChanged(0, "First"))
        viewModel.onEvent(TaskEditorEvent.SubtaskChanged(1, "Second"))
        viewModel.onEvent(TaskEditorEvent.MoveSubtask(1, 0))
        viewModel.onEvent(TaskEditorEvent.Save)
        advanceUntilIdle()

        val saved = repository.getTask(TaskId("fake-1"))
        assertEquals(listOf("Second", "First"), saved?.subtasks?.map(Subtask::title))
        assertEquals(listOf(0, 1), saved?.subtasks?.map(Subtask::position))
        assertEquals(20_000L, saved?.dueDateEpochDays)
        assertEquals(Priority.HIGH, saved?.priority)
    }

    @Test
    fun `existing task is loaded into the draft`() = runTest {
        val taskId = TaskId("task-1")
        val repository = FakeTaskRepository().apply {
            seed(
                Task(
                    id = taskId,
                    title = "Parent",
                    description = "Details",
                    createdAtMillis = 1L,
                    subtasks = persistentListOf(
                        Subtask(SubtaskId("sub-1"), taskId, "Child", position = 0),
                    ),
                ),
            )
        }

        val viewModel = editor(repository, SavedStateHandle(mapOf("taskId" to taskId.value)))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Parent", viewModel.uiState.value.draft.title)
        assertEquals("Child", viewModel.uiState.value.draft.subtasks.single().title)
    }

    @Test
    fun `full draft survives editor recreation`() = runTest {
        val repository = FakeTaskRepository()
        val savedStateHandle = SavedStateHandle()
        val original = editor(repository, savedStateHandle)

        original.onEvent(TaskEditorEvent.TitleChanged("Plan semanal"))
        original.onEvent(TaskEditorEvent.DescriptionChanged("Preparar prioridades"))
        original.onEvent(TaskEditorEvent.DueDateChanged(20_123L))
        original.onEvent(TaskEditorEvent.PriorityChanged(Priority.URGENT))
        original.onEvent(TaskEditorEvent.AddSubtask)
        original.onEvent(TaskEditorEvent.AddSubtask)
        original.onEvent(TaskEditorEvent.SubtaskChanged(0, "Primero"))
        original.onEvent(TaskEditorEvent.SubtaskChanged(1, "Segundo"))
        original.onEvent(TaskEditorEvent.ToggleSubtask(1))
        original.onEvent(TaskEditorEvent.MoveSubtask(1, 0))

        val recreated = editor(repository, savedStateHandle)
        advanceUntilIdle()

        assertTrue(recreated.uiState.value.isDirty)
        assertEquals("Plan semanal", recreated.uiState.value.draft.title)
        assertEquals("Preparar prioridades", recreated.uiState.value.draft.description)
        assertEquals(20_123L, recreated.uiState.value.draft.dueDateEpochDays)
        assertEquals(Priority.URGENT, recreated.uiState.value.draft.priority)
        assertEquals(listOf("Segundo", "Primero"), recreated.uiState.value.draft.subtasks.map(Subtask::title))
        assertEquals(listOf(true, false), recreated.uiState.value.draft.subtasks.map(Subtask::isCompleted))
        assertEquals(listOf(0, 1), recreated.uiState.value.draft.subtasks.map(Subtask::position))
    }

    @Test
    fun `discard clears the persisted draft`() = runTest {
        val repository = FakeTaskRepository()
        val savedStateHandle = SavedStateHandle()
        val original = editor(repository, savedStateHandle)

        original.onEvent(TaskEditorEvent.TitleChanged("No conservar"))
        original.onEvent(TaskEditorEvent.DueDateChanged(20_123L))
        original.onEvent(TaskEditorEvent.PriorityChanged(Priority.HIGH))
        original.onEvent(TaskEditorEvent.Discard)

        val recreated = editor(repository, savedStateHandle)
        advanceUntilIdle()

        assertEquals("", recreated.uiState.value.draft.title)
        assertEquals(null, recreated.uiState.value.draft.dueDateEpochDays)
        assertEquals(Priority.NONE, recreated.uiState.value.draft.priority)
        assertTrue(recreated.uiState.value.draft.subtasks.isEmpty())
    }

    private fun editor(
        repository: FakeTaskRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle(),
    ): TaskEditorViewModel {
        var sequence = 0
        return TaskEditorViewModel(
            repository = repository,
            ids = IdGenerator { "generated-id-${++sequence}" },
            savedStateHandle = savedStateHandle,
            projectRepository = FakeProjectRepository(),
        )
    }
}
