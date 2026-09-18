package com.example.dailyfocus.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.StreakEventType
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineFirstHabitRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineFirstHabitRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).build()
        var id = 0
        repository = OfflineFirstHabitRepository(
            habitDao = database.habitDao(),
            idGenerator = IdGenerator { "habit-${++id}" },
            dates = DateProvider { LocalDate.ofEpochDay(5) },
            ioDispatcher = Dispatchers.Unconfined,
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `times per week allows the configured number of distinct days`() = runBlocking {
        val habitId = repository.createHabit(HabitDraft("Entrenar", HabitFrequency.TIMES_PER_WEEK, occurrencesPerWeek = 2)).getOrThrow()

        assertTrue(repository.completeLevel(habitId, 5, HabitCompletionLevel.MINI).isSuccess)
        assertTrue(repository.completeLevel(habitId, 6, HabitCompletionLevel.PLUS).isSuccess)
        assertTrue(repository.completeLevel(habitId, 7, HabitCompletionLevel.ELITE).isFailure)
        assertEquals(listOf(6L, 5L), repository.observeCompletions(habitId).first().map { it.epochDay })
    }

    @Test
    fun `weekly habit can be completed only once in the civil week`() = runBlocking {
        val habitId = repository.createHabit(HabitDraft("Planear", HabitFrequency.WEEKLY)).getOrThrow()

        assertTrue(repository.completeLevel(habitId, 5, HabitCompletionLevel.MINI).isSuccess)
        assertTrue(repository.completeLevel(habitId, 6, HabitCompletionLevel.MINI).isFailure)
    }

    @Test
    fun `every n days rejects dates outside its anchored schedule`() = runBlocking {
        val habitId = repository.createHabit(HabitDraft("Leer", HabitFrequency.EVERY_N_DAYS, intervalDays = 3)).getOrThrow()

        assertTrue(repository.completeLevel(habitId, 6, HabitCompletionLevel.MINI).isFailure)
        assertTrue(repository.completeLevel(habitId, 8, HabitCompletionLevel.MINI).isSuccess)
    }

    @Test
    fun `upgrading a level keeps one completion and one activity event`() = runBlocking {
        val habitId = repository.createHabit(HabitDraft("Leer", HabitFrequency.DAILY)).getOrThrow()
        val activity = OfflineFirstActivityRepository(database.activityDao(), WallClock { 100L }, Dispatchers.Unconfined)

        for (level in listOf(HabitCompletionLevel.MINI, HabitCompletionLevel.PLUS, HabitCompletionLevel.ELITE)) {
            assertTrue(repository.completeLevel(habitId, 5, level).isSuccess)
            assertTrue(activity.recordCompletion(StreakEventType.HABIT_LEVEL, habitId.value, 5, level).isSuccess)
        }

        assertEquals(listOf(HabitCompletionLevel.ELITE), repository.observeCompletions(habitId).first().map { it.level })
        val events = activity.observeEvents().first()
        assertEquals(1, events.size)
        assertEquals(8, events.single().points)
    }
}
