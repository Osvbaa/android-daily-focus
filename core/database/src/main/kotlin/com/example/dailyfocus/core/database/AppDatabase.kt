package com.example.dailyfocus.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dailyfocus.core.database.dao.NoteDao
import com.example.dailyfocus.core.database.dao.ActivityDao
import com.example.dailyfocus.core.database.dao.FocusSessionDao
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
import com.example.dailyfocus.core.database.model.ProjectEntity
import com.example.dailyfocus.core.database.model.ProjectMilestoneEntity
import com.example.dailyfocus.core.database.model.HabitEntity
import com.example.dailyfocus.core.database.model.HabitCompletionEntity
import com.example.dailyfocus.core.database.model.ActivityEventEntity
import com.example.dailyfocus.core.database.model.FocusSessionEntity
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

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
        ProjectEntity::class,
        ProjectMilestoneEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        ActivityEventEntity::class,
        FocusSessionEntity::class,
    ],
    version = 10,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun taskDraftDao(): TaskDraftDao
    abstract fun taskUndoDao(): TaskUndoDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao
    abstract fun noteDao(): NoteDao
    abstract fun projectDao(): com.example.dailyfocus.core.database.dao.ProjectDao
    abstract fun habitDao(): com.example.dailyfocus.core.database.dao.HabitDao
    abstract fun activityDao(): ActivityDao
    abstract fun focusSessionDao(): FocusSessionDao
}

/**
 * Room calls the [SQLiteConnection] overload when a driver is configured, while
 * [SupportSQLiteDatabase] is still used by legacy migration tooling. Keeping the SQL in one
 * place makes both paths equivalent and protects existing installations using BundledSQLiteDriver.
 */
private class SqlMigration(
    from: Int,
    to: Int,
    private vararg val statements: String,
) : Migration(from, to) {
    override fun migrate(db: SupportSQLiteDatabase) = statements.forEach(db::execSQL)

    override fun migrate(connection: SQLiteConnection) = statements.forEach(connection::execSQL)
}

val MIGRATION_1_2: Migration = SqlMigration(
    1, 2,
    "ALTER TABLE tasks ADD COLUMN description TEXT",
    "CREATE TABLE IF NOT EXISTS subtasks (id TEXT NOT NULL, task_id TEXT NOT NULL, title TEXT NOT NULL, is_completed INTEGER NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(id), FOREIGN KEY(task_id) REFERENCES tasks(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
    "CREATE INDEX IF NOT EXISTS index_subtasks_task_id ON subtasks(task_id)",
)

val MIGRATION_2_3: Migration = SqlMigration(
    2, 3,
    "ALTER TABLE tasks ADD COLUMN revision INTEGER NOT NULL DEFAULT 0",
    "CREATE TABLE IF NOT EXISTS task_drafts (`key` TEXT NOT NULL, task_id TEXT, title TEXT NOT NULL, description TEXT NOT NULL, due_date_epoch_days INTEGER, priority TEXT NOT NULL, base_revision INTEGER, draft_revision INTEGER NOT NULL, updated_at_millis INTEGER NOT NULL, PRIMARY KEY(`key`))",
    "CREATE TABLE IF NOT EXISTS task_draft_subtasks (draft_key TEXT NOT NULL, id TEXT NOT NULL, title TEXT NOT NULL, is_completed INTEGER NOT NULL, position INTEGER NOT NULL, PRIMARY KEY(draft_key, id))",
    "CREATE INDEX IF NOT EXISTS index_task_draft_subtasks_draft_key ON task_draft_subtasks(draft_key)",
    "CREATE TABLE IF NOT EXISTS task_undo_operations (operation_id TEXT NOT NULL, task_id TEXT NOT NULL, task_was_completed INTEGER NOT NULL, created_elapsed_millis INTEGER NOT NULL, expires_at_elapsed_millis INTEGER NOT NULL, expected_revision INTEGER NOT NULL, PRIMARY KEY(operation_id))",
    "CREATE TABLE IF NOT EXISTS task_undo_subtasks (operation_id TEXT NOT NULL, subtask_id TEXT NOT NULL, was_completed INTEGER NOT NULL, PRIMARY KEY(operation_id, subtask_id))",
    "CREATE INDEX IF NOT EXISTS index_task_undo_subtasks_operation_id ON task_undo_subtasks(operation_id)",
    "CREATE TABLE IF NOT EXISTS task_reminder_settings (id INTEGER NOT NULL, enabled INTEGER NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, lastPublishedEpochDay INTEGER, PRIMARY KEY(id))",
)

val MIGRATION_3_4: Migration = SqlMigration(
    3, 4,
    "ALTER TABLE tasks ADD COLUMN project_id TEXT",
    "ALTER TABLE tasks ADD COLUMN milestone_id TEXT",
    "ALTER TABLE task_drafts ADD COLUMN project_id TEXT",
    "ALTER TABLE task_drafts ADD COLUMN milestone_id TEXT",
    "CREATE INDEX IF NOT EXISTS index_tasks_project_id ON tasks(project_id)",
    "CREATE INDEX IF NOT EXISTS index_tasks_milestone_id ON tasks(milestone_id)",
    "CREATE TABLE IF NOT EXISTS projects (id TEXT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, isArchived INTEGER NOT NULL, createdAtMillis INTEGER NOT NULL, PRIMARY KEY(id))",
    "CREATE TABLE IF NOT EXISTS project_milestones (id TEXT NOT NULL, project_id TEXT NOT NULL, title TEXT NOT NULL, position INTEGER NOT NULL, status TEXT NOT NULL, PRIMARY KEY(id), FOREIGN KEY(project_id) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
    "CREATE INDEX IF NOT EXISTS index_project_milestones_project_id ON project_milestones(project_id)",
    "CREATE UNIQUE INDEX IF NOT EXISTS index_project_milestones_project_id_position ON project_milestones(project_id, position)",
)

val MIGRATION_4_5: Migration = SqlMigration(
    4, 5,
    "CREATE TABLE IF NOT EXISTS habits (id TEXT NOT NULL, name TEXT NOT NULL, frequency TEXT NOT NULL, interval_days INTEGER NOT NULL, occurrences_per_week INTEGER NOT NULL, target_level TEXT NOT NULL, created_epoch_day INTEGER NOT NULL, is_archived INTEGER NOT NULL, PRIMARY KEY(id))",
    "CREATE TABLE IF NOT EXISTS habit_completions (habit_id TEXT NOT NULL, epoch_day INTEGER NOT NULL, level TEXT NOT NULL, PRIMARY KEY(habit_id, epoch_day), FOREIGN KEY(habit_id) REFERENCES habits(id) ON UPDATE NO ACTION ON DELETE CASCADE)",
    "CREATE INDEX IF NOT EXISTS index_habit_completions_habit_id ON habit_completions(habit_id)",
    "CREATE INDEX IF NOT EXISTS index_habit_completions_epoch_day ON habit_completions(epoch_day)",
)

val MIGRATION_5_6: Migration = SqlMigration(
    5, 6,
    "CREATE TABLE IF NOT EXISTS activity_events (idempotency_key TEXT NOT NULL, type TEXT NOT NULL, source_id TEXT, epoch_day INTEGER NOT NULL, category TEXT NOT NULL, points INTEGER NOT NULL, occurred_at_millis INTEGER NOT NULL, rules_version INTEGER NOT NULL, PRIMARY KEY(idempotency_key))",
    "CREATE INDEX IF NOT EXISTS index_activity_events_epoch_day ON activity_events(epoch_day)",
    "CREATE INDEX IF NOT EXISTS index_activity_events_category_epoch_day ON activity_events(category, epoch_day)",
)

val MIGRATION_6_7: Migration = SqlMigration(
    6, 7,
    "CREATE TABLE IF NOT EXISTS focus_sessions (id TEXT NOT NULL, task_id TEXT, habit_id TEXT, habit_level TEXT, duration_seconds INTEGER NOT NULL, remaining_seconds INTEGER NOT NULL, started_at_millis INTEGER NOT NULL, completed_at_millis INTEGER, status TEXT NOT NULL, PRIMARY KEY(id))",
    "CREATE INDEX IF NOT EXISTS index_focus_sessions_started_at_millis ON focus_sessions(started_at_millis)",
)

val MIGRATION_7_8: Migration = SqlMigration(
    7, 8,
    "ALTER TABLE tasks ADD COLUMN estimated_duration_seconds INTEGER",
    "ALTER TABLE task_drafts ADD COLUMN estimated_duration_seconds INTEGER",
)

val MIGRATION_8_9: Migration = SqlMigration(
    8, 9,
    "ALTER TABLE focus_sessions ADD COLUMN deadline_at_millis INTEGER",
)

val MIGRATION_9_10: Migration = SqlMigration(
    9, 10,
    "ALTER TABLE focus_sessions ADD COLUMN target_decision TEXT NOT NULL DEFAULT 'NONE'",
)
