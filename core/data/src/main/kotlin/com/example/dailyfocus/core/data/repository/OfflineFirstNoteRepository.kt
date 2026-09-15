package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.database.dao.NoteDao
import com.example.dailyfocus.core.database.model.NoteEntity
import com.example.dailyfocus.core.database.model.asExternalModel
import com.example.dailyfocus.core.model.Note
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstNoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : NoteRepository {

    override fun observeAllNotes(): Flow<List<Note>> =
        noteDao.observeAllNotes()
            .map { entities -> entities.map { it.asExternalModel() } }
            .flowOn(ioDispatcher)

    override suspend fun getNote(id: String): Note? = withContext(ioDispatcher) {
        noteDao.getNoteById(id)?.asExternalModel()
    }

    override suspend fun upsertNote(id: String?, title: String, content: String): String =
        withContext(ioDispatcher) {
            val now = System.currentTimeMillis()
            val noteId = id ?: UUID.randomUUID().toString()
            val entity = NoteEntity(
                id = noteId,
                title = title.trim(),
                content = content,
                updatedAtMillis = now,
                createdAtMillis = if (id == null) now else (noteDao.getNoteById(id)?.createdAtMillis ?: now)
            )
            noteDao.upsertNote(entity)
            noteId
        }

    override suspend fun deleteNote(id: String) = withContext(ioDispatcher) {
        noteDao.deleteNote(id)
    }
}