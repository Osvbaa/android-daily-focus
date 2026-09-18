package com.example.dailyfocus.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.dailyfocus.core.database.model.SubtaskEntity
import com.example.dailyfocus.core.database.model.TaskEntity
import com.example.dailyfocus.core.database.model.NoteEntity
import com.example.dailyfocus.core.database.model.asExternalModelOrNull
import com.example.dailyfocus.core.model.Priority
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import kotlinx.coroutines.flow.first
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskDaoPersistenceTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            AppDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        if (::database.isInitialized) database.close()
    }

    @Test
    fun saveAggregateReplacesSubtasksAndKeepsTheirOrder() = runBlocking {
        val task = task()
        val dao = database.taskDao()
        dao.saveAggregate(task, listOf(subtask("second", task.id, "Second", 1), subtask("first", task.id, "First", 0)))

        dao.saveAggregate(task, listOf(subtask("first", task.id, "Renamed", 0)))

        val rows = dao.getTaskRowsById(task.id)
        assertEquals(listOf("Renamed"), rows.mapNotNull { it.subtaskTitle })
        assertEquals(listOf(0), rows.mapNotNull { it.subtaskPosition })
    }

    @Test
    fun deletingTaskCascadesToSubtasks() = runBlocking {
        val task = task()
        val dao = database.taskDao()
        dao.saveAggregate(task, listOf(subtask("sub", task.id, "Child", 0)))

        dao.deleteTask(task.id)

        assertTrue(dao.getTaskRowsById(task.id).isEmpty())
        assertTrue(dao.getSubtasksForTask(task.id).isEmpty())
    }

    @Test
    fun completingRollsBackParentWhenChildWriteFails() = runBlocking {
        val dao = database.taskDao()
        dao.saveAggregate(task(), listOf(subtask("first", "task-1", "First", 0)))
        rejectChildUpdates()

        val result = runCatching { dao.completeWithSnapshot("task-1", includeSubtasks = true) }

        assertTrue(result.isFailure)
        assertFalse(requireNotNull(dao.getTaskById("task-1")).isCompleted)
        assertFalse(dao.getSubtasksForTask("task-1").single().isCompleted)
    }

    @Test
    fun restoringRollsBackParentAndEarlierChildrenWhenLaterWriteFails() = runBlocking {
        val dao = database.taskDao()
        dao.saveAggregate(task(), listOf(
            subtask("first", "task-1", "First", 0),
            subtask("second", "task-1", "Second", 1),
        ))
        val snapshot = dao.completeWithSnapshot("task-1", includeSubtasks = true)
        rejectChildUpdates("second")

        assertTrue(runCatching { dao.restoreCompletion(snapshot) }.isFailure)

        assertTrue(requireNotNull(dao.getTaskById("task-1")).isCompleted)
        assertTrue(dao.getSubtasksForTask("task-1").all { it.isCompleted })
    }

    @Test
    fun savingRollsBackReplacementWhenInsertFails() = runBlocking {
        val dao = database.taskDao()
        dao.saveAggregate(task(), listOf(subtask("first", "task-1", "First", 0)))
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_child_insert BEFORE INSERT ON subtasks " +
                "WHEN NEW.id = 'second' BEGIN SELECT RAISE(ABORT, 'Test write failure'); END",
        )

        val result = runCatching {
            dao.saveAggregate(task().copy(title = "Changed"), listOf(subtask("second", "task-1", "Second", 0)))
        }

        assertTrue(result.isFailure)
        assertEquals("Parent", dao.getTaskById("task-1")?.title)
        assertEquals("first", dao.getSubtasksForTask("task-1").single().id)
    }

    @Test
    fun queriesKeepNoteOriginAndDeletingNotePreservesTask() = runBlocking {
        val dao = database.taskDao()
        database.noteDao().upsertNote(NoteEntity("note", "Origin", "Body", 1, 1))
        dao.insertTaskWithNoteLink(task(), "note")

        assertEquals("note", dao.getTaskRowsById("task-1").asExternalModelOrNull()?.linkedNoteId)
        assertEquals("note", dao.observeTaskRowsById("task-1").first().asExternalModelOrNull()?.linkedNoteId)

        database.noteDao().deleteNote("note")

        val remaining = requireNotNull(dao.getTaskRowsById("task-1").asExternalModelOrNull())
        assertEquals(null, remaining.linkedNoteId)
        assertEquals("Parent", remaining.title)
    }

    private fun rejectChildUpdates(id: String = "first") {
        // A SQLite failure exercises the real transaction; no DAO or database mock.
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER reject_child_update BEFORE UPDATE ON subtasks " +
                "WHEN NEW.id = '$id' BEGIN SELECT RAISE(ABORT, 'Test write failure'); END",
        )
    }

    private fun task() = TaskEntity(
        id = "task-1",
        title = "Parent",
        description = null,
        isCompleted = false,
        dueDateEpochDays = 20L,
        priority = Priority.NORMAL,
        createdAtMillis = 1L,
    )

    private fun subtask(id: String, taskId: String, title: String, position: Int) = SubtaskEntity(
        id = id,
        taskId = taskId,
        title = title,
        isCompleted = false,
        position = position,
    )
}
