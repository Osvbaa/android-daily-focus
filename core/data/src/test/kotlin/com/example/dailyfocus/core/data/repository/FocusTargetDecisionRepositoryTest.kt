package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTargetDecision
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.testing.repository.FakeActivityRepository
import com.example.dailyfocus.core.testing.repository.FakeFocusSessionRepository
import com.example.dailyfocus.core.testing.repository.FakeHabitRepository
import com.example.dailyfocus.core.testing.repository.FakeTaskRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusTargetDecisionRepositoryTest {
    private val today = LocalDate.of(2026, 9, 17)

    @Test
    fun `completed session waits for explicit confirmation before completing task`() = runTest {
        val sessions = FakeFocusSessionRepository()
        val tasks = FakeTaskRepository()
        val habits = FakeHabitRepository()
        val activity = FakeActivityRepository()
        val taskId = (tasks.saveTask(null, TaskDraft(title = "Write report")) as TaskMutationResult.Success).taskId
        val decisions = OfflineFirstFocusTargetDecisionRepository(sessions, tasks, habits, activity, DateProvider { today })

        sessions.start("focus-1", 60, 0, taskId, null, null).getOrThrow()
        sessions.updateStatus("focus-1", FocusSessionStatus.COMPLETED, completedAtMillis = 60_000).getOrThrow()

        assertFalse(tasks.getTask(taskId)!!.isCompleted)
        assertEquals(FocusTargetDecision.PENDING, decisions.observePending().first()?.targetDecision)
        assertTrue(decisions.confirm("focus-1").getOrThrow())
        assertTrue(tasks.getTask(taskId)!!.isCompleted)
        assertEquals(FocusTargetDecision.CONFIRMED, sessions.get("focus-1")?.targetDecision)
        assertEquals(null, decisions.observePending().first())
        assertFalse(decisions.confirm("focus-1").getOrThrow())
        assertEquals(1, activity.observeEvents().first().size)
    }

    @Test
    fun `dismissing target never completes task`() = runTest {
        val sessions = FakeFocusSessionRepository()
        val tasks = FakeTaskRepository()
        val taskId = (tasks.saveTask(null, TaskDraft(title = "Write report")) as TaskMutationResult.Success).taskId
        val decisions = OfflineFirstFocusTargetDecisionRepository(
            sessions, tasks, FakeHabitRepository(), FakeActivityRepository(), DateProvider { today },
        )
        sessions.start("focus-2", 60, 0, taskId, null, null).getOrThrow()
        sessions.updateStatus("focus-2", FocusSessionStatus.COMPLETED, completedAtMillis = 60_000).getOrThrow()

        assertTrue(decisions.dismiss("focus-2").getOrThrow())
        assertFalse(tasks.getTask(taskId)!!.isCompleted)
        assertEquals(FocusTargetDecision.DISMISSED, sessions.get("focus-2")?.targetDecision)
        assertFalse(decisions.dismiss("focus-2").getOrThrow())
    }

    @Test
    fun `failed task completion keeps decision pending for retry`() = runTest {
        val sessions = FakeFocusSessionRepository()
        val tasks = FakeTaskRepository()
        val taskId = (tasks.saveTask(null, TaskDraft(title = "Write report")) as TaskMutationResult.Success).taskId
        val decisions = OfflineFirstFocusTargetDecisionRepository(
            sessions, tasks, FakeHabitRepository(), FakeActivityRepository(), DateProvider { today },
        )
        sessions.start("focus-3", 60, 0, taskId, null, null).getOrThrow()
        sessions.updateStatus("focus-3", FocusSessionStatus.COMPLETED, completedAtMillis = 60_000).getOrThrow()

        tasks.failure = IllegalStateException("Task unavailable")
        assertTrue(decisions.confirm("focus-3").isFailure)
        assertEquals(FocusTargetDecision.PENDING, sessions.get("focus-3")?.targetDecision)
        tasks.failure = null
        assertTrue(decisions.confirm("focus-3").getOrThrow())
        assertTrue(tasks.getTask(taskId)!!.isCompleted)
    }

    @Test
    fun `habit target is completed only after confirmation`() = runTest {
        val sessions = FakeFocusSessionRepository()
        val habits = FakeHabitRepository()
        val habitId = habits.createHabit(HabitDraft(name = "Study", frequency = HabitFrequency.DAILY)).getOrThrow()
        val decisions = OfflineFirstFocusTargetDecisionRepository(
            sessions, FakeTaskRepository(), habits, FakeActivityRepository(), DateProvider { today },
        )
        sessions.start("focus-4", 60, 0, null, habitId, HabitCompletionLevel.MINI).getOrThrow()
        sessions.updateStatus("focus-4", FocusSessionStatus.COMPLETED, completedAtMillis = 60_000).getOrThrow()
        assertTrue(habits.observeCompletions(habitId).first().isEmpty())

        assertTrue(decisions.confirm("focus-4").getOrThrow())
        assertEquals(HabitCompletionLevel.MINI, habits.observeCompletions(habitId).first().single().level)
    }
}
