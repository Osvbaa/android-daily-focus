package com.example.dailyfocus.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.testing.repository.TaskRepositoryContract
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

/** Runs the same contract with production's driver on an actual Android runtime. */
@RunWith(AndroidJUnit4::class)
class BundledTaskRepositoryContractTest : TaskRepositoryContract() {
    private lateinit var database: AppDatabase
    override lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java,
        ).setDriver(BundledSQLiteDriver()).build()
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
