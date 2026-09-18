package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskUndoResult
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskDraftRecord
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.TaskReminderSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTaskRepositoryDurableTest {
    @Test
    fun `draft survives reads and is removed only when explicitly deleted`() = runTest {
        val repository = FakeTaskRepository()
        val draft = TaskDraft(title = "  Planificar semana  ", description = "  Tres objetivos  ")

        assertTrue(repository.saveDraft(TaskDraftRecord("new-task", draft = draft)) is TaskMutationResult.Success)
        assertEquals("Planificar semana", repository.getDraft("new-task")?.draft?.title)
        assertEquals("Tres objetivos", repository.getDraft("new-task")?.draft?.description)

        repository.deleteDraft("new-task")

        assertEquals(null, repository.getDraft("new-task"))
    }

    @Test
    fun `stale editor revision is rejected without replacing the newer task`() = runTest {
        val repository = FakeTaskRepository()
        val id = (repository.saveTask(null, TaskDraft(title = "Original")) as TaskMutationResult.Success).taskId
        val revision = requireNotNull(repository.getTask(id)).revision

        assertTrue(repository.saveTask(id, TaskDraft(title = "Nuevo"), revision) is TaskMutationResult.Success)
        assertTrue(repository.saveTask(id, TaskDraft(title = "Obsoleto"), revision) is TaskMutationResult.Failure)

        assertEquals("Nuevo", repository.getTask(id)?.title)
        assertEquals(revision + 1L, repository.getTask(id)?.revision)
    }

    @Test
    fun `undo restores the aggregate exactly once before its deadline`() = runTest {
        val repository = FakeTaskRepository().apply {
            seed(Task(TaskId("task"), "Pendiente", createdAtMillis = 0))
        }

        val operation = repository.completeTaskWithUndo(
            id = TaskId("task"),
            includeSubtasks = true,
            operationId = "undo-1",
            nowElapsedMillis = 100,
            windowMillis = 50,
        ).getOrThrow()

        assertEquals(150L, operation.expiresAtElapsedMillis)
        assertTrue(requireNotNull(repository.getTask(TaskId("task"))).isCompleted)
        assertEquals(TaskUndoResult.Success(TaskId("task")), repository.restoreUndo("undo-1", 149))
        assertFalse(requireNotNull(repository.getTask(TaskId("task"))).isCompleted)
        assertEquals(TaskUndoResult.NotFound, repository.restoreUndo("undo-1", 149))
        assertTrue(repository.observeUndoOperations().first().isEmpty())
    }

    @Test
    fun `undo is consumed as expired after deadline or elapsed clock reset`() = runTest {
        val repository = FakeTaskRepository().apply {
            seed(Task(TaskId("task"), "Pendiente", createdAtMillis = 0))
        }
        repository.completeTaskWithUndo(TaskId("task"), true, "expired", 100, 50).getOrThrow()

        assertEquals(TaskUndoResult.Expired, repository.restoreUndo("expired", 150))
        assertTrue(repository.consumeExpiredUndo(150).isEmpty())

        repository.completeTaskWithUndo(TaskId("task"), true, "reboot", 500, 50).getOrThrow()

        val recovered = repository.consumeExpiredUndo(100)
        assertEquals(listOf("reboot"), recovered.map { it.operationId })
        assertEquals(TaskUndoResult.NotFound, repository.restoreUndo("reboot", 500))
    }

    @Test
    fun `undo reports conflict when the aggregate changed after completion`() = runTest {
        val repository = FakeTaskRepository().apply {
            seed(Task(TaskId("task"), "Pendiente", createdAtMillis = 0))
        }
        repository.completeTaskWithUndo(TaskId("task"), true, "conflict", 100, 50).getOrThrow()
        repository.rescheduleTask(TaskId("task"), 42)

        assertEquals(TaskUndoResult.Conflict, repository.restoreUndo("conflict", 120))
        assertEquals(listOf("conflict"), repository.observeUndoOperations().first().map { it.operationId })
    }

    @Test
    fun `reminder settings are durable and observable`() = runTest {
        val repository = FakeTaskRepository()
        val settings = TaskReminderSettings(enabled = true, hour = 8, minute = 30, lastPublishedEpochDay = 12)

        repository.saveReminderSettings(settings)

        assertEquals(settings, repository.getReminderSettings())
        assertEquals(settings, repository.observeReminderSettings().first())
    }
}
