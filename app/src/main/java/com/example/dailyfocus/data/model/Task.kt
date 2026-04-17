package com.example.dailyfocus.data.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.util.UUID

// 1. DEFINICIÓN DE LA DATA CLASS (DISEÑADA PARA SOSTENER DATOS)
// contiene las propiedades de una tarea
@Immutable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)