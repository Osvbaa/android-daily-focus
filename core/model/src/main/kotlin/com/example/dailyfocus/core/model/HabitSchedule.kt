package com.example.dailyfocus.core.model

import java.time.DayOfWeek
import java.time.LocalDate

fun Habit.isDueOn(epochDay: Long): Boolean {
    if (epochDay < createdEpochDay) return false
    return when (frequency) {
        HabitFrequency.DAILY,
        HabitFrequency.WEEKLY,
        HabitFrequency.TIMES_PER_WEEK,
        -> true
        HabitFrequency.EVERY_N_DAYS -> intervalDays > 0 && (epochDay - createdEpochDay) % intervalDays == 0L
    }
}

fun Long.startOfCivilWeek(): Long {
    val date = LocalDate.ofEpochDay(this)
    return date.minusDays((date.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()).toEpochDay()
}
