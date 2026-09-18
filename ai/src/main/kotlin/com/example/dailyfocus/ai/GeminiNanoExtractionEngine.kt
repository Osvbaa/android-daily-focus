package com.example.dailyfocus.ai

import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.AiFailureReason
import com.example.dailyfocus.core.model.AiExtractionResult
import com.example.dailyfocus.core.model.AiProvider
import com.example.dailyfocus.core.model.NoteExtractionInput
import com.example.dailyfocus.core.model.TaskSuggestion
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.time.measureTimedValue

/** Gemini Nano adapter. No prompt leaves the device; unsupported devices use manual extraction. */
@Singleton
class GeminiNanoExtractionEngine @Inject constructor() : AiExtractionEngine {
    private val model: GenerativeModel by lazy { Generation.getClient() }
    private val parser = TaskSuggestionParser()

    override fun observeAvailability(): Flow<AiAvailability> = flow {
        emit(model.checkStatus().toAvailability())
    }

    override suspend fun extractTasks(input: NoteExtractionInput): AiExtractionResult {
        if (input.content.isBlank()) return AiExtractionResult.InsufficientText
        return runCatching {
            val measured = measureTimedValue {
                val response = model.generateContent(prompt(input.content))
                parser.parse(response.candidates.firstOrNull()?.text.orEmpty())
            }
            if (measured.value.isEmpty()) AiExtractionResult.NoActionableTasks
            else AiExtractionResult.Success(AiProvider.GEMINI_NANO, measured.value, measured.duration.inWholeMilliseconds)
        }.getOrElse { AiExtractionResult.Failure(AiFailureReason.INFERENCE_FAILED) }
    }

    override suspend fun downloadIfAvailable(): Flow<AiAvailability> = model.download().map { status ->
        when (status) {
            is DownloadStatus.DownloadCompleted -> AiAvailability.Available(AiProvider.GEMINI_NANO, "gemini-nano")
            is DownloadStatus.DownloadFailed -> AiAvailability.TemporarilyUnavailable
            else -> AiAvailability.Downloading
        }
    }

    private fun prompt(note: String) = """
        Extract actionable tasks from the note below. Return one task per line only, in the format:
        title|confidence between 0 and 1
        Do not return explanations or headings. Ignore wishes, questions, and completed actions.
        NOTE:
        $note
    """.trimIndent()

    private fun Int.toAvailability(): AiAvailability = when (this) {
        FeatureStatus.AVAILABLE -> AiAvailability.Available(AiProvider.GEMINI_NANO, "gemini-nano")
        FeatureStatus.DOWNLOADABLE -> AiAvailability.Downloadable(AiProvider.GEMINI_NANO)
        FeatureStatus.DOWNLOADING -> AiAvailability.Downloading
        else -> AiAvailability.UnsupportedDevice
    }
}
