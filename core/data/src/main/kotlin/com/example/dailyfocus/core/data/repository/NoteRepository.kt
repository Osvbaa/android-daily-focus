package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.Note
import kotlinx.coroutines.flow.Flow

/** Persistence boundary for notes; Room entities and DAOs remain inside core:data/database. */
interface NoteRepository {
    /** Emits notes ordered by most recent update. */
    fun observeAllNotes(): Flow<List<Note>>

    suspend fun getNote(id: String): Note?

    /** Creates or updates a note and returns its stable ID. */
    suspend fun upsertNote(id: String?, title: String, content: String): String

    suspend fun deleteNote(id: String)
}
