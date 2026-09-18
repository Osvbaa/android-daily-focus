package com.example.dailyfocus.features.tasks.domain

import com.example.dailyfocus.core.model.ReschedulePreset
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Resolves quick rescheduling against the user's current civil date. */
internal object TaskReschedulePolicy {
    fun dueDate(preset: ReschedulePreset, today: LocalDate): Long = when (preset) {
        ReschedulePreset.TODAY -> today
        ReschedulePreset.TOMORROW -> today.plusDays(1)
        ReschedulePreset.THIS_WEEKEND -> when (today.dayOfWeek) {
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> today
            else -> today.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
        }
        ReschedulePreset.NEXT_WEEK -> today.with(TemporalAdjusters.next(DayOfWeek.MONDAY))
    }.toEpochDay()
}
