package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.model.TaskId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTaskRepositoryNoteOriginTest {
    @Test
    fun `creates task linked to note`() = runTest {
        val repository = FakeTaskRepository()

        val result = repository.createTaskFromNote("Enviar reporte", "note-1")

        val taskId = (result as TaskMutationResult.Success).taskId
        assertEquals("note-1", repository.getTask(taskId)?.linkedNoteId)
        assertEquals("Enviar reporte", repository.getTask(taskId)?.title)
    }

    @Test
    fun `rejects blank task from note without publishing`() = runTest {
        val repository = FakeTaskRepository()

        val result = repository.createTaskFromNote("   ", "note-1")

        assertTrue(result is TaskMutationResult.Failure)
        assertTrue(repository.observeTasks().first().isEmpty())
    }
}
