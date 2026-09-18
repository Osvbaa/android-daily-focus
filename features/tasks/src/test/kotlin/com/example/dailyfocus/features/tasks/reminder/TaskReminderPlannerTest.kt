package com.example.dailyfocus.features.tasks.reminder

import com.example.dailyfocus.core.model.TaskReminderSettings
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskReminderPlannerTest {
    @Test
    fun `publishes only for enabled reminders with pending tasks not already published today`() {
        val settings = TaskReminderSettings(enabled = true, lastPublishedEpochDay = 10)

        assertTrue(TaskReminderPlanner.shouldPublish(settings, todayEpochDay = 11, hasPendingTasks = true))
        assertFalse(TaskReminderPlanner.shouldPublish(settings, todayEpochDay = 11, hasPendingTasks = false))
        assertFalse(TaskReminderPlanner.shouldPublish(settings.copy(enabled = false), 11, true))
        assertFalse(TaskReminderPlanner.shouldPublish(settings, todayEpochDay = 10, hasPendingTasks = true))
    }

    @Test
    fun `calculates the next configured run on the same day`() {
        val now = ZonedDateTime.of(2026, 9, 11, 8, 30, 0, 0, ZoneOffset.UTC)
        val delay = TaskReminderPlanner.delayUntilNextRun(
            TaskReminderSettings(enabled = true, hour = 9, minute = 15),
            now,
        )

        assertEquals(Duration.ofMinutes(45), delay)
    }

    @Test
    fun `calculates tomorrow when today's configured time has passed`() {
        val now = ZonedDateTime.of(2026, 9, 11, 10, 0, 0, 0, ZoneOffset.UTC)
        val delay = TaskReminderPlanner.delayUntilNextRun(
            TaskReminderSettings(enabled = true, hour = 9, minute = 15),
            now,
        )

        assertEquals(Duration.ofHours(23).plusMinutes(15), delay)
    }
}
