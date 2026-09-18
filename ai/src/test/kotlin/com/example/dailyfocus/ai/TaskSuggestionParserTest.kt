package com.example.dailyfocus.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskSuggestionParserTest {
    @Test fun `accepts only valid protocol lines`() {
        val result = TaskSuggestionParser().parse("Enviar reporte|0.9\n|0.5\nInválida|2\nLlamar|0.8")

        assertEquals(listOf("Enviar reporte", "Llamar"), result.map { it.title })
    }
}
