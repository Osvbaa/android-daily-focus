package com.example.dailyfocus.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.database.MIGRATION_1_2
import com.example.dailyfocus.core.database.MIGRATION_2_3
import com.example.dailyfocus.core.database.MIGRATION_3_4
import com.example.dailyfocus.core.database.MIGRATION_4_5
import com.example.dailyfocus.core.database.MIGRATION_5_6
import com.example.dailyfocus.core.database.MIGRATION_6_7
import com.example.dailyfocus.core.database.MIGRATION_7_8
import com.example.dailyfocus.core.database.MIGRATION_8_9
import com.example.dailyfocus.core.database.MIGRATION_9_10
import com.example.dailyfocus.core.database.dao.NoteDao
import com.example.dailyfocus.core.database.dao.TaskDao
import com.example.dailyfocus.core.database.dao.TaskDraftDao
import com.example.dailyfocus.core.database.dao.TaskUndoDao
import com.example.dailyfocus.core.database.dao.ReminderSettingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        val dbFile = context.getDatabasePath("dailyfocus.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun providesTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun providesTaskDraftDao(database: AppDatabase): TaskDraftDao = database.taskDraftDao()

    @Provides
    fun providesTaskUndoDao(database: AppDatabase): TaskUndoDao = database.taskUndoDao()

    @Provides
    fun providesReminderSettingsDao(database: AppDatabase): ReminderSettingsDao = database.reminderSettingsDao()

    @Provides
    fun providesNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    fun providesProjectDao(database: AppDatabase): com.example.dailyfocus.core.database.dao.ProjectDao = database.projectDao()

    @Provides
    fun providesHabitDao(database: AppDatabase): com.example.dailyfocus.core.database.dao.HabitDao = database.habitDao()

    @Provides
    fun providesActivityDao(database: AppDatabase): com.example.dailyfocus.core.database.dao.ActivityDao = database.activityDao()

    @Provides
    fun providesFocusSessionDao(database: AppDatabase): com.example.dailyfocus.core.database.dao.FocusSessionDao = database.focusSessionDao()
}
