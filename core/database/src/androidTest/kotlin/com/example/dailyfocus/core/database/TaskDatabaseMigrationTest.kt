package com.example.dailyfocus.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class TaskDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test
    fun migrate1To2AddsDescriptionAndSubtasks() {
        helper.createDatabase(DB_NAME, 1).apply {
            execSQL("INSERT INTO tasks(id, title, is_completed, due_date_epoch_days, priority, created_at_millis) VALUES ('task-1', 'Parent', 0, 10, 'NORMAL', 1)")
            close()
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)
            .use { database ->
                database.query("PRAGMA table_info(tasks)").use { cursor ->
                    val descriptionColumn = cursor.getColumnIndex("name")
                    val names = buildList {
                        while (cursor.moveToNext()) add(cursor.getString(descriptionColumn))
                    }
                    assertEquals(true, "description" in names)
                }
                database.query("SELECT COUNT(*) FROM subtasks").use { cursor ->
                    cursor.moveToFirst()
                    assertEquals(0, cursor.getInt(0))
                }
            }
    }

    @Test
    fun migrate9To10TreatsHistoricalFocusTargetsAsResolved() {
        helper.createDatabase(FOCUS_DB_NAME, 9).apply {
            execSQL(
                "INSERT INTO focus_sessions(id, task_id, habit_id, habit_level, duration_seconds, remaining_seconds, started_at_millis, deadline_at_millis, completed_at_millis, status) " +
                    "VALUES ('focus-old', 'task-old', NULL, NULL, 60, 0, 1000, 61000, 61000, 'COMPLETED')",
            )
            close()
        }

        helper.runMigrationsAndValidate(FOCUS_DB_NAME, 10, true, MIGRATION_9_10).use { database ->
            database.query("SELECT target_decision FROM focus_sessions WHERE id = 'focus-old'").use { cursor ->
                cursor.moveToFirst()
                assertEquals("NONE", cursor.getString(0))
            }
        }
    }

    @Test
    fun bundledDriverMigrates3To10AndPreservesTaskData() = runBlocking {
        helper.createDatabase(BUNDLED_DB_NAME, 3).apply {
            execSQL(
                "INSERT INTO tasks(id, title, description, is_completed, due_date_epoch_days, priority, created_at_millis, revision) " +
                    "VALUES ('task-3', 'Persistida', 'Detalle', 0, 10, 'NONE', 1, 0)",
            )
            close()
        }

        val database = Room.databaseBuilder<AppDatabase>(
            InstrumentationRegistry.getInstrumentation().targetContext,
            BUNDLED_DB_NAME,
        )
            .setDriver(BundledSQLiteDriver())
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .build()

        try {
            val task = database.taskDao().getTaskById("task-3")
            assertEquals("Persistida", task?.title)
            assertEquals(null, task?.estimatedDurationSeconds)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val DB_NAME = "task-migration-test"
        const val BUNDLED_DB_NAME = "bundled-task-migration-test"
        const val FOCUS_DB_NAME = "focus-target-migration-test"
    }
}
