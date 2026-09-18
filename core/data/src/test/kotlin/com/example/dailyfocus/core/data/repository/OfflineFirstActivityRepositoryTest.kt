package com.example.dailyfocus.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.StreakEventType
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineFirstActivityRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: OfflineFirstActivityRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).setQueryExecutor(Executors.newSingleThreadExecutor()).build()
        repository = OfflineFirstActivityRepository(database.activityDao(), WallClock { 100L }, kotlinx.coroutines.Dispatchers.Unconfined)
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun `points are idempotent and habit level upgrades grant only the difference`() = runBlocking {
        repository.recordCompletion(StreakEventType.TASK, "task-1", 10)
        repository.recordCompletion(StreakEventType.TASK, "task-1", 10)
        repository.recordCompletion(StreakEventType.TASK, "task-2", 10)
        repository.recordCompletion(StreakEventType.HABIT_LEVEL, "habit-1", 10, HabitCompletionLevel.MINI)
        repository.recordCompletion(StreakEventType.HABIT_LEVEL, "habit-1", 10, HabitCompletionLevel.ELITE)

        val summary = repository.observeSummary(10).first()

        assertEquals(13, summary.todayPoints)
        assertEquals(1, summary.currentStreakDays)
    }

    @Test
    fun `focus points stop after four sessions but all sessions remain streak events`() = runBlocking {
        repeat(5) { index -> repository.recordPomodoro("session-$index", 20) }

        val events = repository.observeEvents().first()

        assertEquals(5, events.size)
        assertEquals(4, events.sumOf { it.points })
    }
}
