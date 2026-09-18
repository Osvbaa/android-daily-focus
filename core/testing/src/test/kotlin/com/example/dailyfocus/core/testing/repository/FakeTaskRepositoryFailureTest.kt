package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeTaskRepositoryFailureTest {
    @Test
    fun cancellationEscapesMutationAndCompletion() = runTest {
        val cancelled = CancellationException("Cancelled by test")
        val repository = FakeTaskRepository().apply { failure = cancelled }

        val saving = runCatching { repository.saveTask(null, TaskDraft(title = "Draft")) }
        val completing = runCatching { repository.completeTask(TaskId("missing"), true) }

        assertSame(cancelled, saving.exceptionOrNull())
        assertSame(cancelled, completing.exceptionOrNull())
        assertTrue(repository.observeTasks().first().isEmpty())
    }

    @Test
    fun injectedWriteFailureDoesNotPublishChanges() = runTest {
        val repository = FakeTaskRepository().apply { failure = IllegalStateException("Disk failure") }

        assertTrue(repository.saveTask(null, TaskDraft(title = "Draft")) is TaskMutationResult.Failure)
        assertTrue(repository.observeTasks().first().isEmpty())
    }

    @Test
    fun readFailureAffectsQueriesWithoutChangingStoredData() = runTest {
        val failure = IllegalStateException("Read failure")
        val repository = FakeTaskRepository().apply {
            seed(Task(TaskId("task"), "Stored", createdAtMillis = 0))
            readFailure = failure
        }

        assertSame(failure, runCatching { repository.getTask(TaskId("task")) }.exceptionOrNull())
        assertSame(failure, runCatching { repository.observeTasks().first() }.exceptionOrNull())
        repository.readFailure = null
        assertEquals("Stored", repository.getTask(TaskId("task"))?.title)
    }

    @Test
    fun concurrentCreationsKeepBothTasksAndAvoidSeededIds() = runTest {
        val repository = FakeTaskRepository(latencyMillis = 100).apply {
            seed(Task(TaskId("fake-1"), "Seed", createdAtMillis = 0))
        }
        val results = listOf(
            async { repository.saveTask(null, TaskDraft(title = "A")) },
            async { repository.saveTask(null, TaskDraft(title = "B")) },
        ).awaitAll()

        assertTrue(results.all { it is TaskMutationResult.Success })
        assertEquals(setOf("Seed", "A", "B"), repository.observeTasks().first().map { it.title }.toSet())
    }
}
