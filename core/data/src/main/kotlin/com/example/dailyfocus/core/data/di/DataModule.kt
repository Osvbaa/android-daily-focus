package com.example.dailyfocus.core.data.di

import com.example.dailyfocus.core.data.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstNoteRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstTaskRepository
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstProjectRepository
import com.example.dailyfocus.core.data.repository.HabitRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstHabitRepository
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstActivityRepository
import com.example.dailyfocus.core.data.repository.FocusSessionRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstFocusSessionRepository
import com.example.dailyfocus.core.data.repository.FocusTargetDecisionRepository
import com.example.dailyfocus.core.data.repository.OfflineFirstFocusTargetDecisionRepository

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindsTaskRepository(
        taskRepository: OfflineFirstTaskRepository
    ): TaskRepository

    @Binds
    @Singleton
    fun bindsNoteRepository(
        noteRepository: OfflineFirstNoteRepository
    ): NoteRepository

    @Binds
    @Singleton
    fun bindsProjectRepository(repository: OfflineFirstProjectRepository): ProjectRepository

    @Binds
    @Singleton
    fun bindsHabitRepository(repository: OfflineFirstHabitRepository): HabitRepository

    @Binds
    @Singleton
    fun bindsActivityRepository(repository: OfflineFirstActivityRepository): ActivityRepository

    @Binds
    @Singleton
    fun bindsFocusSessionRepository(repository: OfflineFirstFocusSessionRepository): FocusSessionRepository

    @Binds
    @Singleton
    fun bindsFocusTargetDecisionRepository(repository: OfflineFirstFocusTargetDecisionRepository): FocusTargetDecisionRepository
}
