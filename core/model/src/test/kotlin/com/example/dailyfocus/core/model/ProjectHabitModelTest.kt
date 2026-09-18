package com.example.dailyfocus.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectHabitModelTest {
    @Test fun `project archives without changing identity`() {
        val project = Project(ProjectId("p1"), "Lanzamiento", createdAtMillis = 1L)
        assertEquals(ProjectId("p1"), project.copy(isArchived = true).id)
    }

    @Test fun `habit completion uses civil day and level`() {
        val completion = HabitCompletion(HabitId("h1"), 20L, HabitCompletionLevel.PLUS)
        assertEquals(20L, completion.epochDay)
        assertEquals(HabitCompletionLevel.PLUS, completion.level)
    }

    @Test fun `habit supports a weekly occurrence target`() {
        val habit = Habit(
            id = HabitId("h1"),
            name = "Entrenar",
            frequency = HabitFrequency.TIMES_PER_WEEK,
            occurrencesPerWeek = 3,
        )
        assertEquals(HabitFrequency.TIMES_PER_WEEK, habit.frequency)
        assertEquals(3, habit.occurrencesPerWeek)
    }

    @Test fun `every n days uses creation day as its anchor`() {
        val habit = Habit(HabitId("h2"), "Leer", HabitFrequency.EVERY_N_DAYS, intervalDays = 3, createdEpochDay = 10)

        assertEquals(true, habit.isDueOn(10))
        assertEquals(false, habit.isDueOn(12))
        assertEquals(true, habit.isDueOn(13))
    }
}
