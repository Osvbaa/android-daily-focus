package com.example.dailyfocus.ai

import com.example.dailyfocus.core.model.TaskSuggestion
import java.util.UUID

/** Strict parser for the line protocol returned by the local model. */
class TaskSuggestionParser {
    fun parse(response: String): List<TaskSuggestion> = response.lineSequence().mapNotNull { line ->
        val parts = line.split('|', limit = 2)
        val title = parts.firstOrNull()?.trim().orEmpty()
        val confidence = parts.getOrNull(1)?.trim()?.toFloatOrNull() ?: return@mapNotNull null
        if (title.isBlank() || confidence !in 0f..1f) return@mapNotNull null
        TaskSuggestion(UUID.randomUUID().toString(), title, confidence = confidence)
    }.toList()
}
