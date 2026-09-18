package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.TaskMutationResult
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskId
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Behavioral tests inherited by both the manual fake and the Room-backed repository. */
abstract class TaskRepositoryContract {
    protected abstract val repository: TaskRepository

    @Test
    fun blankTitleIsRejectedWithoutPublishing() = runTest {
        assertTrue(repository.saveTask(null, TaskDraft(title = "   ")) is TaskMutationResult.Failure)
        assertTrue(repository.observeTasks().first().isEmpty())
    }

    @Test
    fun creationNormalizesTextAndKeepsUniqueOrderedSubtasks() = runTest {
        val draft = TaskDraft(
            title = "  Parent  ",
            description = "  Details  ",
            subtasks = persistentListOf(child("b", 9), child("a", 4)),
        )
        val id = (repository.saveTask(null, draft) as TaskMutationResult.Success).taskId
        val saved = requireNotNull(repository.getTask(id))
        assertEquals("Parent", saved.title)
        assertEquals("Details", saved.description)
        assertEquals(listOf("b", "a"), saved.subtasks.map { it.id.value })
        assertEquals(listOf(0, 1), saved.subtasks.map { it.position })
        assertTrue(saved.subtasks.all { it.taskId == id })
    }

    @Test
    fun duplicateSubtaskIdsRejectEntireMutation() = runTest {
        val draft = TaskDraft(title = "Parent", subtasks = persistentListOf(child("a"), child("a")))
        assertTrue(repository.saveTask(null, draft) is TaskMutationResult.Failure)
        assertTrue(repository.observeTasks().first().isEmpty())
    }

    @Test
    fun updatingMissingTaskDoesNotRecreateIt() = runTest {
        val id = TaskId("missing")
        assertTrue(repository.saveTask(id, TaskDraft(title = "Changed")) is TaskMutationResult.Failure)
        assertEquals(null, repository.getTask(id))
    }

    @Test
    fun subtaskOwnedByAnotherTaskCannotBeStolen() = runTest {
        val first = (repository.saveTask(
            null, TaskDraft(title = "First", subtasks = persistentListOf(child("shared"))),
        ) as TaskMutationResult.Success).taskId
        val second = repository.saveTask(
            null, TaskDraft(title = "Second", subtasks = persistentListOf(child("shared"))),
        )
        assertTrue(second is TaskMutationResult.Failure)
        assertEquals(1, repository.observeTasks().first().size)
        assertEquals("shared", repository.getTask(first)?.subtasks?.single()?.id?.value)
    }

    @Test
    fun pendingChildrenRequireExplicitCompleteAll() = runTest {
        val id = (repository.saveTask(
            null, TaskDraft(title = "Parent", subtasks = persistentListOf(child("child"))),
        ) as TaskMutationResult.Success).taskId
        assertTrue(repository.completeTask(id, includeSubtasks = false).isFailure)
        assertFalse(requireNotNull(repository.getTask(id)).isCompleted)
        assertFalse(requireNotNull(repository.getTask(id)).subtasks.single().isCompleted)
    }

    @Test
    fun completingAndRestoringPreservesMixedChildStatesAndMetadata() = runTest {
        val draft = TaskDraft(
            title = "Parent",
            description = "Details",
            dueDateEpochDays = 42,
            subtasks = persistentListOf(child("done").copy(isCompleted = true), child("pending")),
        )
        val id = (repository.saveTask(null, draft) as TaskMutationResult.Success).taskId
        val before = repository.getTask(id)
        val snapshot = repository.completeTask(id, includeSubtasks = true).getOrThrow()
        assertTrue(requireNotNull(repository.getTask(id)).subtasks.all { it.isCompleted })
        assertTrue(repository.restoreCompletion(snapshot) is TaskMutationResult.Success)
        assertEquals(before, repository.getTask(id))
    }

    @Test
    fun restoringDeletedTaskFailsWithoutResurrection() = runTest {
        val id = (repository.saveTask(null, TaskDraft(title = "Parent")) as TaskMutationResult.Success).taskId
        val snapshot = repository.completeTask(id, includeSubtasks = true).getOrThrow()
        repository.deleteTask(id)
        assertTrue(repository.restoreCompletion(snapshot) is TaskMutationResult.Failure)
        assertEquals(null, repository.getTask(id))
    }

    @Test
    fun restoringChangedChildSetRejectsEntireMutation() = runTest {
        val id = (repository.saveTask(
            null, TaskDraft(title = "Parent", subtasks = persistentListOf(child("original"))),
        ) as TaskMutationResult.Success).taskId
        val snapshot = repository.completeTask(id, includeSubtasks = true).getOrThrow()
        repository.saveTask(id, TaskDraft(title = "Changed", subtasks = persistentListOf(child("replacement"))))
        val beforeUndo = repository.getTask(id)
        assertTrue(repository.restoreCompletion(snapshot) is TaskMutationResult.Failure)
        assertEquals(beforeUndo, repository.getTask(id))
    }

    @Test
    fun reschedulingMissingTaskDoesNotReportSuccess() = runTest {
        assertTrue(repository.rescheduleTask(TaskId("missing"), 42) is TaskMutationResult.Failure)
    }

    @Test
    fun dayAndSingleTaskQueriesReflectWritesAndDeletion() = runTest {
        val id = (repository.saveTask(null, TaskDraft(title = "Parent", dueDateEpochDays = 1))
            as TaskMutationResult.Success).taskId
        assertEquals(id, repository.observeTask(id).first()?.id)
        assertEquals(1, repository.observeTasksForDay(1).first().size)
        repository.rescheduleTask(id, 2)
        assertTrue(repository.observeTasksForDay(1).first().isEmpty())
        assertEquals(1, repository.observeTasksForDay(2).first().size)
        repository.deleteTask(id)
        assertEquals(null, repository.observeTask(id).first())
    }

    private fun child(id: String, position: Int = 0) =
        Subtask(SubtaskId(id), TaskId("unpublished"), id, position = position)
}
