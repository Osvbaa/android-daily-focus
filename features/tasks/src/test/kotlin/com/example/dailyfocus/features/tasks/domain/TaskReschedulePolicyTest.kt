package com.example.dailyfocus.features.tasks.domain

import com.example.dailyfocus.core.model.ReschedulePreset
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskReschedulePolicyTest {
    @Test
    fun `weekend preset stays on weekend and advances from weekdays`() {
        assertEquals(LocalDate.of(2026, 9, 19).toEpochDay(), TaskReschedulePolicy.dueDate(ReschedulePreset.THIS_WEEKEND, LocalDate.of(2026, 9, 17)))
        assertEquals(LocalDate.of(2026, 9, 19).toEpochDay(), TaskReschedulePolicy.dueDate(ReschedulePreset.THIS_WEEKEND, LocalDate.of(2026, 9, 19)))
        assertEquals(LocalDate.of(2026, 9, 20).toEpochDay(), TaskReschedulePolicy.dueDate(ReschedulePreset.THIS_WEEKEND, LocalDate.of(2026, 9, 20)))
    }

    @Test
    fun `next week always selects the following Monday`() {
        assertEquals(LocalDate.of(2026, 9, 21).toEpochDay(), TaskReschedulePolicy.dueDate(ReschedulePreset.NEXT_WEEK, LocalDate.of(2026, 9, 17)))
        assertEquals(LocalDate.of(2026, 9, 28).toEpochDay(), TaskReschedulePolicy.dueDate(ReschedulePreset.NEXT_WEEK, LocalDate.of(2026, 9, 21)))
    }
}
