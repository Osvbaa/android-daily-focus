package com.example.dailyfocus.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.testing.repository.TaskRepositoryContract
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineFirstTaskRepositoryContractTest : TaskRepositoryContract() {
    private lateinit var database: AppDatabase
    override lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java,
        ).build()
        var sequence = 0
        repository = OfflineFirstTaskRepository(
            database.taskDao(), IdGenerator { "task-${++sequence}" }, WallClock { 100L }, Dispatchers.IO, database.projectDao(),
        )
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
    }
}
