package com.example.dailyfocus.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dailyfocus.core.database.dao.NoteDao
import com.example.dailyfocus.core.database.dao.TaskDao
import com.example.dailyfocus.core.database.dao.TaskDraftDao
import com.example.dailyfocus.core.database.dao.TaskUndoDao
import com.example.dailyfocus.core.database.dao.ReminderSettingsDao
import com.example.dailyfocus.core.database.model.NoteEntity
import com.example.dailyfocus.core.database.model.TaskEntity
import com.example.dailyfocus.core.database.model.TaskNoteCrossRef
import com.example.dailyfocus.core.database.model.SubtaskEntity
import com.example.dailyfocus.core.database.model.TaskDraftEntity
import com.example.dailyfocus.core.database.model.TaskDraftSubtaskEntity
import com.example.dailyfocus.core.database.model.TaskUndoEntity
import com.example.dailyfocus.core.database.model.TaskUndoSubtaskEntity
import com.example.dailyfocus.core.database.model.ReminderSettingsEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TaskEntity::class,
        NoteEntity::class,
        TaskNoteCrossRef::class,
        SubtaskEntity::class,
        TaskDraftEntity::class,
        TaskDraftSubtaskEntity::class,
        TaskUndoEntity::class,
        TaskUndoSubtaskEntity::class,
        ReminderSettingsEntity::class,
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskDraftDao(): TaskDraftDao
    abstract fun taskUndoDao(): TaskUndoDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao
    abstract fun noteDao(): NoteDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT")
        db.execSQL("CREATE TABLE IF NOT EXISTS subtasks (id TEXT NOT NULL, task_id TEXT NOT NULL, title TEXT NOT NULL, is_completed INTEGER NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(task_id) REFERENCES tasks(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_task_id ON subtasks(task_id)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tasks ADD COLUMN revision INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_drafts (`key` TEXT NOT NULL, task_id TEXT, title TEXT NOT NULL, description TEXT NOT NULL, due_date_epoch_days INTEGER, priority TEXT NOT NULL, base_revision INTEGER, draft_revision INTEGER NOT NULL, updated_at_millis INTEGER NOT NULL, PRIMARY KEY(`key`))")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_draft_subtasks (draft_key TEXT NOT NULL, id TEXT NOT NULL, title TEXT NOT NULL, is_completed INTEGER NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(draft_key, id))")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_draft_subtasks_draft_key ON task_draft_subtasks(draft_key)")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_undo_operations (operation_id TEXT NOT NULL, task_id TEXT NOT NULL, task_was_completed INTEGER NOT NULL, created_elapsed_millis INTEGER NOT NULL, expires_at_elapsed_millis INTEGER NOT NULL, expected_revision INTEGER NOT NULL, PRIMARY KEY(operation_id))")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_undo_subtasks (operation_id TEXT NOT NULL, subtask_id TEXT NOT NULL, was_completed INTEGER NOT NULL, PRIMARY KEY(operation_id, subtask_id))")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_task_undo_subtasks_operation_id ON task_undo_subtasks(operation_id)")
        db.execSQL("CREATE TABLE IF NOT EXISTS task_reminder_settings (id INTEGER NOT NULL, enabled INTEGER NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, lastPublishedEpochDay INTEGER, PRIMARY KEY(id))")
    }
}
