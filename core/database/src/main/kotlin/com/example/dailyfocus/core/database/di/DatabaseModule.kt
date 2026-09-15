package com.example.dailyfocus.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.example.dailyfocus.core.database.AppDatabase
import com.example.dailyfocus.core.database.dao.NoteDao
import com.example.dailyfocus.core.database.dao.TaskDao
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
            .setDriver(BundledSQLiteDriver())
            .build()
    }

    @Provides
    fun providesTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun providesNoteDao(database: AppDatabase): NoteDao = database.noteDao()
}