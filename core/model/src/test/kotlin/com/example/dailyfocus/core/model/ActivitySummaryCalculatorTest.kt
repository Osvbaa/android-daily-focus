package com.example.dailyfocus.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivitySummaryCalculatorTest {
    @Test
    fun `multiple events on one day count once and consecutive days form a streak`() {
        val events = listOf(
            ActivityEvent("a", StreakEventType.TASK, epochDay = 10, points = 5, occurredAtMillis = 1),
            ActivityEvent("b", StreakEventType.POMODORO, epochDay = 10, points = 1, occurredAtMillis = 2),
            ActivityEvent("c", StreakEventType.HABIT_LEVEL, epochDay = 9, points = 3, occurredAtMillis = 3),
        )

        val summary = calculateActivitySummary(events, todayEpochDay = 10)

        assertEquals(2, summary.currentStreakDays)
        assertEquals(2, summary.longestStreakDays)
        assertEquals(9, summary.totalPoints)
        assertEquals(6, summary.todayPoints)
    }

    @Test
    fun `a gap breaks current streak but not historical best`() {
        val events = (1L..3L).map { day ->
            ActivityEvent("$day", StreakEventType.TASK, epochDay = day, points = 0, occurredAtMillis = day)
        } + ActivityEvent("5", StreakEventType.TASK, epochDay = 5, points = 0, occurredAtMillis = 5)

        val summary = calculateActivitySummary(events, todayEpochDay = 5)

        assertEquals(1, summary.currentStreakDays)
        assertEquals(3, summary.longestStreakDays)
    }
}
