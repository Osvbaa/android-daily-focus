package com.example.dailyfocus.core.testing.ai

import com.example.dailyfocus.ai.AiExtractionEngine
import com.example.dailyfocus.core.model.AiAvailability
import com.example.dailyfocus.core.model.AiExtractionResult
import com.example.dailyfocus.core.model.NoteExtractionInput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAiExtractionEngine(
    initialAvailability: AiAvailability = AiAvailability.UnsupportedDevice,
) : AiExtractionEngine {
    private val availabilityState = MutableStateFlow(initialAvailability)
    var nextResult: AiExtractionResult = AiExtractionResult.NoActionableTasks

    override fun observeAvailability(): Flow<AiAvailability> = availabilityState

    override suspend fun extractTasks(input: NoteExtractionInput): AiExtractionResult = nextResult

    override suspend fun downloadIfAvailable(): Flow<AiAvailability> = flowOf(availabilityState.value)

    fun setAvailability(value: AiAvailability) { availabilityState.value = value }
}
