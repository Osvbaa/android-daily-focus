package com.example.dailyfocus.features.notes.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.AiExtractionResult
import com.example.dailyfocus.core.model.Note
import com.example.dailyfocus.core.model.TaskSuggestion
import com.example.dailyfocus.core.testing.ai.FakeAiExtractionEngine
import com.example.dailyfocus.core.testing.repository.FakeNoteRepository
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.features.notes.ui.state.NoteEditorEvent
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteEditorViewModelTest {
    @Test
    fun `failed note save leaves suggestions pending and creates no tasks`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val storedNotes = FakeNoteRepository()
            var rejectSave = true
            val notes = object : NoteRepository by storedNotes {
                override suspend fun upsertNote(id: String?, title: String, content: String): String {
                    if (rejectSave) error("storage unavailable")
                    return storedNotes.upsertNote(id, title, content)
                }
            }
            val tasks = FakeTaskRepository()
            val ai = FakeAiExtractionEngine(AiAvailability.Available(com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO))
            ai.nextResult = AiExtractionResult.Success(
                provider = com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO,
                suggestions = listOf(TaskSuggestion("s1", "Preparar reporte", confidence = 0.9f)),
                latencyMs = 1,
            )
            val viewModel = NoteEditorViewModel(notes, tasks, ai, SavedStateHandle())
            advanceUntilIdle()
            viewModel.onEvent(NoteEditorEvent.ContentChanged("Preparar reporte"))
            viewModel.onEvent(NoteEditorEvent.ExtractTasks)
            advanceUntilIdle()
            viewModel.onEvent(NoteEditorEvent.SuggestionSelected("s1", true))
            viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
            advanceUntilIdle()

            assertTrue(storedNotes.observeAllNotes().first().isEmpty())
            assertTrue(tasks.observeTasks().first().isEmpty())
            assertEquals(setOf("s1"), viewModel.uiState.value.selectedSuggestionIds.toSet())

            rejectSave = false
            viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
            advanceUntilIdle()
            assertEquals(storedNotes.observeAllNotes().first().single().id, tasks.observeTasks().first().single().linkedNoteId)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `retry after one task fails does not duplicate the task already created`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val notes = FakeNoteRepository()
            val storedTasks = FakeTaskRepository()
            var failSecond = true
            val tasks = object : TaskRepository by storedTasks {
                override suspend fun createTaskFromNote(title: String, noteId: String): TaskMutationResult {
                    if (title == "Llamar al cliente" && failSecond) {
                        return TaskMutationResult.Failure(IllegalStateException("storage unavailable"))
                    }
                    return storedTasks.createTaskFromNote(title, noteId)
                }
            }
            val ai = FakeAiExtractionEngine(AiAvailability.Available(com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO))
            ai.nextResult = AiExtractionResult.Success(
                provider = com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO,
                suggestions = listOf(
                    TaskSuggestion("s1", "Preparar reporte", confidence = 0.9f),
                    TaskSuggestion("s2", "Llamar al cliente", confidence = 0.8f),
                ),
                latencyMs = 1,
            )
            val savedState = SavedStateHandle()
            val viewModel = NoteEditorViewModel(notes, tasks, ai, savedState)
            advanceUntilIdle()
            viewModel.onEvent(NoteEditorEvent.ContentChanged("Preparar reporte y llamar al cliente"))
            viewModel.onEvent(NoteEditorEvent.ExtractTasks)
            advanceUntilIdle()
            viewModel.onEvent(NoteEditorEvent.SuggestionSelected("s1", true))
            viewModel.onEvent(NoteEditorEvent.SuggestionSelected("s2", true))
            viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
            advanceUntilIdle()

            assertEquals(listOf("Preparar reporte"), storedTasks.observeTasks().first().map { it.title })
            assertEquals(setOf("s2"), viewModel.uiState.value.selectedSuggestionIds.toSet())

            failSecond = false
            viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
            advanceUntilIdle()
            assertEquals(setOf("Preparar reporte", "Llamar al cliente"), storedTasks.observeTasks().first().map { it.title }.toSet())
            assertTrue(viewModel.uiState.value.suggestions.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `confirming suggestions on a new note persists origin before creating tasks`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val notes = FakeNoteRepository()
            val tasks = FakeTaskRepository()
            val ai = FakeAiExtractionEngine(AiAvailability.Available(com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO))
            ai.nextResult = AiExtractionResult.Success(
                provider = com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO,
                suggestions = listOf(TaskSuggestion("s1", "Preparar reporte", confidence = 0.9f)),
                latencyMs = 1,
            )
            val savedState = SavedStateHandle()
            val viewModel = NoteEditorViewModel(notes, tasks, ai, savedState)
            advanceUntilIdle()
            viewModel.onEvent(NoteEditorEvent.TitleChanged("Plan"))
            viewModel.onEvent(NoteEditorEvent.ContentChanged("Preparar reporte"))
            viewModel.onEvent(NoteEditorEvent.ExtractTasks)
            advanceUntilIdle()
            assertTrue(notes.observeAllNotes().first().isEmpty())
            assertTrue(tasks.observeTasks().first().isEmpty())

            viewModel.onEvent(NoteEditorEvent.SuggestionSelected("s1", true))
            viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
            advanceUntilIdle()

            val note = notes.observeAllNotes().first().single()
            val task = tasks.observeTasks().first().single()
            assertEquals(note.id, task.linkedNoteId)
            assertEquals(note.id, viewModel.uiState.value.noteId)
            assertEquals(note.id, savedState.get<String>(NoteEditorViewModel.NOTE_ID))
            assertTrue(viewModel.uiState.value.suggestions.isEmpty())

            val recreated = NoteEditorViewModel(notes, tasks, ai, savedState)
            advanceUntilIdle()
            assertEquals(note.id, recreated.uiState.value.noteId)
            assertEquals("Preparar reporte", recreated.uiState.value.content)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `confirming one suggestion creates only selected linked task`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
        val notes = FakeNoteRepository()
        notes.upsertNote("note-1", "Plan", "Preparar reporte y llamar al cliente")
        val tasks = FakeTaskRepository()
        val ai = FakeAiExtractionEngine(AiAvailability.Available(com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO))
        ai.nextResult = AiExtractionResult.Success(
            provider = com.example.dailyfocus.core.model.AiProvider.GEMINI_NANO,
            suggestions = listOf(
                TaskSuggestion("s1", "Preparar reporte", confidence = 0.9f),
                TaskSuggestion("s2", "Llamar al cliente", confidence = 0.8f),
            ),
            latencyMs = 1,
        )
        val viewModel = NoteEditorViewModel(
            repository = notes,
            taskRepository = tasks,
            aiEngine = ai,
            savedStateHandle = SavedStateHandle(mapOf(NoteEditorViewModel.NOTE_ID to "note-1")),
        )
        advanceUntilIdle()
        runCurrent()

        viewModel.onEvent(NoteEditorEvent.ExtractTasks)
        advanceUntilIdle()
        runCurrent()
        assertEquals(2, viewModel.uiState.value.suggestions.size)
        viewModel.onEvent(NoteEditorEvent.SuggestionSelected("s1", true))
        viewModel.onEvent(NoteEditorEvent.ConfirmSuggestions)
        advanceUntilIdle()

        val created = tasks.observeTasks().first()
        assertEquals(1, created.size)
        assertEquals("Preparar reporte", created.single().title)
        assertEquals("note-1", created.single().linkedNoteId)
        assertNotNull(viewModel.uiState.value)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
