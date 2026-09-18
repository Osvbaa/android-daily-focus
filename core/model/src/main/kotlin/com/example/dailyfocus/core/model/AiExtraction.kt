package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteExtractionInput(
    val noteId: String,
    val content: String,
)

@Serializable
data class TaskSuggestion(
    val id: String,
    val title: String,
    val suggestedDueDateEpochDays: Long? = null,
    val confidence: Float? = null,
)

enum class AiProvider { GEMINI_NANO, KOOG_LITERT }

sealed interface AiAvailability {
    data object Checking : AiAvailability
    data class Available(val provider: AiProvider, val modelVersion: String? = null) : AiAvailability
    data class Downloadable(val provider: AiProvider, val estimatedBytes: Long? = null) : AiAvailability
    data object Downloading : AiAvailability
    data object UnsupportedDevice : AiAvailability
    data object InsufficientStorage : AiAvailability
    data object TemporarilyUnavailable : AiAvailability
}

sealed interface AiExtractionResult {
    data class Success(
        val provider: AiProvider,
        val suggestions: List<TaskSuggestion>,
        val latencyMs: Long,
    ) : AiExtractionResult

    data object InsufficientText : AiExtractionResult
    data object NoActionableTasks : AiExtractionResult
    data class Failure(val reason: AiFailureReason) : AiExtractionResult
}

enum class AiFailureReason {
    UNSUPPORTED_DEVICE,
    MODEL_NOT_READY,
    OFFLINE,
    QUOTA_EXCEEDED,
    INFERENCE_FAILED,
    PARSER_FAILED,
}
