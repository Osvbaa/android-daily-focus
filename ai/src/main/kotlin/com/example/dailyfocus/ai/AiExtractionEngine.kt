package com.example.dailyfocus.ai

import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.AiExtractionResult
import com.example.dailyfocus.core.model.NoteExtractionInput
import kotlinx.coroutines.flow.Flow

/** Provider-neutral port used by Notes. SDK-specific types stay behind this interface. */
interface AiExtractionEngine {
    fun observeAvailability(): Flow<AiAvailability>

    suspend fun extractTasks(input: NoteExtractionInput): AiExtractionResult

    suspend fun downloadIfAvailable(): Flow<AiAvailability>
}
