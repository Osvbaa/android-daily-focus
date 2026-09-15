package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.NoteRepository
import com.example.dailyfocus.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.UUID

class FakeNoteRepository : NoteRepository {

    private val notesStream = MutableStateFlow<Map<String, Note>>(emptyMap())

    override fun observeAllNotes(): Flow<List<Note>> =
        notesStream.map { map ->
            map.values.sortedByDescending { it.updatedAtMillis }
        }

    override suspend fun getNote(id: String): Note? = notesStream.value[id]

    override suspend fun upsertNote(id: String?, title: String, content: String): String {
        val now = System.currentTimeMillis()
        val noteId = id ?: UUID.randomUUID().toString()
        val existing = notesStream.value[noteId]
        val note = Note(
            id = noteId,
            title = title.trim(),
            content = content,
            updatedAtMillis = now,
            createdAtMillis = existing?.createdAtMillis ?: now
        )
        notesStream.update { it + (noteId to note) }
        return noteId
    }

    override suspend fun deleteNote(id: String) {
        notesStream.update { it - id }
    }
}